#!/bin/bash

# Script pour remplir la base de données via les API REST

# Vérifier que curl est installé
if ! command -v curl &> /dev/null; then
    echo "❌ curl n'est pas installé. Installez-le avec: sudo apt-get install curl"
    exit 1
fi

BASE_URL="${1:-http://localhost:8080/api}"
echo "🌱 Remplissage de la base de données via $BASE_URL"
echo ""

# Vérifier que l'API est accessible
if ! curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/auth/register" | grep -q "200\|400\|405"; then
    echo "⚠️  L'API ne semble pas accessible à $BASE_URL"
    echo "   Assure-toi que l'application est démarrée"
    read -p "Continuer quand même ? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi
echo ""

# Fonction pour faire des requêtes POST avec session
post_request() {
    local endpoint=$1
    local data=$2
    local cookie_file=$3
    local response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL$endpoint" \
        -H "Content-Type: application/json" \
        -b "$cookie_file" -c "$cookie_file" \
        -d "$data")
    local body=$(echo "$response" | head -n -1)
    local status=$(echo "$response" | tail -n 1)
    echo "$body"
    return $status
}

# Fonction pour faire des requêtes GET avec session
get_request() {
    local endpoint=$1
    local cookie_file=$2
    curl -s -X GET "$BASE_URL$endpoint" \
        -H "Content-Type: application/json" \
        -b "$cookie_file" -c "$cookie_file"
}

# Fonction pour extraire l'ID d'une réponse JSON
extract_id() {
    echo "$1" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*'
}

# Créer un fichier cookie temporaire
COOKIE_FILE=$(mktemp)
trap "rm -f $COOKIE_FILE" EXIT

echo "📝 Création / récupération des trainers..."
TRAINER_IDS=()

# Liste de vrais personnages de Pokémon
declare -a trainer_names=(
    "Ash Ketchum"
    "Misty"
    "Brock"
    "Gary Oak"
    "May"
    "Dawn"
    "Serena"
    "Clemont"
    "Lillie"
    "Red"
)

declare -a trainer_emails=(
    "ash@pokemon.com"
    "misty@pokemon.com"
    "brock@pokemon.com"
    "gary@pokemon.com"
    "may@pokemon.com"
    "dawn@pokemon.com"
    "serena@pokemon.com"
    "clemont@pokemon.com"
    "lillie@pokemon.com"
    "red@pokemon.com"
)


for i in {0..9}; do
    name="${trainer_names[$i]}"
    email="${trainer_emails[$i]}"
    password="password$((i+1))"

    # d'abord essayer de se connecter (au cas où le trainer existe déjà)
    login_resp=$(post_request "/auth/login" "{\"email\":\"$email\",\"password\":\"$password\"}" "$COOKIE_FILE")
    existing_id=$(echo "$login_resp" | grep -o '"trainerId":[0-9]*' | head -1 | grep -o '[0-9]*')

    if [ ! -z "$existing_id" ]; then
        TRAINER_IDS+=($existing_id)
        echo "  ✓ Trainer déjà existant utilisé: $name (ID: $existing_id)"
        continue
    fi

    # sinon, tenter l'enregistrement
    response=$(post_request "/auth/register" "{\"name\":\"$name\",\"email\":\"$email\",\"password\":\"$password\"}" "$COOKIE_FILE")
    trainer_id=$(extract_id "$response")

    if [ ! -z "$trainer_id" ]; then
        TRAINER_IDS+=($trainer_id)
        echo "  ✓ Trainer créé: $name (ID: $trainer_id)"
    else
        echo "  ✗ Erreur création trainer $name: $response"
    fi
done

echo ""
echo "🔐 Connexion avec le premier trainer pour les opérations protégées..."
# Se connecter avec Ash Ketchum (premier trainer)
login_response=$(post_request "/auth/login" "{\"email\":\"ash@pokemon.com\",\"password\":\"password1\"}" "$COOKIE_FILE")
if echo "$login_response" | grep -q "trainerId"; then
    echo "  ✓ Connexion réussie"
else
    echo "  ✗ Erreur de connexion: $login_response"
    exit 1
fi

