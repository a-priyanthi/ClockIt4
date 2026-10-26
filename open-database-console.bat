@echo off
title ClockIt - H2 SQL Database Console
color 0b
echo =====================================================================
echo   CLOCKIT - H2 SQL DATABASE BROWSER CONSOLE
echo =====================================================================
echo.
echo Database Connection Credentials:
echo   - Driver Class: org.h2.Driver
echo   - JDBC URL:     jdbc:h2:./data/academic_clock
echo   - User Name:    sa
echo   - Password:     (leave empty)
echo.
echo Launching H2 SQL Web Console in your default browser...
echo.
java -cp "lib/h2.jar" org.h2.tools.Console -web -browser
pause
