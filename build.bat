@echo off
if not exist bin mkdir bin
dir /s /b src\*.java > sources_raw.txt
powershell -NoProfile -Command "$lines = Get-Content sources_raw.txt; $fwd = foreach($l in $lines){ '\"' + ($l -replace '\\', '/') + '\"' }; Set-Content -Path sources.txt -Value $fwd -Encoding ascii"
javac -encoding UTF-8 -cp "lib/h2.jar" -d bin @sources.txt
if %ERRORLEVEL% equ 0 (
    echo [BUILD SUCCESS] All Java classes compiled cleanly into bin\
) else (
    echo [BUILD FAILED] Compilation errors encountered.
)