echo ""
echo "🔴 Création des types..."
TYPE_IDS=()

# Récupérer les types existants
existing_types=$(get_request "/types" "$COOKIE_FILE")

types=("Fire" "Water" "Grass" "Electric" "Psychic" "Ice" "Dragon" "Dark" "Fairy" "Normal")
for type_name in "${types[@]}"; do
    # Vérifier si le type existe déjà en cherchant dans la réponse JSON
    # Format attendu: {"id":X,"name":"TypeName"} ou [{"id":X,"name":"TypeName"},...]
    existing_id=$(echo "$existing_types" | grep -o "\"name\":\"$type_name\"" | head -1)
    if [ ! -z "$existing_id" ]; then
        # Extraire l'ID qui précède le nom dans le JSON
        existing_id=$(echo "$existing_types" | sed -n "s/.*\"id\":\([0-9]*\).*\"name\":\"$type_name\".*/\1/p" | head -1)
        if [ -z "$existing_id" ]; then
            # Essayer l'autre ordre possible
            existing_id=$(echo "$existing_types" | sed -n "s/.*\"name\":\"$type_name\".*\"id\":\([0-9]*\).*/\1/p" | head -1)
        fi
    fi
    
    if [ ! -z "$existing_id" ]; then
        TYPE_IDS+=($existing_id)
        echo "  ✓ Type déjà existant utilisé: $type_name (ID: $existing_id)"
    else
        # Essayer de créer le type
        response=$(post_request "/types" "{\"name\":\"$type_name\"}" "$COOKIE_FILE")
        type_id=$(extract_id "$response")

        if [ ! -z "$type_id" ]; then
            TYPE_IDS+=($type_id)
            echo "  ✓ Type créé: $type_name (ID: $type_id)"
        else
            # Si l'erreur est due à une duplication, récupérer depuis la liste existante mise à jour
            existing_types=$(get_request "/types" "$COOKIE_FILE")
            existing_id=$(echo "$existing_types" | sed -n "s/.*\"id\":\([0-9]*\).*\"name\":\"$type_name\".*/\1/p" | head -1)
            if [ -z "$existing_id" ]; then
                existing_id=$(echo "$existing_types" | sed -n "s/.*\"name\":\"$type_name\".*\"id\":\([0-9]*\).*/\1/p" | head -1)
            fi
            if [ ! -z "$existing_id" ]; then
                TYPE_IDS+=($existing_id)
                echo "  ✓ Type déjà existant (récupéré): $type_name (ID: $existing_id)"
            else
                echo "  ⚠ Type $type_name existe peut-être déjà (ignoré)"
            fi
        fi
    fi
done

echo ""
echo "⚡ Création des pokemons..."
POKEMON_IDS=()

# Récupérer les pokémons existants
existing_pokemons=$(get_request "/pokemons" "$COOKIE_FILE")

# Liste de pokemons avec leurs stats
declare -A pokemons=(
    ["1"]="Bulbasaur:45:49:49:45"
    ["4"]="Charmander:39:52:43:65"
    ["7"]="Squirtle:44:48:65:43"
    ["25"]="Pikachu:35:55:30:90"
    ["39"]="Jigglypuff:115:45:20:20"
    ["52"]="Meowth:40:45:35:90"
    ["54"]="Psyduck:50:52:48:55"
    ["66"]="Machop:70:80:50:35"
    ["92"]="Gastly:30:35:30:80"
    ["129"]="Magikarp:20:10:55:80"
    ["133"]="Eevee:55:55:50:55"
    ["150"]="Mewtwo:106:110:90:130"
    ["151"]="Mew:100:100:100:100"
    ["155"]="Cyndaquil:39:52:43:65"
    ["158"]="Totodile:50:65:64:43"
)

