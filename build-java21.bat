@echo off
REM Script pour compiler avec Java 21
REM Modifiez le chemin JAVA_HOME ci-dessous pour pointer vers votre installation Java 21

REM Exemple : set JAVA_HOME=C:\Program Files\Java\jdk-21
REM Chercher automatiquement Java 21
for /d %%i in ("C:\Program Files\Java\jdk-21*") do set JAVA_HOME=%%i
if not defined JAVA_HOME (
    for /d %%i in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do set JAVA_HOME=%%i
)
if not defined JAVA_HOME (
    for /d %%i in ("%ProgramFiles%\Java\jdk-21*") do set JAVA_HOME=%%i
)

if not defined JAVA_HOME (
    echo ERREUR: Java 21 n'a pas ete trouve automatiquement.
    echo Veuillez modifier ce script et definir JAVA_HOME manuellement.
    echo Exemple: set JAVA_HOME=C:\Program Files\Java\jdk-21.0.1
    pause
    exit /b 1
)

echo Utilisation de Java: %JAVA_HOME%
"%JAVA_HOME%\bin\java" -version

echo.
echo Compilation avec Maven...
call mvn clean install %*

