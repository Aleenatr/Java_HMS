#!/bin/bash
# Hospital Management System - Launcher Script (Linux/Mac)
# Java 21 LTS Version

echo ""
echo "========================================"
echo " Hospital Management System"
echo " Java 21 LTS Edition"
echo "========================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH!"
    echo "Please install JDK 21 from: https://adoptium.net/"
    exit 1
fi

echo "[1/3] Checking Java version..."
java -version

echo ""
echo "[2/3] Setting up classpath..."
CLASSPATH="mysql-connector-java-8.0.28.jar:ResultSet2xml.jar:src"

echo ""
echo "[3/3] Launching Hospital Management System..."
echo ""
java -cp "$CLASSPATH" hospital.management.system.Login

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Application failed to start!"
    echo "Please check:"
    echo " - MySQL is running"
    echo " - Database is created (run database_setup.sql)"
    echo " - Credentials in conn.java are correct"
    read -p "Press Enter to continue..."
fi
