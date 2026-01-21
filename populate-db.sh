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
    local temp_file=$(mktemp)
    local http_code=$(curl -s -w "%{http_code}" -o "$temp_file" -X POST "$BASE_URL$endpoint" \
        -H "Content-Type: application/json" \
        -b "$cookie_file" -c "$cookie_file" \
        -d "$data")
    local body=$(cat "$temp_file")
    rm -f "$temp_file"
    echo "$body"
    echo "$http_code"
}

# Fonction pour extraire l'ID d'une réponse JSON
extract_id() {
    echo "$1" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*'
}

# Fonction pour extraire trainerId d'une réponse JSON
extract_trainer_id() {
    echo "$1" | grep -o '"trainerId":[0-9]*' | head -1 | grep -o '[0-9]*'
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
    http_status=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | head -n -1)
    trainer_id=$(extract_trainer_id "$body")
    
    if [ -z "$trainer_id" ]; then
        trainer_id=$(extract_id "$body")
    fi

    if [ ! -z "$trainer_id" ] && [ "$http_status" = "201" ]; then
        TRAINER_IDS+=($trainer_id)
        echo "  ✓ Trainer créé: $name (ID: $trainer_id)"
    elif [ ! -z "$trainer_id" ]; then
        TRAINER_IDS+=($trainer_id)
        echo "  ✓ Trainer trouvé: $name (ID: $trainer_id)"
    else
        echo "  ⚠️  Trainer $name non créé (peut-être déjà existant ou erreur)"
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

types=("Fire" "Water" "Grass" "Electric" "Psychic" "Ice" "Dragon" "Dark" "Fairy" "Normal")
for type_name in "${types[@]}"; do
    response=$(post_request "/types" "{\"name\":\"$type_name\"}" "$COOKIE_FILE")
    http_status=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | head -n -1)
    type_id=$(extract_id "$body")

    if [ ! -z "$type_id" ] && [ "$http_status" = "201" ]; then
        TYPE_IDS+=($type_id)
        echo "  ✓ Type créé: $type_name (ID: $type_id)"
    elif [ "$http_status" = "500" ] && echo "$body" | grep -q "duplicate key\|already exists"; then
        # Type existe déjà, essayer de le récupérer via GET
        existing_type=$(curl -s "$BASE_URL/types" -b "$COOKIE_FILE" | grep -o "\"name\":\"$type_name\".*\"id\":[0-9]*" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
        if [ ! -z "$existing_type" ]; then
            TYPE_IDS+=($existing_type)
            echo "  ✓ Type déjà existant: $type_name (ID: $existing_type)"
        else
            echo "  ⚠️  Type $type_name existe mais ID non récupéré"
        fi
    elif [ ! -z "$type_id" ]; then
        TYPE_IDS+=($type_id)
        echo "  ✓ Type trouvé: $type_name (ID: $type_id)"
    else
        echo "  ⚠️  Type $type_name non créé"
    fi
done

echo ""
echo "⚡ Création des pokemons..."
POKEMON_IDS=()

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

    response=$(post_request "/pokemons" "{\"pokedexNumber\":$pokedex_num,\"name\":\"$name\",\"hp\":$hp,\"attack\":$attack,\"defense\":$defense,\"speed\":$speed}" "$COOKIE_FILE")
    http_status=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | head -n -1)
    pokemon_id=$(extract_id "$body")

    if [ ! -z "$pokemon_id" ] && [ "$http_status" = "201" ]; then
        POKEMON_IDS+=($pokemon_id)
        echo "  ✓ Pokemon créé: $name #$pokedex_num (ID: $pokemon_id)"
    elif [ "$http_status" = "500" ] && echo "$body" | grep -q "duplicate key\|already exists"; then
        # Pokemon existe déjà, essayer de le récupérer via GET
        existing_pokemon=$(curl -s "$BASE_URL/pokemons" -b "$COOKIE_FILE" | grep -o "\"pokedexNumber\":$pokedex_num.*\"id\":[0-9]*" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
        if [ ! -z "$existing_pokemon" ]; then
            POKEMON_IDS+=($existing_pokemon)
            echo "  ✓ Pokemon déjà existant: $name #$pokedex_num (ID: $existing_pokemon)"
        else
            echo "  ⚠️  Pokemon $name existe mais ID non récupéré"
        fi
    elif [ ! -z "$pokemon_id" ]; then
        POKEMON_IDS+=($pokemon_id)
        echo "  ✓ Pokemon trouvé: $name #$pokedex_num (ID: $pokemon_id)"
    else
        echo "  ⚠️  Pokemon $name non créé"
    fi
done

echo ""
echo "🎣 Création des captures..."
CAPTURE_COUNT=0

# Chaque trainer capture quelques pokemons aléatoirement
if [ ${#POKEMON_IDS[@]} -eq 0 ]; then
    echo "  ⚠️  Aucun pokemon disponible, impossible de créer des captures"
else
    for trainer_id in "${TRAINER_IDS[@]}"; do
        # Chaque trainer capture 2-4 pokemons
        num_captures=$((RANDOM % 3 + 2))

        for ((i=0; i<num_captures; i++)); do
            # Sélectionner un pokemon aléatoire
            random_index=$((RANDOM % ${#POKEMON_IDS[@]}))
            pokemon_id=${POKEMON_IDS[$random_index]}

            response=$(post_request "/caught-pokemons" "{\"trainerId\":$trainer_id,\"pokemonId\":$pokemon_id}" "$COOKIE_FILE")
            body=$(echo "$response" | head -n -1)
            capture_id=$(extract_id "$body")

            if [ ! -z "$capture_id" ]; then
                CAPTURE_COUNT=$((CAPTURE_COUNT + 1))
            fi
        done
    done
fi

echo "  ✓ $CAPTURE_COUNT captures créées"

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
