@echo off
chcp 65001 >nul
cd /d "%~dp0src"
if not exist ..\out mkdir ..\out
javac -encoding UTF-8 -d ..\out *.java
if errorlevel 1 (echo Compile failed & pause & exit /b 1)
java -cp ..\out HospitalGUI
