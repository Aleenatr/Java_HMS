@echo off
REM Hospital Management System - Launcher Script
REM Java 21 LTS Version

echo.
echo ========================================
echo  Hospital Management System
echo  Java 21 LTS Edition
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH!
    echo Please install JDK 21 from: https://adoptium.net/
    pause
    exit /b 1
)

echo [1/3] Checking Java version...
java -version

echo.
echo [2/3] Setting up classpath...
set CLASSPATH=mysql-connector-java-8.0.28.jar;ResultSet2xml.jar;src

echo.
echo [3/3] Launching Hospital Management System...
echo.
java -cp %CLASSPATH% hospital.management.system.Login

if errorlevel 1 (
    echo.
    echo ERROR: Application failed to start!
    echo Please check:
    echo  - MySQL is running
    echo  - Database is created ^(run database_setup.sql^)
    echo  - Credentials in conn.java are correct
    pause
)
