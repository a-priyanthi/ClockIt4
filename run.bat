@echo off
echo ==========================================================
echo  MIT ACADEMIC CLOCK -- FULLSTACK JAVA PLATFORM
echo  Course: CS23304 Java Programming Capstone
echo ==========================================================

REM Check if compiled classes exist, otherwise build
if not exist bin\edu\mit\academicclock\Main.class (
    echo [INFO] Binaries not found. Running compilation build...
    call build.bat
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] Build failed. Please check javac output.
        pause
        exit /b 1
    )
)

echo [INFO] Starting Java Academic Clock Server on port 8080...
echo [INFO] Opening http://localhost:8080 in your browser...
start http://localhost:8080

java -cp "bin;lib/h2.jar" edu.mit.academicclock.Main
