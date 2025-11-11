@echo off
REM Compile Hospital Management System
REM Java 21 LTS Version

echo.
echo ========================================
echo  Compiling Hospital Management System
echo  Java 21 LTS Edition
echo ========================================
echo.

REM Check if Java is installed
javac -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java compiler (javac) is not installed or not in PATH!
    echo Please install JDK 21 from: https://adoptium.net/
    pause
    exit /b 1
)

echo [1/2] Java Compiler Found
javac -version

echo.
echo [2/2] Compiling all Java files...
echo.

REM Create output directory if it doesn't exist
if not exist "out\production\classes" mkdir out\production\classes

REM Compile with proper classpath
javac -cp "mysql-connector-java-8.0.28.jar;ResultSet2xml.jar" -d out\production\classes src\hospital\management\system\*.java

if errorlevel 1 (
    echo.
    echo ERROR: Compilation failed!
    echo Check the error messages above.
    pause
    exit /b 1
)

echo.
echo ========================================
echo  Compilation Successful!
echo ========================================
echo.
echo You can now run the application using:
echo   run_hospital.bat
echo.
echo Or manually:
echo   java -cp "mysql-connector-java-8.0.28.jar;ResultSet2xml.jar;src" hospital.management.system.Login
echo.
pause