for pokedex_num in "${!pokemons[@]}"; do
    IFS=':' read -r name hp attack defense speed <<< "${pokemons[$pokedex_num]}"

    # Vérifier si le pokémon existe déjà par son pokedexNumber
    existing_id=$(echo "$existing_pokemons" | grep -o "\"pokedexNumber\":$pokedex_num" | head -1)
    if [ ! -z "$existing_id" ]; then
        # Extraire l'ID qui correspond à ce pokedexNumber
        existing_id=$(echo "$existing_pokemons" | sed -n "s/.*\"id\":\([0-9]*\).*\"pokedexNumber\":$pokedex_num.*/\1/p" | head -1)
        if [ -z "$existing_id" ]; then
            # Essayer l'autre ordre possible
            existing_id=$(echo "$existing_pokemons" | sed -n "s/.*\"pokedexNumber\":$pokedex_num.*\"id\":\([0-9]*\).*/\1/p" | head -1)
        fi
    fi
    
    if [ ! -z "$existing_id" ]; then
        POKEMON_IDS+=($existing_id)
        echo "  ✓ Pokemon déjà existant utilisé: $name #$pokedex_num (ID: $existing_id)"
    else
        # Essayer de créer le pokémon
        response=$(post_request "/pokemons" "{\"pokedexNumber\":$pokedex_num,\"name\":\"$name\",\"hp\":$hp,\"attack\":$attack,\"defense\":$defense,\"speed\":$speed}" "$COOKIE_FILE")
        pokemon_id=$(extract_id "$response")

        if [ ! -z "$pokemon_id" ]; then
            POKEMON_IDS+=($pokemon_id)
            echo "  ✓ Pokemon créé: $name #$pokedex_num (ID: $pokemon_id)"
        else
            # Si l'erreur est due à une duplication, récupérer depuis la liste existante mise à jour
            existing_pokemons=$(get_request "/pokemons" "$COOKIE_FILE")
            existing_id=$(echo "$existing_pokemons" | sed -n "s/.*\"id\":\([0-9]*\).*\"pokedexNumber\":$pokedex_num.*/\1/p" | head -1)
            if [ -z "$existing_id" ]; then
                existing_id=$(echo "$existing_pokemons" | sed -n "s/.*\"pokedexNumber\":$pokedex_num.*\"id\":\([0-9]*\).*/\1/p" | head -1)
            fi
            if [ ! -z "$existing_id" ]; then
                POKEMON_IDS+=($existing_id)
                echo "  ✓ Pokemon déjà existant (récupéré): $name #$pokedex_num (ID: $existing_id)"
            else
                echo "  ⚠ Pokemon $name #$pokedex_num existe peut-être déjà (ignoré)"
            fi
        fi
    fi
done

echo ""
echo "🎣 Création des captures..."
CAPTURE_COUNT=0

# Vérifier qu'on a des pokémons avant de créer des captures
if [ ${#POKEMON_IDS[@]} -eq 0 ]; then
    echo "  ⚠ Aucun pokemon disponible, impossible de créer des captures"
else
    # Chaque trainer capture quelques pokemons aléatoirement
    for trainer_id in "${TRAINER_IDS[@]}"; do
        # Chaque trainer capture 2-4 pokemons
        num_captures=$((RANDOM % 3 + 2))

        for ((i=0; i<num_captures; i++)); do
            # Sélectionner un pokemon aléatoire
            random_index=$((RANDOM % ${#POKEMON_IDS[@]}))
            pokemon_id=${POKEMON_IDS[$random_index]}

            response=$(post_request "/caught-pokemons" "{\"trainerId\":$trainer_id,\"pokemonId\":$pokemon_id}" "$COOKIE_FILE")
            capture_id=$(extract_id "$response")

            if [ ! -z "$capture_id" ]; then
                CAPTURE_COUNT=$((CAPTURE_COUNT + 1))
            fi
        done
    done
    echo "  ✓ $CAPTURE_COUNT captures créées"
fi

echo ""
echo "✅ Remplissage terminé !"
echo ""
echo "📊 Résumé:"
echo "  - Trainers: ${#TRAINER_IDS[@]}"
echo "  - Types: ${#TYPE_IDS[@]}"
echo "  - Pokemons: ${#POKEMON_IDS[@]}"
echo "  - Captures: $CAPTURE_COUNT"
echo ""
echo "💡 Tu peux maintenant tester l'API avec:"
echo "   curl $BASE_URL/auth/login -X POST -H 'Content-Type: application/json' -d '{\"email\":\"ash@pokemon.com\",\"password\":\"password1\"}'"
