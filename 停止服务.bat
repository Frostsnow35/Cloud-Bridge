@echo off
cd /d "%~dp0"
title Cloud Bridge Stopper
cls
echo ========================================================
echo       云桥 (Cloud Bridge) 停止服务脚本
echo ========================================================
echo.

echo 正在停止所有服务...
docker compose -p cloud-bridge down

if %errorlevel% neq 0 goto StopFail

echo.
echo 服务已停止。
pause
exit /b 0

:StopFail
echo.
echo [注意] 停止命令返回了错误代码，但这可能只是因为服务未运行。
echo 您可以尝试手动在 Docker Desktop 中检查。
pause
exit /b 1
