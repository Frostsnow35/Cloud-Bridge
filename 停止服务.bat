@echo off
chcp 65001
cls
echo ========================================================
echo       云桥 (Cloud Bridge) 停止服务脚本
echo ========================================================
echo.

echo 正在停止所有服务...
docker compose down
if %errorlevel% neq 0 (
    docker-compose down
)

echo.
echo 服务已停止。
pause
