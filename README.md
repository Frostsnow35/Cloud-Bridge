# Cloud Bridge (云转桥)

**垂域AI驱动下科技成果智能匹配与转化平台**

## 🚀 快速启动

### 💻 Windows 本地一键启动 (开发/演示)
双击运行根目录下的脚本：
- **启动服务**: `一键启动.bat` (自动构建并打开浏览器)
- **停止服务**: `停止服务.bat`
- 详细说明请阅读: [本地测试配置指引.md](./本地测试配置指引.md)

### ☁️ 自动化部署 (GitHub Actions)
本项目已配置 CI/CD 流水线，支持 **Push to Deploy** (类似 Vercel 体验)。
只需将代码推送到 `main` 分支，系统会自动构建并部署到您的 Linux 服务器。

**配置步骤**:
1. 在 GitHub 仓库 Settings -> Secrets and variables -> Actions 中添加：
   - `SERVER_HOST`: 服务器IP (如 36.140.97.212)
   - `SERVER_USER`: root
   - `SERVER_PASSWORD`: 服务器密码
   - `SERVER_PORT`: 8022 (SSH端口)
   - `AI_API_KEY`: 您的AI密钥

2. 修改代码并提交：
   ```bash
   git add .
   git commit -m "update feature"
   git push origin main
   ```

3. 在 GitHub Actions 页面查看部署进度。

## 📂 项目结构
- `cloud-bridge/`: 核心源代码 (Backend + Frontend)
- `数据集/`: 演示用真实数据集 (CSV)
- `docs/`: 设计文档与任务说明
- `docker-compose.yml`: 容器编排配置
- `.github/workflows/`: 自动化部署配置

## 🛠️ 技术栈
- **前端**: Vue 3 + TypeScript + Element Plus
- **后端**: Spring Boot 3 + JDK 17
- **数据库**: PostgreSQL + Neo4j (图数据库) + Elasticsearch (全文检索)
- **AI模型**: Qwen2.5-72B (通过 API 集成)
