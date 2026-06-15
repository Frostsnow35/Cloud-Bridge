# Cloud Bridge 部署指南

## 概述

本文档描述如何将 Cloud Bridge 项目从本地开发环境部署到云服务器，实现服务的持续运行。

---

## 一、本地环境与云端环境对比

| 项目 | 本地开发环境 | 云服务器部署 |
|------|-------------|-------------|
| Elasticsearch | 手动启动，进程随终端终止 | Docker容器，自动重启 |
| 数据存储 | 临时目录，重启丢失 | 持久化卷，数据安全 |
| 访问方式 | localhost:9200 | 公网IP:9200 |
| 可用性 | 依赖本地电脑开机 | 24小时运行 |
| 扩展性 | 单机 | 可水平扩展 |

---

## 二、推荐配置

### 2.1 云服务器配置

```
最低配置：
- CPU: 2核
- 内存: 4GB（ES至少需要2G）
- 硬盘: 50GB SSD
- 带宽: 5Mbps

推荐配置：
- CPU: 4核
- 内存: 8GB
- 硬盘: 100GB SSD
- 带宽: 10Mbps
```

推荐云服务商：
- 阿里云 ECS
- 腾讯云 CVM
- 华为云 ECS

### 2.2 端口规划

| 服务 | 端口 | 说明 |
|------|------|------|
| Elasticsearch | 9200, 9300 | 搜索服务 |
| Spring Boot后端 | 8080 | API服务 |
| Nginx前端 | 80 | Web服务 |
| HTTPS | 443 | 安全访问（可选） |

---

## 三、Docker容器化部署

### 3.1 目录结构

```
/opt/cloud-bridge/
├── docker-compose.yml
├── nginx.conf
├── backend/
│   └── Dockerfile
└── frontend/
    └── dist/          # npm run build 产物
```

### 3.2 docker-compose.yml

```yaml
version: '3.8'

services:
  elasticsearch:
    image: elasticsearch:8.11.0
    container_name: cloud-bridge-es
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - ES_JAVA_OPTS=-Xms2g -Xmx2g
      - cluster.name=cloud-bridge-cluster
      - cluster.routing.allocation.disk.watermark.low=1gb
      - cluster.routing.allocation.disk.watermark.high=500mb
      - cluster.routing.allocation.disk.watermark.flood_stage=200mb
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - es-data:/usr/share/elasticsearch/data
    restart: always
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9200 || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: cloud-bridge-backend
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - ELASTICSEARCH_HOST=elasticsearch
      - ELASTICSEARCH_PORT=9200
    ports:
      - "8080:8080"
    depends_on:
      elasticsearch:
        condition: service_healthy
    restart: always

  frontend:
    image: nginx:alpine
    container_name: cloud-bridge-frontend
    volumes:
      - ./frontend/dist:/usr/share/nginx/html:ro
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: always

volumes:
  es-data:
    driver: local
```

### 3.3 nginx.conf

```nginx
server {
    listen 80;
    server_name _;

    root /usr/share/nginx/html;
    index index.html;

    # 前端路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 后端详细日志
    access_log /var/log/nginx/backend-access.log;
    error_log /var/log/nginx/backend-error.log;
}
```

### 3.4 后端 Dockerfile

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 3.5 后端 application-prod.yml

```yaml
spring:
  elasticsearch:
    uris: http://elasticsearch:9200

server:
  port: 8080

logging:
  level:
    com.cloudbridge: INFO
    org.elasticsearch: WARN
```

---

## 四、部署步骤

### 4.1 服务器环境准备

```bash
# 1. 更新系统
sudo apt update && sudo apt upgrade -y

# 2. 安装Docker
curl -fsSL https://get.docker.com | sh

# 3. 安装Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 4. 验证安装
docker --version
docker-compose --version

# 5. 设置Docker开机自启
sudo systemctl enable docker
```

### 4.2 一键部署脚本

创建 `deploy.sh`：

```bash
#!/bin/bash
set -e

PROJECT_DIR="/opt/cloud-bridge"
cd "$PROJECT_DIR"

echo "[$(date)] 开始部署 Cloud Bridge..."

# 1. 拉取最新代码
echo "[1/5] 拉取最新代码..."
git pull origin main

# 2. 构建前端
echo "[2/5] 构建前端..."
cd "$PROJECT_DIR/cloud-bridge/frontend"
npm install
npm run build

# 3. 重启服务
echo "[3/5] 重启Docker服务..."
cd "$PROJECT_DIR"
docker-compose down
docker-compose up -d --build

# 4. 等待服务就绪
echo "[4/5] 等待服务启动..."
sleep 30

# 5. 验证服务状态
echo "[5/5] 验证服务状态..."
if curl -s http://localhost:9200 > /dev/null; then
    echo "✅ Elasticsearch: OK"
else
    echo "❌ Elasticsearch: FAIL"
fi

if curl -s http://localhost:8080/api/health > /dev/null; then
    echo "✅ Backend: OK"
else
    echo "❌ Backend: FAIL"
fi

echo "[$(date)] 部署完成！"
docker-compose ps
```

```bash
chmod +x deploy.sh
```

### 4.3 开机自启配置

创建 systemd 服务 `/etc/systemd/system/cloud-bridge.service`：

