@echo off
REM Build script for the TwitchRecover fork (Windows, no Maven/Gradle needed).
cd /d "%~dp0"

setlocal enabledelayedexpansion
set CP=
for %%j in (lib\*.jar) do set CP=!CP!%%j;

if exist bin rmdir /s /q bin
mkdir bin

dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 --release 8 -cp "%CP%" -d bin @sources.txt
del sources.txt

REM Bundle the app icon inside the jar so the window uses it.
if exist logo.png copy /y logo.png bin\ >nul

> MANIFEST.MF echo Manifest-Version: 1.0
>> MANIFEST.MF echo Main-Class: TwitchRecover.GUI.App
set MCP=Class-Path:
for %%j in (lib\*.jar) do set MCP=!MCP! %%j
>> MANIFEST.MF echo !MCP!

REM "-C bin ." adds every file under bin with relative paths (classes + logo).
jar cfm TwitchRecover.jar MANIFEST.MF -C bin .
del MANIFEST.MF

echo Built TwitchRecover.jar - run with: java -jar TwitchRecover.jar
endlocal
