@echo off
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
if exist sources.list del sources.list
for /r src %%f in (*.java) do echo "%%f">>sources.list
javac --release 17 -encoding UTF-8 -d out @sources.list
if errorlevel 1 (
  if exist sources.list del sources.list
  echo Ошибка компиляции. Проверьте, что установлен JDK 17 или новее.
  exit /b 1
)
if exist sources.list del sources.list
jar --create --file AntColony.jar --main-class antcolony.Main -C out .
if errorlevel 1 exit /b 1
echo Сборка завершена: AntColony.jar
