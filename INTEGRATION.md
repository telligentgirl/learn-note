# 学习笔记助手：整合运行指南

## 启动

1. 启动 MySQL、Redis，在 IDEA 启动 Java（8080）。Java 详情、删除接口增加了归属检查，需重启生效。
2. 在 agent-service 目录执行：

   ```powershell
   .\.venv\Scripts\python.exe -m uvicorn web_app:app --host 127.0.0.1 --port 8000
   ```

   网站使用新入口 **web_app:app**，原 main.py 和学习脚本保留。不要同时启动两个占用 8000 的服务。
   .env 继续配置 JAVA_API_URL、OPENROUTER_API_KEY。网站不会使用 JAVA_API_TOKEN。
3. 在 frontend 目录执行 `npm run dev`，访问终端显示的地址。

## 请求流程

浏览器 → Vite /api 代理 → Java 登录、笔记、分类。
浏览器 → Vite /ai 代理 → Python → Java 验证当前 Token → 模型服务。

Java 原始 Token 放在 Authorization 请求头中，Python 每次请求都向 Java 验证。
每个新对话创建独立 Agent、笔记向量和检查点；练习使用独立图。
thread_id 还必须匹配登录用户和会话类型，知道编号并不等于有权限访问。

## 功能验收

- 登录后进入工作台；无效密码显示错误。
- 新建分类、笔记；分页查看、选择正文、删除自己的笔记。
- 为选中笔记生成结构化摘要。
- AI 问答首次发送自动创建对话；连续追问保留上下文，流式显示正文。
- 点击生成练习，提交答案，显示逐题批改和待复习题目。
- 空答案不提交；Python 停止时显示错误而不是假装成功。
- 修改笔记后新建对话读取最新知识库。

## 当前边界

登录 Token 和用户资料通过 pinia-plugin-persistedstate 保存到 localStorage，刷新后自动恢复；Token 在 Java 过期时仍须重新登录。Python 重启后须新建对话或练习。
新建对话会重新向量化当前账号的笔记；旧对话维持创建时快照。
笔记提供新增、分页筛选、详情、编辑、删除；新增了 PUT /article，需重启 Java。默认子路由为 /workspace/notes，AI 子路由为 /workspace/chat。
正文与模型文本以纯文字安全展示，暂未渲染富文本或 Markdown。
Vite 代理仅用于开发。生产部署需要配置反向代理、HTTPS 和会话容量/过期清理。

## 阅读顺序

frontend/src/views/LoginView.vue → stores/auth.js → views/WorkspaceView.vue → vite.config.js
→ agent-service/web_app.py → learn_rag.py / learn_graph.py。

## 检查

前端：`npm run build`，`npx eslint src vite.config.js`。
Python（agent-service 目录）：`.\.venv\Scripts\python.exe -m unittest test_web_app -v`。
Python 接口测试使用模拟模型和 Java 数据，不代表真实模型联调已通过。
