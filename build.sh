#!/usr/bin/env bash
# Build script for the TwitchRecover fork (no Maven/Gradle needed).
set -e
cd "$(dirname "$0")"

# Classpath separator: ';' on Windows (Git Bash/MSYS/Cygwin), ':' elsewhere.
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) SEP=';' ;;
  *) SEP=':' ;;
esac
CP=$(ls lib/*.jar | tr '\n' "$SEP")

rm -rf bin
mkdir -p bin
find src -name '*.java' > sources.txt
javac -encoding UTF-8 --release 8 -cp "$CP" -d bin @sources.txt
rm -f sources.txt

# Build a runnable jar (depends on the jars in lib/ via Class-Path).
{
  echo "Manifest-Version: 1.0"
  echo "Main-Class: TwitchRecover.GUI.App"
  printf "Class-Path:"
  for j in lib/*.jar; do printf " %s" "$j"; done
  echo
} > bin/MANIFEST.MF
# Bundle the app icon inside the jar so the window uses it.
cp logo.png bin/ 2>/dev/null || true
( cd bin && jar cfm ../TwitchRecover.jar MANIFEST.MF $(find . -name '*.class' | sed 's|^\./||') $([ -f logo.png ] && echo logo.png) )

echo "Built TwitchRecover.jar — run with: java -jar TwitchRecover.jar"
