@echo off
cd /d "%~dp0"
title Cloud Bridge Stopper
cls
echo ========================================================
echo       Cloud Bridge - Stop Services
echo ========================================================
echo.

echo Stopping all services...
docker compose -p cloud-bridge down

if %errorlevel% neq 0 goto StopFail

echo.
echo Services stopped.
pause
exit /b 0

:StopFail
echo.
echo [NOTE] Stop command returned an error code.
echo This might be because services were not running.
pause
exit /b 1
