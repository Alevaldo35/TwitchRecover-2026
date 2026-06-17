@echo off
REM ============================================================
REM  Twitch Recover - one-click launcher (patched fork)
REM  Nothing to install: if Java is missing, a portable Java
REM  runtime is downloaded automatically on the first run.
REM ============================================================
setlocal enabledelayedexpansion
cd /d "%~dp0"
title Twitch Recover

set "JAVA_EXE="

REM --- 1) Java already installed on the system? ---
where java >nul 2>&1
if %ERRORLEVEL%==0 set "JAVA_EXE=java"

REM --- 2) Portable Java previously downloaded by this launcher? ---
if not defined JAVA_EXE if exist "jre\bin\java.exe" set "JAVA_EXE=jre\bin\java.exe"

REM --- 3) Otherwise download a portable Java runtime (one time only) ---
if not defined JAVA_EXE (
  echo.
  echo Java was not found on this PC.
  echo Downloading a portable Java runtime ^(about 45 MB^). This happens only once...
  echo.
  powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; $ErrorActionPreference='Stop'; try { $u='https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jre/hotspot/normal/eclipse'; Invoke-WebRequest -Uri $u -OutFile 'jre.zip'; if (Test-Path 'jre_tmp') { Remove-Item 'jre_tmp' -Recurse -Force }; Expand-Archive -Path 'jre.zip' -DestinationPath 'jre_tmp' -Force; $d=Get-ChildItem 'jre_tmp' -Directory | Select-Object -First 1; if (Test-Path 'jre') { Remove-Item 'jre' -Recurse -Force }; Move-Item $d.FullName 'jre'; Remove-Item 'jre_tmp' -Recurse -Force; Remove-Item 'jre.zip' -Force; exit 0 } catch { Write-Host $_.Exception.Message; exit 1 }"
  if exist "jre\bin\java.exe" set "JAVA_EXE=jre\bin\java.exe"
)

if not defined JAVA_EXE (
  echo.
  echo ERROR: Could not find or download Java automatically.
  echo Please install Java manually from https://adoptium.net then run start.bat again.
  echo.
  pause
  exit /b 1
)

if not exist "TwitchRecover.jar" (
  echo.
  echo ERROR: TwitchRecover.jar is missing. Re-download the project ^(it ships prebuilt^).
  echo.
  pause
  exit /b 1
)

REM --- Internal video player: ensure VLC is available (system install or bundled) ---
if exist "vlc\libvlc.dll" goto :havevlc
if exist "%ProgramFiles%\VideoLAN\VLC\libvlc.dll" goto :havevlc
if exist "%ProgramFiles(x86)%\VideoLAN\VLC\libvlc.dll" goto :havevlc
echo.
echo Setting up the built-in video player ^(downloading a portable VLC, ~40 MB, once^)...
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; $ErrorActionPreference='Stop'; try { $u='https://download.videolan.org/pub/videolan/vlc/3.0.21/win64/vlc-3.0.21-win64.zip'; Invoke-WebRequest -Uri $u -OutFile 'vlc.zip'; if (Test-Path 'vlc_tmp') { Remove-Item 'vlc_tmp' -Recurse -Force }; Expand-Archive -Path 'vlc.zip' -DestinationPath 'vlc_tmp' -Force; $d=Get-ChildItem 'vlc_tmp' -Directory | Select-Object -First 1; if (Test-Path 'vlc') { Remove-Item 'vlc' -Recurse -Force }; Move-Item $d.FullName 'vlc'; Remove-Item 'vlc_tmp' -Recurse -Force; Remove-Item 'vlc.zip' -Force; exit 0 } catch { Write-Host ('Could not download VLC: ' + $_.Exception.Message); exit 0 }"
:havevlc

REM --- 4) Launch the program ---
echo Starting Twitch Recover...
echo.
"%JAVA_EXE%" -jar TwitchRecover.jar
echo.
pause
endlocal
