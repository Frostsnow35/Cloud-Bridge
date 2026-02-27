# Cloud Bridge (云转桥) MVP

垂域AI驱动下科技成果智能匹配与转化平台 - MVP版本

## 项目结构
- `backend/`: Spring Boot 3.0 后端服务
- `frontend/`: Vue 3 + TypeScript 前端应用
- `deploy/`: Docker Compose 部署配置 (MySQL, Redis, Neo4j)

## 快速开始

### 1. 启动基础设施
进入 `deploy` 目录并启动 Docker 容器：
```bash
cd deploy
docker-compose up -d
```
这将启动 MySQL (3306), Redis (6379), Neo4j (7474/7687)。

### 2. 启动后端服务
进入 `backend` 目录：
```bash
cd backend
# 确保已安装 JDK 17+ 和 Maven
mvn spring-boot:run
```
后端服务将运行在 `http://localhost:8080`。
Swagger 文档: `http://localhost:8080/swagger-ui.html`

### 3. 启动前端应用
进入 `frontend` 目录：
```bash
cd frontend
npm install
npm run dev
```
前端应用将运行在 `http://localhost:5173`。

## 功能演示
1. **注册/登录**: 访问 `/login`，注册任意账号（MVP模式下不做严格校验）。
2. **需求发布**: 访问 `/needs` 查看需求列表。
3. **成果发布**: 访问 `/achievements` 查看成果列表。
4. **AI智能匹配**: 访问 `/match`，输入需求描述（如"Carbon Fiber"），系统将模拟调用AI并结合Neo4j图谱返回推荐成果。
5. **区块链存证**: 访问 `/evidence`，输入文本，点击"存证"，系统将模拟上链并返回交易哈希；使用该哈希可进行核验。

## 技术栈
- **Backend**: Spring Boot 3, Spring Data JPA, Spring Data Neo4j
- **Frontend**: Vue 3, TypeScript, Element Plus, Vite
- **Database**: MySQL 8.0, Redis, Neo4j 5.x
- **Blockchain**: FISCO BCOS (Mocked for MVP)
- **AI**: ChatGLM-6B (Mocked integration point)
