@echo off
REM *** Set system-wide "_JAVA_OPTIONS" environment variable to use OpenGL pipeline (improved performance of > 30% potentially. Also use anti-aliasing for non-LR2 fonts, and finally allow Swing framework to utilize AA and GTKLookAndFeel for config window. ***
set "_JAVA_OPTIONS=-Dsun.java2d.opengl=true -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -Dswing.defaultlaf=com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
set "LOCAL_JAVA=%~dp0..\jdk-17.0.19-full\bin\java.exe"

if not exist "%LOCAL_JAVA%" (
    echo Local JDK was not found: "%LOCAL_JAVA%"
    echo Put jdk-17.0.19-full next to the assets folder, or edit this script.
    pause
    exit /b 1
)

pushd "%~dp0"
"%LOCAL_JAVA%" -Xms1g -Xmx4g -jar beatoraja.jar
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%
