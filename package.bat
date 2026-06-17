@echo off
REM ============================================================
REM  Build a Windows INSTALLER (.exe) for Twitch Recover using
REM  jpackage (bundled with the JDK 17+). The installer bundles
REM  the Java runtime, ffmpeg and VLC, and creates Start-menu +
REM  desktop shortcuts - so end users just run the installer.
REM
REM  Requirements:
REM    - A JDK 17+ on PATH (provides jpackage).
REM    - WiX Toolset 3.x on PATH (https://wixtoolset.org/) for
REM      building the .exe installer. Without it, jpackage will
REM      tell you WiX is missing.
REM ============================================================
setlocal enabledelayedexpansion
cd /d "%~dp0"

where jpackage >nul 2>&1
if errorlevel 1 (
  echo ERROR: jpackage not found. Install a JDK 17+ ^(https://adoptium.net^) and try again.
  pause
  exit /b 1
)

echo [1/4] Building the jar...
call build.bat
if not exist TwitchRecover.jar ( echo Build failed. & pause & exit /b 1 )

echo [2/4] Ensuring the built-in player (VLC) is bundled...
if exist "vlc\libvlc.dll" goto :havevlc
echo     Downloading a portable VLC ^(~40 MB, once^)...
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; $ErrorActionPreference='Stop'; try { $u='https://download.videolan.org/pub/videolan/vlc/3.0.21/win64/vlc-3.0.21-win64.zip'; Invoke-WebRequest -Uri $u -OutFile 'vlc.zip'; if (Test-Path 'vlc_tmp') { Remove-Item 'vlc_tmp' -Recurse -Force }; Expand-Archive -Path 'vlc.zip' -DestinationPath 'vlc_tmp' -Force; $d=Get-ChildItem 'vlc_tmp' -Directory | Select-Object -First 1; if (Test-Path 'vlc') { Remove-Item 'vlc' -Recurse -Force }; Move-Item $d.FullName 'vlc'; Remove-Item 'vlc_tmp' -Recurse -Force; Remove-Item 'vlc.zip' -Force; exit 0 } catch { Write-Host ('VLC download failed: ' + $_.Exception.Message); exit 0 }"
:havevlc

echo [3/4] Staging files...
if exist build_stage rmdir /s /q build_stage
mkdir build_stage
copy /y TwitchRecover.jar build_stage\ >nul
xcopy /e /i /y lib build_stage\lib >nul
if exist "src\TwitchRecover.Core\Libraries\ffmpeg.exe" copy /y "src\TwitchRecover.Core\Libraries\ffmpeg.exe" build_stage\ >nul
if exist vlc xcopy /e /i /y vlc build_stage\vlc >nul

if exist dist rmdir /s /q dist

REM Choose installer (needs WiX) or portable app-image (no WiX).
where candle.exe >nul 2>&1
if errorlevel 1 goto :portable

echo [4/4] Building the INSTALLER with jpackage ^(this can take a few minutes^)...
jpackage ^
  --type exe ^
  --name "Twitch Recover" ^
  --app-version 2.0.1 ^
  --vendor "Alevaldo35" ^
  --description "View, recover and download Twitch videos" ^
  --input build_stage ^
  --main-jar TwitchRecover.jar ^
  --main-class TwitchRecover.GUI.App ^
  --icon logo.ico ^
  --dest dist ^
  --win-menu ^
  --win-menu-group "Twitch Recover" ^
  --win-shortcut ^
  --win-shortcut-prompt ^
  --win-dir-chooser ^
  --win-per-user-install
echo.
if exist "dist\Twitch Recover-2.0.1.exe" (
  echo Done. INSTALLER created at: dist\Twitch Recover-2.0.1.exe
  echo Users just run it: it installs the app and adds Start-menu + desktop shortcuts.
) else (
  echo jpackage did not produce the installer - check the messages above.
)
goto :end

:portable
echo [4/4] WiX not found - building a PORTABLE .exe instead ^(no installer/shortcuts^)...
echo      To build the full installer with shortcuts, install WiX Toolset 3.x
echo      from https://wixtoolset.org/ then run package.bat again.
jpackage ^
  --type app-image ^
  --name "TwitchRecover" ^
  --app-version 2.0.1 ^
  --vendor "Alevaldo35" ^
  --input build_stage ^
  --main-jar TwitchRecover.jar ^
  --main-class TwitchRecover.GUI.App ^
  --icon logo.ico ^
  --dest dist
echo.
if exist "dist\TwitchRecover\TwitchRecover.exe" (
  echo Done. PORTABLE app at: dist\TwitchRecover\TwitchRecover.exe
  echo Zip the "dist\TwitchRecover" folder and share it - double-click the .exe to run.
) else (
  echo jpackage did not produce the portable app - check the messages above.
)

:end
rmdir /s /q build_stage
pause
endlocal
