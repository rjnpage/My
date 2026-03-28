@echo off
setlocal enabledelayedexpansion

set APP_HOME=%~dp0
set PROPS_FILE=%APP_HOME%gradle\wrapper\gradle-wrapper.properties

if not exist "%PROPS_FILE%" (
  echo Missing %PROPS_FILE%
  exit /b 1
)

for /f "tokens=1,* delims==" %%A in (%PROPS_FILE%) do (
  if "%%A"=="distributionUrl" set DIST_URL=%%B
)
set DIST_URL=%DIST_URL:\:=:%
for %%I in (%DIST_URL%) do set DIST_FILE=%%~nxI
set DIST_NAME=%DIST_FILE:.zip=%

if "%GRADLE_USER_HOME%"=="" set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set DIST_DIR=%GRADLE_USER_HOME%\wrapper\dists\%DIST_NAME%

for /f "delims=" %%G in ('dir /b /s "%DIST_DIR%\bin\gradle.bat" 2^>nul') do (
  set GRADLE_BIN=%%G
  goto runGradle
)

set TMP_ZIP=%GRADLE_USER_HOME%\wrapper\%DIST_NAME%.zip
if not exist "%GRADLE_USER_HOME%\wrapper" mkdir "%GRADLE_USER_HOME%\wrapper"
if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"

echo Downloading Gradle distribution: %DIST_URL%
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing '%DIST_URL%' -OutFile '%TMP_ZIP%'"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%TMP_ZIP%' -DestinationPath '%DIST_DIR%' -Force"

for /f "delims=" %%G in ('dir /b /s "%DIST_DIR%\bin\gradle.bat" 2^>nul') do (
  set GRADLE_BIN=%%G
  goto runGradle
)

:runGradle
"%GRADLE_BIN%" %*
