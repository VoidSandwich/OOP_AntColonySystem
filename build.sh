#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
find src -name '*.java' -print > .sources.list
javac --release 17 -encoding UTF-8 -d out @.sources.list
rm .sources.list
jar --create --file AntColony.jar --main-class antcolony.Main -C out .
echo "Сборка завершена: AntColony.jar"
