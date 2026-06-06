@echo off
setlocal

set "ROOT=%~dp0"
set "JAVA_HOME=%ROOT%jdk-17.0.19-full"
set "PATH=%JAVA_HOME%\bin;%PATH%"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo Local JDK was not found: "%JAVA_HOME%"
    pause
    exit /b 1
)

if not exist "%JAVA_HOME%\jmods\javafx.controls.jmod" (
    echo This project requires JDK 17 with JavaFX.
    echo The local JDK does not contain JavaFX modules: "%JAVA_HOME%"
    echo Replace it with BellSoft Liberica Full JDK 17, then run this again.
    pause
    exit /b 1
)

pushd "%ROOT%"

if not exist "core\dependencies\jbms-parser\src\bms\model\BMSModel.java" (
    echo Missing jbms-parser sources. Initializing submodules...
    call git submodule update --init --recursive
    if exist "core\dependencies\jbms-parser\.git" (
        call git -C core/dependencies/jbms-parser reset --hard HEAD
    )
)

if not exist "core\dependencies\jbmstable-parser\src\bms\table\BMSTable.java" (
    echo Missing jbmstable-parser sources. Initializing submodules...
    call git submodule update --init --recursive
    if exist "core\dependencies\jbmstable-parser\.git" (
        call git -C core/dependencies/jbmstable-parser reset --hard HEAD
    )
)

if not exist "core\dependencies\jbms-parser\src\bms\model\BMSModel.java" (
    echo jbms-parser sources are still missing.
    echo Run: git submodule update --init --recursive
    popd
    pause
    exit /b 1
)

if not exist "core\dependencies\jbmstable-parser\src\bms\table\BMSTable.java" (
    echo jbmstable-parser sources are still missing.
    echo Run: git submodule update --init --recursive
    popd
    pause
    exit /b 1
)

call gradlew.bat --stop
call gradlew.bat --no-daemon core:shadowJar -Dplatform=windows
if errorlevel 1 (
    popd
    pause
    exit /b 1
)

for /f "delims=" %%F in ('dir /b /o-d "dist\lr2oraja-*-windows-*.jar" 2^>nul') do (
    copy /y "dist\%%F" "assets\beatoraja.jar" >nul
    echo Copied dist\%%F to assets\beatoraja.jar
    popd
    exit /b 0
)

echo Built jar was not found in dist.
popd
pause
exit /b 1
