@echo off
cd /d "%~dp0"
title Cloud Bridge Launcher
cls
echo ========================================================
echo       Cloud Bridge - One Click Start
echo ========================================================
echo.

:: 1. Check Docker
echo [1/4] Checking Docker environment...
docker --version >nul 2>&1
if %errorlevel% neq 0 goto DockerMissing

echo       Docker found. Preparing to start services...
echo       NOTE: First run requires downloading images (5-10 mins).
echo.

:: 2. Build and Start
echo [2/4] Building and starting containers...
docker compose -p cloud-bridge up -d --build
if %errorlevel% neq 0 goto DockerFail

echo.
echo [3/4] Services started in background. Waiting for readiness...
echo       (Backend initialization takes time, please wait...)

:CheckHealth
timeout /t 5 >nul
curl -I http://localhost:80 >nul 2>&1
if %errorlevel% neq 0 goto CheckHealth

echo.
echo [4/4] Services Ready! Opening browser...
start http://localhost

echo.
echo ========================================================
echo       System Running
echo ========================================================
echo.
echo To stop services, run "stop_services.bat"
echo.
pause
exit /b 0

:DockerMissing
echo.
echo [ERROR] Docker not found!
echo Please install Docker Desktop for Windows and start it.
echo Download: https://www.docker.com/products/docker-desktop/
echo.
pause
exit /b 1

:DockerFail
echo.
echo [ERROR] Service start failed!
echo Please check if Docker Desktop is running.
echo.
pause
exit /b 1
