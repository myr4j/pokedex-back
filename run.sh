#!/bin/bash

# Script unifié pour lancer les composants du Pokédex Backend
# Usage: ./run.sh <docker|back|jms>

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Répertoire racine du projet
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Options Maven pour GlassFish
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED"

# Fonction d'aide
show_help() {
    echo ""
    echo -e "${BLUE}╔════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║       🎮 Pokédex Backend Runner 🎮             ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${YELLOW}Usage:${NC} ./run.sh <command>"
    echo ""
    echo -e "${YELLOW}Commands:${NC}"
    echo -e "  ${GREEN}docker${NC}    Lance les containers Docker (PostgreSQL + ActiveMQ Artemis)"
    echo -e "  ${GREEN}back${NC}      Lance le serveur backend REST (port 8080)"
    echo -e "  ${GREEN}jms${NC}       Lance le consumer JMS (port 8081)"
    echo ""
    echo -e "${YELLOW}Examples:${NC}"
    echo "  ./run.sh docker    # Terminal 1 - Infrastructure"
    echo "  ./run.sh back      # Terminal 2 - API REST"
    echo "  ./run.sh jms       # Terminal 3 - JMS Consumer"
    echo ""
    echo -e "${YELLOW}Order:${NC} Lancez dans l'ordre: docker → back → jms"
    echo ""
}

# Fonction pour lancer Docker
run_docker() {
    echo -e "${BLUE}🐳 Lancement de Docker (PostgreSQL + ActiveMQ Artemis)...${NC}"
    echo ""
    cd "$PROJECT_ROOT"
    docker-compose up
}

# Fonction pour lancer le backend
run_backend() {
    echo -e "${BLUE}🚀 Lancement du Backend REST (port 8080)...${NC}"
    echo ""
    cd "$PROJECT_ROOT"
    mvn embedded-glassfish:run
}

# Fonction pour lancer le JMS consumer
run_jms() {
    echo -e "${BLUE}📡 Lancement du JMS Consumer (port 8081)...${NC}"
    echo ""
    cd "$PROJECT_ROOT/pokedex-jms-consumer"
    mvn embedded-glassfish:run
}

# Vérification de l'argument
if [ $# -eq 0 ]; then
    show_help
    exit 1
fi

# Traitement de la commande
case "$1" in
    docker)
        run_docker
        ;;
    back)
        run_backend
        ;;
    jms)
        run_jms
        ;;
    -h|--help|help)
        show_help
        ;;
    *)
        echo -e "${RED}❌ Commande invalide: $1${NC}"
        show_help
        exit 1
        ;;
esac
