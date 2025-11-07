@echo off
setlocal enabledelayedexpansion

REM =================================================================
REM Script de demarrage rapide pour l'application Todo DDD (Windows)
REM =================================================================

echo 🚀 Demarrage de l'application Todo DDD...
echo.

REM Verification des prerequis
echo 📋 Verification des prerequis...

where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Java n'est pas installe. Veuillez installer Java 21.
    pause
    exit /b 1
)

where docker >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker n'est pas installe. Veuillez installer Docker.
    pause
    exit /b 1
)

where docker-compose >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker Compose n'est pas installe.
    pause
    exit /b 1
)

echo ✅ Tous les prerequis sont satisfaits

REM Mode de demarrage
set MODE=%1
if "%MODE%"=="" set MODE=docker

if "%MODE%"=="local" (
    echo.
    echo 🔧 Mode local - Infrastructure Docker + Application locale

    REM Demarrage de l'infrastructure uniquement
    echo 📋 Demarrage de l'infrastructure PostgreSQL, Redis, Monitoring...
    docker-compose up -d postgres redis prometheus grafana zipkin

    REM Attendre que PostgreSQL soit pret
    echo 📋 Attente de PostgreSQL...
    timeout /t 10 /nobreak >nul

    REM Construction de l'application
    echo 📋 Construction de l'application...
    call mvnw.cmd clean package -DskipTests -q

    REM Migration de la base de donnees
    echo 📋 Migration de la base de donnees...
    call mvnw.cmd flyway:migrate -q

    echo ✅ Infrastructure demarree. Vous pouvez maintenant lancer l'application avec :
    echo mvnw.cmd spring-boot:run -Dspring.profiles.active=dev

) else if "%MODE%"=="docker" (
    echo.
    echo 🐳 Mode Docker - Stack complete

    REM Construction et demarrage complet
    echo 📋 Construction et demarrage de tous les services...
    docker-compose up -d --build

    echo ✅ Stack complete demarree !

) else (
    echo ❌ Mode invalide. Utilisez 'local' ou 'docker'
    echo Usage: %0 [local^|docker]
    pause
    exit /b 1
)

echo.
echo 🌐 Services disponibles :
echo.
echo ┌─────────────────────────────────────────────────────────────┐
echo │                    📋 TODO DDD SERVICES                     │
echo ├─────────────────────────────────────────────────────────────┤
echo │ 🌍 API REST          │ http://localhost:8080               │
echo │ 📚 Swagger UI        │ http://localhost:8080/swagger-ui.html│
echo │ 🩺 Health Check      │ http://localhost:8080/actuator/health│
echo │ 📊 Prometheus        │ http://localhost:9090               │
echo │ 📈 Grafana           │ http://localhost:3000 (admin/admin123)│
echo │ 🔍 Zipkin            │ http://localhost:9411               │
echo └─────────────────────────────────────────────────────────────┘

echo.
echo 🧪 Test rapide de l'API :
echo.
echo # Creer un todo
echo curl -X POST http://localhost:8080/api/v1/todos ^
echo   -H "Content-Type: application/json" ^
echo   -H "X-User-ID: user123" ^
echo   -d "{\"title\": \"Mon premier todo\", \"description\": \"Test de l'API\", \"priority\": {\"level\": 2, \"name\": \"Normale\"}}"

echo.
echo # Lister les todos
echo curl -X GET http://localhost:8080/api/v1/todos -H "X-User-ID: user123"

echo.
echo ✅ Application Todo DDD prete ! 🎉

REM Si mode docker, attendre que l'application soit prete
if "%MODE%"=="docker" (
    echo.
    echo 📋 Verification de la sante de l'application...

    for /L %%i in (1,1,30) do (
        curl -f -s http://localhost:8080/actuator/health >nul 2>nul
        if !ERRORLEVEL! EQU 0 (
            echo ✅ Application demarree et operationnelle !
            goto :app_ready
        ) else (
            echo|set /p="."
            timeout /t 2 /nobreak >nul
        )
    )

    echo ⚠️ L'application met du temps a demarrer. Verifiez les logs avec:
    echo docker-compose logs app

    :app_ready
)

echo.
echo Pour arreter tous les services : docker-compose down
echo Pour voir les logs : docker-compose logs -f [service-name]
echo.

pause
