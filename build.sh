#!/bin/bash

# =================================================================
# Script de Build pour Todo DDD Spring Boot (Linux/Mac)
# =================================================================

set -e

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}================================================================="
echo -e "       INSTALLATION ET BUILD - TODO DDD SPRING BOOT"
echo -e "=================================================================${NC}"
echo

# Fonctions d'affichage
print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️ $1${NC}"; }
print_info() { echo -e "${BLUE}📋 $1${NC}"; }

# Vérification Java
print_info "Vérification des prérequis..."
if ! command -v java &> /dev/null; then
    print_error "JAVA N'EST PAS INSTALLÉ"
    echo
    echo "Pour faire fonctionner ce projet, vous devez installer :"
    echo "1. Java 21 JDK"
    echo "   - Ubuntu/Debian: sudo apt install openjdk-21-jdk"
    echo "   - macOS: brew install openjdk@21"
    echo "   - Ou téléchargez depuis: https://adoptium.net/"
    echo
    echo "2. Maven (optionnel - ce script peut le télécharger)"
    echo "   - Ubuntu/Debian: sudo apt install maven"
    echo "   - macOS: brew install maven"
    echo
    exit 1
fi

print_success "Java trouvé: $(java -version 2>&1 | head -n1)"

# Vérification Maven
if command -v mvn &> /dev/null; then
    print_success "Maven trouvé - utilisation de la version système"
    USE_SYSTEM_MAVEN=true
else
    print_warning "Maven non trouvé - utilisation du wrapper intégré"
    USE_SYSTEM_MAVEN=false
fi

echo
print_info "🏗️ Structure du projet Todo DDD :"
echo
echo "src/main/java/com/example/todo/"
echo "├── domain/          # Logique métier (Aggregates, Value Objects, Events)"
echo "├── application/     # Use Cases et orchestration"
echo "├── infrastructure/  # Persistence, Config, REST Controllers"
echo "└── TodoApplication.java"
echo

# Menu des actions
echo "Que souhaitez-vous faire ?"
echo
echo "1. Compiler le projet (mvn compile)"
echo "2. Exécuter les tests (mvn test)"
echo "3. Build complet (mvn clean install)"
echo "4. Lancer l'application (mvn spring-boot:run)"
echo "5. Nettoyer (mvn clean)"
echo "6. Afficher l'aide Maven"
echo

read -p "Entrez votre choix (1-6) [3]: " choice
choice=${choice:-3}

case $choice in
    1) MAVEN_GOAL="compile" ;;
    2) MAVEN_GOAL="test" ;;
    3) MAVEN_GOAL="clean install" ;;
    4) MAVEN_GOAL="spring-boot:run -Dspring.profiles.active=dev" ;;
    5) MAVEN_GOAL="clean" ;;
    6) MAVEN_GOAL="help:describe -Dcmd" ;;
    *) MAVEN_GOAL="compile" ; print_warning "Choix invalide, utilisation de 'compile'" ;;
esac

echo
print_info "🚀 Exécution : $MAVEN_GOAL"
echo

if [ "$USE_SYSTEM_MAVEN" = true ]; then
    print_info "Utilisation de Maven système..."
    mvn $MAVEN_GOAL
else
    print_info "Installation de Maven local..."

    MAVEN_VERSION="3.9.6"
    MAVEN_HOME=".m2/apache-maven-$MAVEN_VERSION"

    if [ ! -f "$MAVEN_HOME/bin/mvn" ]; then
        print_info "📥 Téléchargement de Maven $MAVEN_VERSION..."
        mkdir -p .m2

        if command -v wget &> /dev/null; then
            wget -q "https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz" -O .m2/maven.tar.gz
        elif command -v curl &> /dev/null; then
            curl -s -L "https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz" -o .m2/maven.tar.gz
        else
            print_error "wget ou curl requis pour télécharger Maven"
            exit 1
        fi

        print_info "Extraction de Maven..."
        tar -xzf .m2/maven.tar.gz -C .m2/
        rm .m2/maven.tar.gz
        chmod +x "$MAVEN_HOME/bin/mvn"
        print_success "Maven installé localement"
    fi

    "./$MAVEN_HOME/bin/mvn" $MAVEN_GOAL
fi

BUILD_RESULT=$?

echo
if [ $BUILD_RESULT -eq 0 ]; then
    print_success "BUILD RÉUSSI !"
    echo
    case $choice in
        4)
            echo "🌐 Application démarrée !"
            echo
            echo "Services disponibles :"
            echo "- API REST      : http://localhost:8080"
            echo "- Swagger UI    : http://localhost:8080/swagger-ui.html"
            echo "- Health Check  : http://localhost:8080/actuator/health"
            echo
            echo "Appuyez sur Ctrl+C pour arrêter l'application"
            ;;
        3)
            print_success "🚀 Projet buildé avec succès !"
            echo
            echo "Pour démarrer l'application :"
            echo "  ./build.sh puis choisissez l'option 4"
            echo
            echo "Ou directement :"
            echo "  java -jar target/todo-ddd-springboot-app-1.0.0-SNAPSHOT.jar"
            ;;
    esac
else
    print_error "BUILD ÉCHOUÉ"
    echo
    echo "Vérifiez les erreurs ci-dessus et assurez-vous que :"
    echo "- Java 21 est bien installé"
    echo "- Le code source est correct"
    echo "- Les dépendances peuvent être téléchargées"
fi
