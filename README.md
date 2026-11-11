# 学习笔记助手

一个结合笔记管理与 AI 辅助学习的 Web 应用。用户可以整理自己的笔记，根据正文生成摘要、检索问答和复习题，并在提交答案后查看逐题反馈。

## 功能

- **账户管理**：注册、登录、查看与修改个人资料、修改密码。修改密码后重新登录。
- **笔记管理**：新建、查看、编辑和删除纯文字笔记，支持自定义分类、状态筛选和分页。
- **AI 摘要**：从选中笔记提取摘要、关键知识点和复习问题。
- **AI 问答**：基于当前用户的笔记正文进行向量检索，流式展示回答，支持同一对话内追问。
- **笔记练习**：根据选中笔记生成题目，暂停等待作答，提交后显示结构化批改和待复习题目。

## AI 实现

| 能力 | 网站中的用法 |
| --- | --- |
| Tool 与 RAG | LangChain Agent 调用 `search_notes` 检索正文片段，回答时标注笔记来源。 |
| 短期记忆 | `InMemorySaver` 按 `thread_id` 保留对话状态。历史达到 20 条消息时，摘要中间件压缩较早消息，并尝试保留最近 8 条。 |
| Skill 与 MCP | 出题接口通过 MCP stdio 子进程读取 `skills/note-quiz/SKILL.md` 资源，再将规则交给 LangGraph 出题节点。子进程由客户端启动，无需额外端口。 |
| Stream | Agent 文本片段通过 NDJSON 发送给 Vue，前端逐行解析并追加显示。 |
| 工作流与结构化输出 | LangGraph 中断与恢复实现等待作答，Pydantic 校验摘要和批改结构。 |

## 项目结构

| 目录 | 技术与职责 |
| --- | --- |
| `frontend/` | Vue 3、Vite、Vue Router、Pinia。使用浏览器 `fetch` 发送请求，持久化登录状态。 |
| `src/` | Java 17、Spring Boot、MyBatis、MySQL、Redis。处理账户、鉴权、分类和笔记。 |
| `agent-service/` | Python、FastAPI、LangChain、LangGraph。处理摘要、向量检索、流式问答和练习流程。 |
| `config/` | Java 本地配置模板。 |

开发环境中，Vite 将 `/api` 转发给 Java，将 `/ai` 转发给 Python。Python 通过 Java 验证登录令牌，再读取当前账号的笔记。正文相关的模型和向量请求通过 OpenRouter 调用。

## 本地运行

准备 JDK 17、MySQL、Redis、Node.js 22.18 或以上的 22.x 版本（也可用 24.12 或以上版本）、Python 和 OpenRouter API Key。Python 已在 3.13 环境中开发。以下命令以 Windows PowerShell 为例。

### 1. 配置并启动 Java

启动 MySQL 和 Redis，准备已有的「大事件」数据库表结构（`user`、`category`、`article`）。仓库当前没有附带初始化 SQL，空数据库需要先导入对应表结构。

在仓库根目录复制配置模板，已有本地配置时跳过复制。

```powershell
Copy-Item config/local.properties.example config/local.properties
```

编辑 `config/local.properties`，填写数据库地址、账号密码、Redis 地址和自定义的 `JWT_SECRET`。OSS 配置仅在使用旧的文件上传接口时需要。在 IDEA 中从项目根目录运行 `com.example.learn_note.LearnNoteApplication`，或执行：

```powershell
.\mvnw.cmd spring-boot:run
```

Java 默认监听 `8080`。配置字段及忽略规则见 [CONFIGURATION.md](CONFIGURATION.md)。

### 2. 启动 Python Agent

在新终端中，从仓库根目录执行：

```powershell
cd agent-service
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
Copy-Item .env.example .env
```

已有 `.env` 时保留原文件。填写 `OPENROUTER_API_KEY`，并在 `.env` 中添加 `JAVA_API_URL=http://127.0.0.1:8080`。网站版使用当前用户的登录令牌访问 Java。

```powershell
.\.venv\Scripts\python.exe -m uvicorn web_app:app --host 127.0.0.1 --port 8000
```

### 3. 启动 Vue 前端

在新终端中，从仓库根目录执行：

```powershell
cd frontend
npm ci
npm run dev
```

打开终端显示的地址，通常为 [http://localhost:5173](http://localhost:5173)。未登录时进入登录页，可从「去注册」创建账号。登录后默认进入笔记列表，可先创建分类，再新增笔记并在详情中试用摘要和练习。

## 使用说明

- AI 对话使用创建时的笔记快照。修改笔记后，可点击「新建对话」读取最新内容。
- 问答和练习状态保存在 Python 服务内存中。重启 Python 后，重新创建对话或练习。
- 分类以笔记管理页面显示的数据为准，AI 问答主要处理正文内容。
- 本地配置 `config/local.properties` 和 `agent-service/.env` 已列入 Git 忽略规则。共享项目时提交对应的示例文件。

更多内容见 [整合运行指南](INTEGRATION.md) 和 [后端接口参数核对](API_AUDIT.md)。