```ini
[Unit]
Description=Cloud Bridge Application
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/cloud-bridge
ExecStart=/usr/local/bin/docker-compose up -d
ExecStop=/usr/local/bin/docker-compose down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
```

```bash
# 启用服务
sudo systemctl enable cloud-bridge

# 手动控制
sudo systemctl start cloud-bridge
sudo systemctl stop cloud-bridge
sudo systemctl restart cloud-bridge
sudo systemctl status cloud-bridge
```

---

## 五、监控与维护

### 5.1 服务健康检查

```bash
# 检查所有服务状态
docker-compose ps

# 检查ES集群健康
curl http://localhost:9200/_cluster/health

# 检查后端API
curl http://localhost:8080/api/health

# 查看实时日志
docker-compose logs -f
docker-compose logs -f elasticsearch
docker-compose logs -f backend
```

### 5.2 自动监控脚本

创建 `monitor.sh`：

```bash
#!/bin/bash
LOG_FILE="/var/log/cloud-bridge-monitor.log"

while true; do
    TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
    
    # 检查ES
    if curl -s http://localhost:9200 > /dev/null; then
        echo "[$TIMESTAMP] Elasticsearch: OK" >> "$LOG_FILE"
    else
        echo "[$TIMESTAMP] Elasticsearch: FAIL - restarting..." >> "$LOG_FILE"
        docker-compose restart elasticsearch
    fi
    
    # 检查后端
    if curl -s http://localhost:8080/actuator/health > /dev/null; then
        echo "[$TIMESTAMP] Backend: OK" >> "$LOG_FILE"
    else
        echo "[$TIMESTAMP] Backend: FAIL - restarting..." >> "$LOG_FILE"
        docker-compose restart backend
    fi
    
    sleep 60
done
```

### 5.3 日志管理

```bash
# 限制日志大小
docker-compose logs --tail=100 > /tmp/cloud-bridge-latest.log

# 清理旧日志（添加到crontab）
# 0 2 * * * /opt/cloud-bridge/clean-logs.sh
```

创建 `clean-logs.sh`：

```bash
#!/bin/bash
find /var/log -name "cloud-bridge-*.log" -mtime +7 -delete
docker system prune -f --filter "until=24h"
```

---

## 六、数据迁移

### 6.1 从本地ES导出数据

在本地执行：

```bash
# 创建快照仓库
curl -X PUT "localhost:9200/_snapshot/local_backup" -H 'Content-Type: application/json' -d'
{
  "type": "fs",
  "settings": {
    "location": "/tmp/es-backup"
  }
}
'

# 快照所有索引
curl -X PUT "localhost:9200/_snapshot/local_backup/snapshot_001?wait_for_completion=true"

# 打包备份
tar -czvf es-backup.tar.gz /tmp/es-backup
```

### 6.2 导入到云端ES

在服务器执行：

```bash
# 创建备份目录
docker exec cloud-bridge-es mkdir -p /usr/share/elasticsearch/backups

# 复制备份文件
docker cp es-backup.tar.gz cloud-bridge-es:/usr/share/elasticsearch/backups/

# 解压
docker exec cloud-bridge-es tar -xzvf /usr/share/elasticsearch/backups/es-backup.tar.gz -C /usr/share/elasticsearch/backups/

# 创建仓库
curl -X PUT "localhost:9200/_snapshot/cloud_backup" -H 'Content-Type: application/json' -d'
{
  "type": "fs",
  "settings": {
    "location": "/usr/share/elasticsearch/backups/es-backup"
  }
}
'

# 恢复快照
curl -X POST "localhost:9200/_snapshot/cloud_backup/snapshot_001/_restore"
```

---

## 七、安全建议

### 7.1 防火墙配置

```bash
# 只开放必要端口
sudo ufw allow 22   # SSH
sudo ufw allow 80   # HTTP
sudo ufw allow 443  # HTTPS
sudo ufw enable
```

### 7.2 HTTPS配置（推荐）

使用 Let's Encrypt 免费证书：

```bash
# 安装certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com
```

### 7.3 定期备份

添加 crontab 任务：

```bash
# 每天凌晨3点备份ES数据
0 3 * * * /opt/cloud-bridge/backup-es.sh

# 每周日凌晨4点执行系统更新
0 4 * * 0 sudo apt update && sudo apt upgrade -y
```

---

## 八、故障排查

### 常见问题

| 问题 | 可能原因 | 解决方案 |
|------|---------|---------|
| ES启动失败 | 内存不足 | 增加服务器内存或降低ES_JAVA_OPTS |
| 后端无法连接ES | 网络问题 | 检查docker网络配置 |
| 前端404 | 路由配置错误 | 检查nginx try_files配置 |
| 磁盘空间不足 | ES数据过大 | 启用ILM策略或扩容磁盘 |

### 日志位置

- Docker日志：`docker-compose logs`
- Nginx日志：`/var/log/nginx/`
- ES日志：`docker exec cloud-bridge-es ls /usr/share/elasticsearch/logs`

---

## 九、快速参考

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 重启所有服务
docker-compose restart

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 进入ES容器
docker exec -it cloud-bridge-es /bin/bash

# 重新构建并启动
docker-compose up -d --build
```

---

*文档更新时间：2026-06-15*
