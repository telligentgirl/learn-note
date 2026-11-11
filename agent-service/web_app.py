"""网站整合入口：每次请求由 Java 验证身份，学习脚本仍可单独运行。"""
import os
import json
import asyncio
import sys
from pathlib import Path
from uuid import uuid4
from threading import Lock
import requests
from fastapi import FastAPI, Header, HTTPException, Depends
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field, ConfigDict
from learn_rag import build_rag_agent, stream_notes
from learn_graph import build_quiz_graph, start_quiz, submit_answer
from summarize_note import summarize_note
from langchain_mcp_adapters.client import MultiServerMCPClient

app = FastAPI(title="学习笔记助手整合 API")
# 内存会话：重启会清空。存用户 ID，不存密码或长期保存登录 Token。
sessions = {}
guard = Lock()

def java_get(path, token, params=None):
    try:
        with requests.Session() as session:
            session.trust_env = False
            response = session.get(os.environ['JAVA_API_URL'].rstrip('/') + path,
                headers={'Authorization': token}, params=params, timeout=10)
        if response.status_code == 401:
            raise HTTPException(401, '登录已失效，请重新登录。')
        response.raise_for_status()
        result = response.json()
        if result.get('code') != 0:
            raise HTTPException(502, 'Java 查询失败。')
        return result['data']
    except requests.RequestException as exc:
        raise HTTPException(502, '无法连接 Java 服务。') from exc

def identity(authorization: str = Header()):
    user = java_get('/user/userInfo', authorization)
    return user['id'], authorization

def notes_for(token):
    # 读取全部分页，不能只把前 20 条笔记放进知识库。
    notes, page = [], 1
    while True:
        data = java_get('/article', token, {'pageNum': page, 'pageSize': 100})
        items = data['items']
        notes.extend(items)
        if not items or len(notes) >= data['total']:
            return notes
        page += 1

class TextBody(BaseModel):
    model_config = ConfigDict(str_strip_whitespace=True, extra='forbid')
    question: str = Field(min_length=1)
    thread_id: str

class NoteBody(BaseModel):
    note_id: int = Field(gt=0)

async def fetch_quiz_skill():
    # stdio 客户端自动启动和关闭子进程，不需要额外端口。
    client = MultiServerMCPClient({'quiz': {
        'transport': 'stdio', 'command': sys.executable,
        'args': [str(Path(__file__).with_name('quiz_mcp.py').resolve())],
    }})
    resources = await client.get_resources(server_name='quiz', uris=['skill://note-quiz/rules'])
    rules = '\n'.join(item.as_string() for item in resources)
    if not rules.strip():
        raise RuntimeError('MCP 出题规则为空')
    return rules

class AnswerBody(BaseModel):
    model_config = ConfigDict(str_strip_whitespace=True, extra='forbid')
    answer: str = Field(min_length=1)

def owned_note(note_id, token):
    # 使用 Java 已按登录用户过滤的列表，避免仅凭任意 ID 读取其他用户笔记。
    note = next((n for n in notes_for(token) if n['id'] == note_id), None)
    if note is None:
        raise HTTPException(404, '笔记不存在或不属于当前账号。')
    return note

def save_session(uid, kind, value):
    key = str(uuid4())
    with guard:
        sessions[key] = (uid, kind, value, Lock())
    return key

def get_session(key, uid, kind):
    with guard:
        item = sessions.get(key)
    if item is None or item[0] != uid or item[1] != kind:
        raise HTTPException(404, '会话不存在，请重新创建。')
    return item[2], item[3]

@app.post('/ai/conversations')
def conversation(auth=Depends(identity)):
    try:
        agent = build_rag_agent(notes_for(auth[1]))
        return {'thread_id': save_session(auth[0], 'chat', agent)}
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(502, '知识库准备失败，请检查模型与向量服务配置。') from exc

@app.post('/ai/notes/ask/stream')
def chat(body: TextBody, auth=Depends(identity)):
    agent, lock = get_session(body.thread_id, auth[0], 'chat')
    if not lock.acquire(blocking=False):
        raise HTTPException(409, '当前会话正在回答，请稍后再试。')
    def events():
        try:
            for text in stream_notes(agent, body.question, body.thread_id):
                yield json.dumps({'type': 'text', 'content': text}, ensure_ascii=False) + '\n'
            yield '{"type":"done"}\n'
        except Exception:
            yield json.dumps({'type': 'error', 'message': '模型回答失败，请重试。'}, ensure_ascii=False) + '\n'
        finally:
            lock.release()
    return StreamingResponse(events(), media_type='application/x-ndjson')

@app.post('/ai/notes/summarize')
def summary(body: NoteBody, auth=Depends(identity)):
    note = owned_note(body.note_id, auth[1])
    try:
        return summarize_note(note['content'])
    except Exception as exc:
        raise HTTPException(502, '生成摘要失败，请检查模型服务。') from exc

def quiz_result(result, thread_id):
    if result.get('__interrupt__'):
        return {'thread_id': thread_id, 'status': 'waiting_for_answer', **result['__interrupt__'][0].value}
    return {'thread_id': thread_id, 'status': 'completed', 'grading': result['grading'],
            'feedback': result['feedback'], 'review_items': result['review_items']}

@app.post('/ai/quizzes')
def quiz(body: NoteBody, auth=Depends(identity)):
    note = owned_note(body.note_id, auth[1])
    try:
        rules = asyncio.run(asyncio.wait_for(fetch_quiz_skill(), timeout=20))
    except Exception as exc:
        raise HTTPException(502, '无法通过 MCP 加载出题规则。') from exc
    # 节点闭包持有已授权的正文，不再读取 .env 中的固定用户 Token。
    graph = build_quiz_graph(lambda state: {
        'note_content': f"标题：{note['title']}\n正文：{note['content']}",
        'quiz_rules': rules,
    })
    key = save_session(auth[0], 'quiz', graph)
    try:
        return quiz_result(start_quiz(graph, body.note_id, key), key)
    except Exception as exc:
        with guard:
            sessions.pop(key, None)
        raise HTTPException(502, '出题失败，请重试。') from exc

@app.post('/ai/quizzes/{key}/answer')
def answer(key: str, body: AnswerBody, auth=Depends(identity)):
    graph, lock = get_session(key, auth[0], 'quiz')
    if not lock.acquire(blocking=False):
        raise HTTPException(409, '正在批改，请勿重复提交。')
    try:
        return quiz_result(submit_answer(graph, body.answer, key), key)
    except ValueError as exc:
        raise HTTPException(400, str(exc)) from exc
    except Exception as exc:
        raise HTTPException(502, '批改失败，请重试。') from exc
    finally:
        lock.release()
