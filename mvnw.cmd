@echo off
rem ----------------------------------------------------------------------------
rem Apache Maven Wrapper for Windows
rem ----------------------------------------------------------------------------
rem This file was added to allow executing the maven wrapper on Windows (PowerShell/CMD).
rem It invokes the maven-wrapper.jar in the .mvn\wrapper directory.

setlocal enabledelayedexpansion

nREM Resolve the directory this script is in
set MVNW_DIR=%~dp0
nREM Trim trailing backslash if present
if "%MVNW_DIR:~-1%"=="\" set MVNW_DIR=%MVNW_DIR:~0,-1%

nREM Wrapper jar path
set MVNW_WRAPPER_JAR=%MVNW_DIR%\.mvn\wrapper\maven-wrapper.jar
n
nif not exist "%MVNW_WRAPPER_JAR%" (
  echo ERROR: Maven wrapper jar not found: %MVNW_WRAPPER_JAR%
  echo Please ensure the .mvn\wrapper directory and maven-wrapper.jar exist.
  exit /b 1
)

nREM Java executable
nif defined JAVA_HOME (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
n) else (
  set JAVA_EXE=java
n)

n"%JAVA_EXE%" -version >nul 2>&1
nif errorlevel 1 (
  echo ERROR: Java not found. Please install JDK 17 and set JAVA_HOME environment variable.
  exit /b 1
)

nREM Build the argument list and invoke the wrapper
nset ARGS=
:loopArgs
if "%~1"=="" goto endLoopArgs
  set ARGS=!ARGS! "%~1"
  shift
goto loopArgs
:endLoopArgs

n"%JAVA_EXE%" -jar "%MVNW_WRAPPER_JAR%" !ARGS!
endlocal

