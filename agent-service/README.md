# 学习笔记助手 · AI 服务

这是网站的 Python 后端，负责笔记摘要、基于正文的流式问答，以及出题和批改。Vue 将 `/ai` 请求转发到 FastAPI。FastAPI 用当前请求的登录令牌向 Java 验证身份，并读取该账号的笔记。

## 本地启动

先启动 Java 后端（默认端口 `8080`），再从仓库根目录执行以下 PowerShell 命令。已有虚拟环境时可跳过创建步骤。

```powershell
cd agent-service
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
if (!(Test-Path .env)) { Copy-Item .env.example .env }
```

在本地 `.env` 中设置以下配置。实际 API Key 留在本地，仓库只提交 `.env.example`。

```dotenv
OPENROUTER_API_KEY=填入你自己的密钥
JAVA_API_URL=http://127.0.0.1:8080
```

`OPENROUTER_MODEL` 可用于指定笔记摘要模型。问答、出题和批改模型在对应 Python 模块中配置。

```powershell
.\.venv\Scripts\python.exe -m uvicorn web_app:app --host 127.0.0.1 --port 8000
```

启动 Vue 后，从网站登录并使用 AI 功能。整体运行步骤见[项目 README](../README.md)。

## 网站运行依赖

| 文件 | 作用 |
| --- | --- |
| `web_app.py` | FastAPI 入口，验证用户身份、管理会话并提供 AI 接口 |
| `summarize_note.py` | 调用模型生成摘要、知识点和复习问题，使用 Pydantic 校验结果 |
| `learn_rag.py` | 笔记分块与向量检索、LangChain Tool、多轮对话、消息摘要和流式输出 |
| `learn_graph.py`、`quiz_schema.py` | LangGraph 出题、暂停等待作答、恢复批改以及批改结果校验 |
| `quiz_mcp.py`、`skills/note-quiz/SKILL.md` | 出题时通过 MCP stdio 子进程读取技能规则，无需单独启动 MCP 服务 |
| `requirements.txt`、`.env.example` | Python 依赖及本地配置模板 |

`main.py`、`static/chat.html` 和其他独立学习脚本已列入此目录的 `.gitignore`。其中 `learn_rag.py` 与 `learn_graph.py` 是网站运行依赖。测试文件用于验证接口和会话逻辑。

## 问答和练习如何运行

- **正文检索**：新建对话时读取当前用户的笔记并建立向量索引。Agent 通过 `search_notes` 查询相关正文。修改笔记后，新建对话可读取最新内容。
- **多轮对话**：后续消息复用同一个 `thread_id`。短期上下文保存在 `InMemorySaver` 中。历史达到 20 条消息时触发压缩，目标是保留最近 8 条原始消息。
- **记忆范围**：消息摘要只服务于当前对话。项目未接入跨对话长期记忆。刷新网页后重新创建对话，重启 Python 会清空内存中的对话和练习状态。
- **流式输出**：问答接口按 NDJSON 逐行发送文本、完成或错误事件，Vue 接收后逐段追加内容。
- **出题与批改**：先读取已授权的笔记和 MCP 技能规则，再出题并暂停。提交答案后用原练习 ID 恢复流程，返回逐题反馈和待复习题目。

## 接口

除 Java 登录流程外，调用下列 AI 接口时均在 `Authorization` 请求头携带 Java 下发的原始令牌。正文参数使用 JSON。

| 方法和路径 | 参数与用途 |
| --- | --- |
| `POST /ai/conversations` | 无请求体，创建对话并返回 `thread_id` |
| `POST /ai/notes/ask/stream` | `question`、`thread_id`，流式回答 |
| `POST /ai/notes/summarize` | `note_id`，生成摘要 |
| `POST /ai/quizzes` | `note_id`，生成练习并返回练习 ID |
| `POST /ai/quizzes/{key}/answer` | 路径中的 `key` 为练习 ID，正文传 `answer` |

## 本地检查

在 `agent-service` 目录执行：

```powershell
.\.venv\Scripts\python.exe -m unittest test_web_logic -v
```

这组测试覆盖空答案校验、笔记访问、会话归属和分页逻辑。实际问答、摘要和批改需要 Java、模型服务及有效配置。
