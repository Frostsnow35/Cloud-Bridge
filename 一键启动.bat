@echo off
cd /d "%~dp0"
title Cloud Bridge Launcher
cls
echo ========================================================
echo       云桥 (Cloud Bridge) 一键启动脚本
echo ========================================================
echo.

:: 1. Check Docker
docker --version >nul 2>&1
if %errorlevel% neq 0 goto DockerMissing

echo [1/4] 检测到 Docker 已安装，准备启动服务...
echo       注意：首次启动需要下载依赖和编译，可能需要 5-10 分钟，请耐心等待。
echo.

:: 2. Build and Start
echo [2/4] 正在构建并启动容器...
docker compose -p cloud-bridge up -d --build
if %errorlevel% neq 0 goto DockerFail

echo.
echo [3/4] 服务已后台启动，正在等待服务就绪...
echo       (后端初始化需要时间，请稍候...)

:CheckHealth
timeout /t 5 >nul
curl -I http://localhost:80 >nul 2>&1
if %errorlevel% neq 0 goto CheckHealth

echo.
echo [4/4] 服务已就绪！正在打开浏览器...
start http://localhost

echo.
echo ========================================================
echo       系统运行中
echo ========================================================
echo.
echo 如需停止服务，请运行根目录下的 "停止服务.bat"
echo.
pause
exit /b 0

:DockerMissing
echo.
echo [错误] 未检测到 Docker 环境！
echo 请先安装 Docker Desktop (Windows) 并启动。
echo 下载地址: https://www.docker.com/products/docker-desktop/
echo.
pause
exit /b 1

:DockerFail
echo.
echo [错误] 服务启动失败！
echo 请检查 Docker Desktop 是否正在运行。
echo.
pause
exit /b 1
