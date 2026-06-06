@echo off
setlocal

set "ROOT=%~dp0"

if not exist "%ROOT%assets\beatoraja.jar" (
    echo assets\beatoraja.jar was not found.
    echo Run build-windows-local-jdk.bat first, or copy a built lr2oraja jar to assets\beatoraja.jar.
    pause
    exit /b 1
)

call "%ROOT%assets\beatoraja-config.bat"
