#!/bin/bash

# =================================================================
# Script de démarrage rapide pour l'application Todo DDD
# =================================================================

set -e

echo "🚀 Démarrage de l'application Todo DDD..."
echo ""

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction pour afficher avec des couleurs
print_step() {
    echo -e "${BLUE}📋 $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Vérification des prérequis
print_step "Vérification des prérequis..."

if ! command -v java &> /dev/null; then
    print_error "Java n'est pas installé. Veuillez installer Java 21."
    exit 1
fi

if ! command -v docker &> /dev/null; then
    print_error "Docker n'est pas installé. Veuillez installer Docker."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose n'est pas installé."
    exit 1
fi

print_success "Tous les prérequis sont satisfaits"

# Vérification de la version Java
JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "?\K[0-9]+')
if [ "$JAVA_VERSION" -lt 21 ]; then
    print_warning "Java version $JAVA_VERSION détectée. Java 21 recommandé."
fi

# Mode de démarrage
MODE=${1:-"docker"}

if [ "$MODE" = "local" ]; then
    echo ""
    print_step "🔧 Mode local - Infrastructure Docker + Application locale"

    # Démarrage de l'infrastructure uniquement
    print_step "Démarrage de l'infrastructure (PostgreSQL, Redis, Monitoring)..."
    docker-compose up -d postgres redis prometheus grafana zipkin

    # Attendre que PostgreSQL soit prêt
    print_step "Attente de PostgreSQL..."
    sleep 10

    # Construction de l'application
    print_step "Construction de l'application..."
    ./mvnw clean package -DskipTests -q

    # Migration de la base de données
    print_step "Migration de la base de données..."
    ./mvnw flyway:migrate -q

    print_success "Infrastructure démarrée. Vous pouvez maintenant lancer l'application avec :"
    echo -e "${YELLOW}./mvnw spring-boot:run -Dspring.profiles.active=dev${NC}"

elif [ "$MODE" = "docker" ]; then
    echo ""
    print_step "🐳 Mode Docker - Stack complète"

    # Construction et démarrage complet
    print_step "Construction et démarrage de tous les services..."
    docker-compose up -d --build

    print_success "Stack complète démarrée !"

else
    print_error "Mode invalide. Utilisez 'local' ou 'docker'"
    echo "Usage: $0 [local|docker]"
    exit 1
fi

echo ""
print_step "🌐 Services disponibles :"
echo ""
echo "┌─────────────────────────────────────────────────────────────┐"
echo "│                    📋 TODO DDD SERVICES                     │"
echo "├─────────────────────────────────────────────────────────────┤"
echo "│ 🌍 API REST          │ http://localhost:8080               │"
echo "│ 📚 Swagger UI        │ http://localhost:8080/swagger-ui.html│"
echo "│ 🩺 Health Check      │ http://localhost:8080/actuator/health│"
echo "│ 📊 Prometheus        │ http://localhost:9090               │"
echo "│ 📈 Grafana           │ http://localhost:3000 (admin/admin123)│"
echo "│ 🔍 Zipkin            │ http://localhost:9411               │"
echo "└─────────────────────────────────────────────────────────────┘"

echo ""
print_step "🧪 Test rapide de l'API :"
echo ""
echo "# Créer un todo"
echo "curl -X POST http://localhost:8080/api/v1/todos \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -H \"X-User-ID: user123\" \\"
echo "  -d '{"
echo "    \"title\": \"Mon premier todo\","
echo "    \"description\": \"Test de l\\'API\","
echo "    \"priority\": {"
echo "      \"level\": 2,"
echo "      \"name\": \"Normale\""
echo "    }"
echo "  }'"

echo ""
echo "# Lister les todos"
echo "curl -X GET http://localhost:8080/api/v1/todos -H \"X-User-ID: user123\""

echo ""
print_success "Application Todo DDD prête ! 🎉"

# Si mode docker, attendre que l'application soit prête
if [ "$MODE" = "docker" ]; then
    echo ""
    print_step "Vérification de la santé de l'application..."

    for i in {1..30}; do
        if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
            print_success "Application démarrée et opérationnelle !"
            break
        else
            echo -n "."
            sleep 2
        fi

        if [ $i -eq 30 ]; then
            print_warning "L'application met du temps à démarrer. Vérifiez les logs avec:"
            echo "docker-compose logs app"
        fi
    done
fi

echo ""
echo "Pour arrêter tous les services : docker-compose down"
echo "Pour voir les logs : docker-compose logs -f [service-name]"
echo ""
