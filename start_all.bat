@echo off
echo ==========================================
echo       Cloud Bridge 一键启动脚本
echo ==========================================

:: 设置环境变量
set JAVA_HOME=E:\env\jdk-17
set MAVEN_HOME=E:\env\apache-maven-3.9.6
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo [1/2] 正在启动后端服务 (Spring Boot)...
start "Cloud Bridge Backend" cmd /k "cd cloud-bridge\backend && mvn spring-boot:run"

echo [2/2] 正在启动前端服务 (Vue/Vite)...
start "Cloud Bridge Frontend" cmd /k "cd cloud-bridge\frontend && npm run dev"

echo.
echo 服务已启动！
echo 后端地址: http://localhost:8080
echo 前端地址: http://localhost:5173
echo.
echo 请勿关闭弹出的命令行窗口。
pause
