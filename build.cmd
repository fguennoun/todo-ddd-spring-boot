@echo off
setlocal enabledelayedexpansion

echo =================================================================
echo        INSTALLATION ET BUILD - TODO DDD SPRING BOOT
echo =================================================================
echo.

REM Verification des prerequis
echo 📋 Verification des prerequis...

REM Verification Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ JAVA N'EST PAS INSTALLE
    echo.
    echo Pour faire fonctionner ce projet, vous devez installer :
    echo 1. Java 21 JDK
    echo    - Telechargez depuis : https://adoptium.net/telechargement/
    echo    - Ou utilisez : winget install Eclipse.Telechargement.JDK.21
    echo.
    echo 2. Maven (optionnel - ce script peut le telecharger automatiquement)
    echo    - Telechargez depuis : https://maven.apache.org/download.cgi
    echo    - Ou utilisez : winget install Apache.Maven
    echo.
    echo Apres installation, relancez ce script.
    echo.
    pause
    exit /b 1
)

echo ✅ Java trouve

REM Verification Maven
mvn --version >nul 2>&1
set MAVEN_INSTALLED=%ERRORLEVEL%

if %MAVEN_INSTALLED% EQU 0 (
    echo ✅ Maven trouve - utilisation de la version systeme
    set USE_SYSTEM_MAVEN=true
) else (
    echo ⚠️  Maven non trouve - utilisation du wrapper integre
    set USE_SYSTEM_MAVEN=false
)

echo.
echo 🏗️  Structure du projet Todo DDD :
echo.
echo src/main/java/com/example/todo/
echo ├── domain/          # Logique metier (Aggregates, Value Objects, Events)
echo ├─��� application/     # Use Cases et orchestration
echo ├── infrastructure/  # Persistence, Config, REST Controllers
echo └── TodoApplication.java
echo.

REM Choix de l'action
echo Que souhaitez-vous faire ?
echo.
echo 1. Compiler le projet (mvn compile)
echo 2. Executer les tests (mvn test)
echo 3. Build complet (mvn clean install)
echo 4. Lancer l'application (mvn spring-boot:run)
echo 5. Nettoyer (mvn clean)
echo 6. Afficher l'aide Maven
echo.
set /p choice="Entrez votre choix (1-6) : "

if "%choice%"=="1" set MAVEN_GOAL=compile
if "%choice%"=="2" set MAVEN_GOAL=test
if "%choice%"=="3" set MAVEN_GOAL=clean install
if "%choice%"=="4" set MAVEN_GOAL=spring-boot:run -Dspring.profiles.active=dev
if "%choice%"=="5" set MAVEN_GOAL=clean
if "%choice%"=="6" set MAVEN_GOAL=help:describe -Dcmd

if "%MAVEN_GOAL%"=="" (
    echo Choix invalide. Utilisation de 'compile' par defaut.
    set MAVEN_GOAL=compile
)

echo.
echo 🚀 Execution : %MAVEN_GOAL%
echo.

if "%USE_SYSTEM_MAVEN%"=="true" (
    echo Utilisation de Maven systeme...
    mvn %MAVEN_GOAL%
) else (
    echo Utilisation du wrapper Maven integre...

    REM Si le wrapper n'existe pas encore, on utilise une approche simplifiee
    if not exist ".m2\apache-maven-3.9.6\bin\mvn.cmd" (
        echo.
        echo 📥 Telechargement de Maven 3.9.6...
        echo Cela peut prendre quelques minutes la premiere fois.

        mkdir .m2 2>nul

        powershell -Command "& {
            $ProgressPreference = 'SilentlyContinue'
            Write-Host 'Telechargement de Maven...'
            try {
                Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '.m2\maven.zip'
                Write-Host 'Extraction...'
                Expand-Archive -Path '.m2\maven.zip' -DestinationPath '.m2' -Force
                Remove-Item '.m2\maven.zip'
                Write-Host '✅ Maven installe localement'
            } catch {
                Write-Host '❌ Erreur:' $_.Exception.Message
                exit 1
            }
        }"

        if !ERRORLEVEL! NEQ 0 (
            echo Erreur lors de l'installation de Maven
            pause
            exit /b 1
        )
    )

    .m2\apache-maven-3.9.6\bin\mvn.cmd %MAVEN_GOAL%
)

set BUILD_RESULT=%ERRORLEVEL%

echo.
if %BUILD_RESULT% EQU 0 (
    echo ✅ BUILD REUSSI !
    echo.
    if "%choice%"=="4" (
        echo 🌐 Application demarree !
        echo.
        echo Services disponibles :
        echo - API REST      : http://localhost:8080
        echo - Swagger UI    : http://localhost:8080/swagger-ui.html
        echo - Health Check  : http://localhost:8080/actuator/health
        echo.
        echo Appuyez sur Ctrl+C pour arreter l'application
    ) else if "%choice%"=="3" (
        echo 🚀 Projet build avec succes !
        echo.
        echo Pour demarrer l'application :
        echo   %~nx0 puis choisissez l'option 4
        echo.
        echo Ou directement :
        echo   java -jar target\todo-ddd-springboot-app-1.0.0-SNAPSHOT.jar
    )
) else (
    echo ❌ BUILD ECHOUE
    echo.
    echo Verifiez les erreurs ci-dessus et assurez-vous que :
    echo - Java 21 est bien installe
    echo - Le code source est correct
    echo - Les dependances peuvent etre telechargees
)

echo.
pause
