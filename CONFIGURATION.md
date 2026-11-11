# 配置与运行指南 (Configuration & Running Guide)

本项目包含 Java (Spring Boot) 后端与 Python (agent-service) 服务。为了保证安全，所有敏感凭据（如数据库密码、JWT 密钥等）均不提交至 GitHub。

## 1. 环境准备 (Prerequisites)

在启动项目前，请确保你的电脑已安装并配置好以下环境：
*   **Java (JDK 17+)** 与 **Maven** （用于运行 Java 后端）
*   **Python (3.9+)** （用于运行 agent-service）
*   **MySQL** 和 **Redis** （你需要自行安装并在本地启动这两个服务）
*   **IDE**：推荐使用 IntelliJ IDEA

## 2. 配置文件初始化 (Configuration)

克隆代码后，你需要基于模板创建自己的本地配置文件。**请勿将填写了真实密码的文件提交到 Git。**

### 2.1 Java 后端配置
1. 找到 `config/local.properties.example` 文件。
2. 复制并重命名为 `config/local.properties`。
3. 编辑 `config/local.properties`，填入你**本地真实的**数据库连接、Redis 配置、JWT 密钥和 OSS 凭据。
   *(注：`src/main/resources/application.yaml` 中只包含配置变量引用，请勿直接修改里面为硬编码值)*

### 2.2 Python 服务配置
1. 进入 `agent-service` 文件夹。
2. 找到 `.env.example` 文件，复制并重命名为 `.env`。
3. 编辑 `.env`，填入你的大模型 API Key 等配置。

## 3. 启动项目 (Running)

### 3.1 启动 Java 后端
*   **IDE 启动**：在 IntelliJ IDEA 中打开项目。**非常重要：** 请确保你的运行配置 (Run/Debug Configurations) 中的 **Working directory** 设置为**项目根目录**（即 `big_event` 文件夹）。然后运行主启动类 `com.example.learn_note.LearnNoteApplication`。
*   **环境变量启动**：如果你不创建 `local.properties`，也可以直接在系统中设置与 `local.properties.example` 同名的环境变量，Spring 和 `LocalConfig` 会优先读取环境变量。

### 3.2 启动 Python 服务
进入 `agent-service` 目录，激活你的 Python 虚拟环境后，根据该目录下的说明文件启动服务。

## 4. 安全与 Git 提交规范

本项目已配置 `.gitignore`，以下文件绝对**不**应被提交：
*   `config/local.properties`
*   `agent-service/.env`
*   依赖目录（如 `target/`, `node_modules/`, `venv/` 等）
*   构建产物及本地上传文件

提交前，请务必使用以下命令核对：
```bash
git check-ignore config/local.properties agent-service/.env
git status --short