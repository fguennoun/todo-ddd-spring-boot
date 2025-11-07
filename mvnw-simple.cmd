@echo off
REM =================================================================
REM Simple Maven Wrapper for Windows - Todo DDD Project
REM =================================================================

setlocal

set JAVA_HOME=%JAVA_HOME%
if "%JAVA_HOME%" == "" (
    echo ERREUR: JAVA_HOME n'est pas defini
    echo Veuillez installer Java 21 et definir JAVA_HOME
    pause
    exit /b 1
)

set MAVEN_VERSION=3.9.6
set MAVEN_HOME=%~dp0.m2\apache-maven-%MAVEN_VERSION%
set MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd

REM Verifier si Maven est deja telecharge
if not exist "%MAVEN_CMD%" (
    echo Telechargement de Maven %MAVEN_VERSION%...

    REM Creer le repertoire .m2
    mkdir "%~dp0.m2" 2>nul

    REM Telecharger Maven avec PowerShell
    powershell -Command "& {
        $ProgressPreference = 'SilentlyContinue'
        $url = 'https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip'
        $output = '%~dp0.m2\apache-maven-%MAVEN_VERSION%-bin.zip'
        try {
            Invoke-WebRequest -Uri $url -OutFile $output
            Write-Host 'Telechargement termine.'
        } catch {
            Write-Host 'Erreur de telechargement:' $_.Exception.Message
            exit 1
        }
    }"

    if errorlevel 1 (
        echo Erreur lors du telechargement de Maven
        pause
        exit /b 1
    )

    echo Extraction de Maven...
    powershell -Command "Expand-Archive -Path '%~dp0.m2\apache-maven-%MAVEN_VERSION%-bin.zip' -DestinationPath '%~dp0.m2' -Force"

    del "%~dp0.m2\apache-maven-%MAVEN_VERSION%-bin.zip"
    echo Maven installe avec succes dans %MAVEN_HOME%
)

REM Executer Maven avec les arguments passes
echo Execution: %MAVEN_CMD% %*
"%MAVEN_CMD%" %*
