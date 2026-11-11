# 学习笔记助手 · Web 前端

使用 Vue 3、Vite、Vue Router 和 Pinia。页面提供注册登录、笔记和分类管理、AI 问答、摘要、练习与批改，以及个人资料和密码修改。

## 本地运行

使用 Node.js 22.18 或以上的 22.x 版本，或 Node.js 24.12 及以上版本。从仓库根目录运行：

```powershell
cd frontend
npm ci
npm run dev
```

打开终端显示的地址，通常为 http://localhost:5173。登录和笔记管理需要 Java 后端，AI 功能还需要 Python 服务。启动步骤见[项目 README](../README.md)和 [AI 服务说明](../agent-service/README.md)。

## 页面路由

| 路径 | 用途 |
| --- | --- |
| `/login` | 登录 |
| `/register` | 注册，成功后返回登录 |
| `/workspace/notes` | 默认工作台，管理笔记、分类，查看摘要和练习 |
| `/workspace/chat` | 笔记正文问答与多轮对话 |
| `/workspace/profile` | 查看和更新资料、修改密码 |

未登录访问工作台会跳到登录页。Pinia 持久化登录 Token 和用户资料，退出登录或 Token 失效时清除。修改密码成功后返回登录页。

## 接口调用

前端使用浏览器原生 `fetch`。[`src/api/request.js`](src/api/request.js) 统一处理 Java 请求的认证头、参数编码和业务响应。AI 问答读取 NDJSON 响应流并逐段展示。

| 浏览器路径 | Vite 开发代理 |
| --- | --- |
| `/api/*` | 转发至 Java 的 `127.0.0.1:8080`，移除 `/api` 前缀 |
| `/ai/*` | 转发至 Python 的 `127.0.0.1:8000`，保留 `/ai` 前缀 |

代理规则在 [vite.config.js](vite.config.js) 中。部署构建产物时，需要在托管端配置对应的 API 转发和 Vue Router 的 `index.html` 回退。

## 构建与检查

```powershell
npm run build
npm run lint
```

构建产物位于 `dist/`。`npm run lint` 会尝试自动修复可修复的问题。
