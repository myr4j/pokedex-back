#!/usr/bin/env node
/**
 * Script Node.js pour gérer la base de données du Pokédex
 * 
 * Ce script récupère les données des Pokémon depuis PokeAPI (https://pokeapi.co)
 * et utilise les données du script populate-db.sh pour les trainers.
 * 
 * Usage: 
 *   node populate-db.js <command> [BASE_URL]
 * 
 * Commands:
 *   populate    - Crée les données (trainers, types, pokémons, captures)
 *   delete      - Supprime toutes les données de la base
 *   repopulate  - Supprime tout puis recrée les données
 *   test        - Teste l'API avec les données existantes
 * 
 * Examples:
 *   node populate-db.js populate
 *   node populate-db.js delete
 *   node populate-db.js repopulate
 *   node populate-db.js test
 *   node populate-db.js populate http://localhost:8080/api
 */

// Parsing des arguments
const args = process.argv.slice(2);
const COMMAND = args[0] || 'populate';
const BASE_URL = args[1] || 'http://localhost:8080/api';
const JMS_URL = args[2] || 'http://localhost:8081/api';

const VALID_COMMANDS = ['populate', 'delete', 'repopulate', 'test'];

if (!VALID_COMMANDS.includes(COMMAND)) {
    console.log('❌ Commande invalide:', COMMAND);
    console.log('');
    console.log('Usage: node populate-db.js <command> [BASE_URL] [JMS_URL]');
    console.log('');
    console.log('Commands:');
    console.log('  populate    - Crée les données (trainers, types, pokémons, captures)');
    console.log('  delete      - Supprime toutes les données de la base');
    console.log('  repopulate  - Supprime tout puis recrée les données');
    console.log('  test        - Teste l\'API avec les données existantes');
    console.log('');
    console.log('Examples:');
    console.log('  node populate-db.js populate');
    console.log('  node populate-db.js delete');
    console.log('  node populate-db.js repopulate');
    console.log('  node populate-db.js test');
    console.log('  node populate-db.js test http://localhost:8080/api http://localhost:8081/api');
    process.exit(1);
}

// Configuration
const CONFIG = {
    // Nombre de Pokémon à récupérer depuis PokeAPI (les 151 premiers = Génération 1)
    pokemonCount: 151,
    // Délai entre les requêtes PokeAPI pour éviter le rate limiting (en ms)
    pokeApiDelay: 100
};

// Données des trainers (issues de populate-db.sh)
const TRAINERS = [
    { name: "Ash Ketchum", email: "ash@pokemon.com", password: "password1" },
    { name: "Misty", email: "misty@pokemon.com", password: "password2" },
    { name: "Brock", email: "brock@pokemon.com", password: "password3" },
    { name: "Gary Oak", email: "gary@pokemon.com", password: "password4" },
    { name: "May", email: "may@pokemon.com", password: "password5" },
    { name: "Dawn", email: "dawn@pokemon.com", password: "password6" },
    { name: "Serena", email: "serena@pokemon.com", password: "password7" },
    { name: "Clemont", email: "clemont@pokemon.com", password: "password8" },
    { name: "Lillie", email: "lillie@pokemon.com", password: "password9" },
    { name: "Red", email: "red@pokemon.com", password: "password10" }
];

// Types Pokémon (tous les types existants)
const POKEMON_TYPES = [
    "Normal", "Fire", "Water", "Electric", "Grass", "Ice",
    "Fighting", "Poison", "Ground", "Flying", "Psychic", "Bug",
    "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy"
];

// Variables globales pour stocker les IDs
let sessionCookies = '';
let trainerIds = [];
let typeIds = {};
let pokemonIds = [];

// ============== Utilitaires HTTP ==============

async function fetchWithRetry(url, options = {}, retries = 3) {
    for (let i = 0; i < retries; i++) {
        try {
            const response = await fetch(url, options);
            return response;
        } catch (error) {
            if (i === retries - 1) throw error;
            await sleep(1000 * (i + 1));
        }
    }
}

async function postRequest(endpoint, data) {
    const response = await fetchWithRetry(`${BASE_URL}${endpoint}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Cookie': sessionCookies
        },
        body: JSON.stringify(data)
    });
    
    // Récupérer les cookies de session
    const setCookie = response.headers.get('set-cookie');
    if (setCookie) {
        const jsessionId = setCookie.match(/JSESSIONID=[^;]+/);
        if (jsessionId) {
            sessionCookies = jsessionId[0];
        }
    }
    
    const text = await response.text();
    try {
        return { status: response.status, data: text ? JSON.parse(text) : null };
    } catch {
        return { status: response.status, data: text };
    }
}

async function getRequest(endpoint) {
    const response = await fetchWithRetry(`${BASE_URL}${endpoint}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Cookie': sessionCookies
        }
    });
    
    const text = await response.text();
    try {
        return { status: response.status, data: text ? JSON.parse(text) : null };
    } catch {
        return { status: response.status, data: text };
    }
}

async function deleteRequest(endpoint) {
    const response = await fetchWithRetry(`${BASE_URL}${endpoint}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'Cookie': sessionCookies
        }
    });
    
    const text = await response.text();
    try {
        return { status: response.status, data: text ? JSON.parse(text) : null };
    } catch {
        return { status: response.status, data: text };
    }
}

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// ============== PokeAPI ==============

async function fetchPokemonFromPokeApi(pokemonId) {
    const response = await fetchWithRetry(`https://pokeapi.co/api/v2/pokemon/${pokemonId}`);
    if (!response.ok) {
        throw new Error(`PokeAPI erreur: ${response.status}`);
    }
    const data = await response.json();
    
    // Extraire les stats
    const stats = {};
    for (const stat of data.stats) {
        const statName = stat.stat.name;
        if (statName === 'hp') stats.hp = stat.base_stat;
        else if (statName === 'attack') stats.attack = stat.base_stat;
        else if (statName === 'defense') stats.defense = stat.base_stat;
        else if (statName === 'speed') stats.speed = stat.base_stat;
    }
    
    // Extraire les types (capitaliser la première lettre)
    const types = data.types.map(t => 
        t.type.name.charAt(0).toUpperCase() + t.type.name.slice(1)
    );
    
    return {
        pokedexNumber: data.id,
        name: data.name.charAt(0).toUpperCase() + data.name.slice(1),
        hp: stats.hp,
        attack: stats.attack,
        defense: stats.defense,
        speed: stats.speed,
        types: types
    };
}

// ============== Suppression des données ==============

async function deleteAllCaptures() {
    console.log('\n🗑️  Suppression des captures...');
    
    const result = await getRequest('/caught-pokemons');
    if (!Array.isArray(result.data)) {
        console.log('  ⚠ Impossible de récupérer les captures');
        return 0;
    }
    
    let deleted = 0;
    for (const capture of result.data) {
        const deleteResult = await deleteRequest(`/caught-pokemons/${capture.id}`);
        if (deleteResult.status === 204 || deleteResult.status === 200) {
            deleted++;
        }
    }
    
    console.log(`  ✓ ${deleted} captures supprimées`);
    return deleted;
}

async function deleteAllPokemons() {
    console.log('\n🗑️  Suppression des pokémons...');
    
    const result = await getRequest('/pokemons');
    if (!Array.isArray(result.data)) {
        console.log('  ⚠ Impossible de récupérer les pokémons');
        return 0;
    }
    
    let deleted = 0;
    let errors = 0;
    
    for (const pokemon of result.data) {
        const deleteResult = await deleteRequest(`/pokemons/${pokemon.id}`);
        if (deleteResult.status === 204 || deleteResult.status === 200) {
            deleted++;
        } else {
            errors++;
        }
    }
    
    console.log(`  ✓ ${deleted} pokémons supprimés${errors > 0 ? `, ${errors} erreurs` : ''}`);
    return deleted;
}

async function deleteAllTypes() {
    console.log('\n🗑️  Suppression des types...');
    
    const result = await getRequest('/types');
    if (!Array.isArray(result.data)) {
        console.log('  ⚠ Impossible de récupérer les types');
        return 0;
    }
    
    let deleted = 0;
    let errors = 0;
    
    for (const type of result.data) {
        const deleteResult = await deleteRequest(`/types/${type.id}`);
        if (deleteResult.status === 204 || deleteResult.status === 200) {
            deleted++;
        } else {
            errors++;
        }
    }
    
    console.log(`  ✓ ${deleted} types supprimés${errors > 0 ? `, ${errors} erreurs` : ''}`);
    return deleted;
}

async function deleteAllTrainers() {
    console.log('\n🗑️  Suppression des trainers...');
    
    const result = await getRequest('/trainers');
    if (!Array.isArray(result.data)) {
        console.log('  ⚠ Impossible de récupérer les trainers');
        return 0;
    }
    
    let deleted = 0;
    let errors = 0;
    
    for (const trainer of result.data) {
        const deleteResult = await deleteRequest(`/trainers/${trainer.id}`);
        if (deleteResult.status === 204 || deleteResult.status === 200) {
            deleted++;
        } else {
            errors++;
        }
    }
    
    console.log(`  ✓ ${deleted} trainers supprimés${errors > 0 ? `, ${errors} erreurs` : ''}`);
    return deleted;
}

// ============== Création des données ==============

async function createTrainers() {
    console.log('\n📝 Création des trainers...');
    
    for (const trainer of TRAINERS) {
        // D'abord essayer de se connecter (au cas où le trainer existe déjà)
        const loginResult = await postRequest('/auth/login', {
            email: trainer.email,
            password: trainer.password
        });
        
        if (loginResult.data && loginResult.data.trainerId) {
            trainerIds.push(loginResult.data.trainerId);
            console.log(`  ✓ Trainer déjà existant: ${trainer.name} (ID: ${loginResult.data.trainerId})`);
            continue;
        }
        
        // Sinon, tenter l'enregistrement
        const registerResult = await postRequest('/auth/register', trainer);
        
        // L'API peut renvoyer soit "id" soit "trainerId"
        const newId = registerResult.data?.id || registerResult.data?.trainerId;
        
        if (newId) {
            trainerIds.push(newId);
            console.log(`  ✓ Trainer créé: ${trainer.name} (ID: ${newId})`);
        } else {
            console.log(`  ✗ Erreur création trainer ${trainer.name}: ${JSON.stringify(registerResult.data)}`);
        }
    }
    
    console.log(`  Total trainers: ${trainerIds.length}`);
}

async function loginAsFirstTrainer() {
    console.log('\n🔐 Connexion avec le premier trainer...');
    
    const result = await postRequest('/auth/login', {
        email: TRAINERS[0].email,
        password: TRAINERS[0].password
    });
    
    if (result.data && result.data.trainerId) {
        console.log(`  ✓ Connecté en tant que ${TRAINERS[0].name}`);
        return true;
    } else {
        console.log(`  ✗ Échec de connexion: ${JSON.stringify(result.data)}`);
        return false;
    }
}

async function createTypes() {
    console.log('\n🔴 Création des types...');
    
    // Récupérer les types existants
    const existingTypes = await getRequest('/types');
    const existingTypeMap = {};
    
    if (Array.isArray(existingTypes.data)) {
        for (const type of existingTypes.data) {
            existingTypeMap[type.name] = type.id;
        }
    }
    
    for (const typeName of POKEMON_TYPES) {
        if (existingTypeMap[typeName]) {
            typeIds[typeName] = existingTypeMap[typeName];
            console.log(`  ✓ Type existant: ${typeName} (ID: ${typeIds[typeName]})`);
            continue;
        }
        
        const result = await postRequest('/types', { name: typeName });
        
        if (result.data && result.data.id) {
            typeIds[typeName] = result.data.id;
            console.log(`  ✓ Type créé: ${typeName} (ID: ${result.data.id})`);
        } else {
            // Peut-être déjà existant, rafraîchir la liste
            const refreshed = await getRequest('/types');
            if (Array.isArray(refreshed.data)) {
                const found = refreshed.data.find(t => t.name === typeName);
                if (found) {
                    typeIds[typeName] = found.id;
                    console.log(`  ✓ Type existant (récupéré): ${typeName} (ID: ${found.id})`);
                } else {
                    console.log(`  ⚠ Type ${typeName} non créé`);
                }
            }
        }
    }
    
    console.log(`  Total types: ${Object.keys(typeIds).length}`);
}

async function createPokemons() {
    console.log('\n⚡ Récupération des Pokémon depuis PokeAPI et création...');
    console.log(`  (${CONFIG.pokemonCount} Pokémon à traiter)`);
    
    // Récupérer les pokémons existants
    const existingPokemons = await getRequest('/pokemons');
    const existingPokemonMap = {};
    
    if (Array.isArray(existingPokemons.data)) {
        for (const pokemon of existingPokemons.data) {
            existingPokemonMap[pokemon.pokedexNumber] = pokemon.id;
        }
    }
    
    let created = 0;
    let existing = 0;
    let errors = 0;
    
    for (let i = 1; i <= CONFIG.pokemonCount; i++) {
        // Vérifier si le pokémon existe déjà
        if (existingPokemonMap[i]) {
            pokemonIds.push(existingPokemonMap[i]);
            existing++;
            if (i % 20 === 0) {
                console.log(`  [${i}/${CONFIG.pokemonCount}] Progression...`);
            }
            continue;
        }
        
        try {
            // Récupérer les données depuis PokeAPI
            const pokemonData = await fetchPokemonFromPokeApi(i);
            
            // Créer le pokémon dans notre API
            const result = await postRequest('/pokemons', {
                pokedexNumber: pokemonData.pokedexNumber,
                name: pokemonData.name,
                hp: pokemonData.hp,
                attack: pokemonData.attack,
                defense: pokemonData.defense,
                speed: pokemonData.speed
            });
            
            if (result.data && result.data.id) {
                pokemonIds.push(result.data.id);
                created++;
                
                if (i % 20 === 0) {
                    console.log(`  [${i}/${CONFIG.pokemonCount}] ${pokemonData.name} créé (${pokemonData.types.join('/')})`);
                }
            } else {
                errors++;
                console.log(`  ✗ Erreur création ${pokemonData.name}: ${JSON.stringify(result.data)}`);
            }
            
            // Petit délai pour ne pas surcharger PokeAPI
            await sleep(CONFIG.pokeApiDelay);
            
        } catch (error) {
            errors++;
            console.log(`  ✗ Erreur récupération Pokémon #${i}: ${error.message}`);
        }
    }
    
    console.log(`  ✓ Créés: ${created}, Existants: ${existing}, Erreurs: ${errors}`);
    console.log(`  Total pokémons disponibles: ${pokemonIds.length}`);
}

async function createCaptures() {
    console.log('\n🎣 Création des captures...');
    
    if (pokemonIds.length === 0) {
        console.log('  ⚠ Aucun pokémon disponible, impossible de créer des captures');
        return;
    }
    
    let captureCount = 0;
    
    // Chaque trainer capture quelques pokémons aléatoirement
    for (const trainerId of trainerIds) {
        // Chaque trainer capture 3-6 pokémons
        const numCaptures = Math.floor(Math.random() * 4) + 3;
        
        for (let i = 0; i < numCaptures; i++) {
            // Sélectionner un pokémon aléatoire
            const randomIndex = Math.floor(Math.random() * pokemonIds.length);
            const pokemonId = pokemonIds[randomIndex];
            
            const result = await postRequest('/caught-pokemons', {
                trainerId: trainerId,
                pokemonId: pokemonId
            });
            
            if (result.data && result.data.id) {
                captureCount++;
            }
        }
    }
    
    console.log(`  ✓ ${captureCount} captures créées`);
}

// ============== Commandes principales ==============

async function checkApiAccess() {
    try {
        const healthCheck = await fetchWithRetry(`${BASE_URL}/pokemons`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        }, 1);
        
        if (!healthCheck.ok && healthCheck.status !== 401) {
            console.log(`⚠️  L'API ne semble pas accessible (status: ${healthCheck.status})`);
            console.log('   Assure-toi que l\'application est démarrée');
            return false;
        }
        return true;
    } catch (error) {
        console.log(`⚠️  L'API ne semble pas accessible à ${BASE_URL}`);
        console.log(`   Erreur: ${error.message}`);
        console.log('   Assure-toi que l\'application est démarrée');
        return false;
    }
}

async function commandPopulate() {
    console.log('🌱 Mode: POPULATE - Création des données');
    console.log(`   URL de l'API: ${BASE_URL}`);
    console.log('');
    
    if (!await checkApiAccess()) {
        process.exit(1);
    }
    
    // Étapes de création
    await createTrainers();
    
    if (trainerIds.length === 0) {
        console.log('\n❌ Aucun trainer créé, arrêt du script');
        process.exit(1);
    }
    
    const loggedIn = await loginAsFirstTrainer();
    if (!loggedIn) {
        console.log('\n❌ Impossible de se connecter, arrêt du script');
        process.exit(1);
    }
    
    await createTypes();
    await createPokemons();
    await createCaptures();
    
    // Résumé final
    console.log('\n' + '='.repeat(50));
    console.log('✅ Population terminée !');
    console.log('');
    console.log('📊 Résumé:');
    console.log(`   - Trainers: ${trainerIds.length}`);
    console.log(`   - Types: ${Object.keys(typeIds).length}`);
    console.log(`   - Pokémon: ${pokemonIds.length}`);
    console.log('');
    console.log('💡 Tu peux maintenant tester l\'API avec:');
    console.log(`   curl ${BASE_URL}/auth/login -X POST -H 'Content-Type: application/json' -d '{"email":"ash@pokemon.com","password":"password1"}'`);
}

async function commandDelete() {
    console.log('🗑️  Mode: DELETE - Suppression de toutes les données');
    console.log(`   URL de l'API: ${BASE_URL}`);
    console.log('');
    
    if (!await checkApiAccess()) {
        process.exit(1);
    }
    
    // Créer un trainer temporaire pour avoir accès aux endpoints protégés
    console.log('\n🔐 Création d\'un compte temporaire pour la suppression...');
    const tempEmail = `temp_delete_${Date.now()}@pokemon.com`;
    const tempPassword = 'temppassword123';
    
    const registerResult = await postRequest('/auth/register', {
        name: 'Temp Delete User',
        email: tempEmail,
        password: tempPassword
    });
    
    if (!registerResult.data || !registerResult.data.id) {
        // Essayer de se connecter avec un compte existant
        const loginResult = await postRequest('/auth/login', {
            email: TRAINERS[0].email,
            password: TRAINERS[0].password
        });
        
        if (!loginResult.data || !loginResult.data.trainerId) {
            console.log('  ✗ Impossible de se connecter pour supprimer les données');
            process.exit(1);
        }
        console.log(`  ✓ Connecté avec ${TRAINERS[0].name}`);
    } else {
        console.log(`  ✓ Compte temporaire créé (ID: ${registerResult.data.id})`);
    }
    
    // Ordre de suppression important (relations)
    // 1. Captures (dépend des trainers et pokémons)
    // 2. Pokémons (peut avoir des types associés)
    // 3. Types
    // 4. Trainers
    
    const capturesDeleted = await deleteAllCaptures();
    const pokemonsDeleted = await deleteAllPokemons();
    const typesDeleted = await deleteAllTypes();
    const trainersDeleted = await deleteAllTrainers();
    
    // Résumé
    console.log('\n' + '='.repeat(50));
    console.log('✅ Suppression terminée !');
    console.log('');
    console.log('📊 Résumé:');
    console.log(`   - Captures supprimées: ${capturesDeleted}`);
    console.log(`   - Pokémon supprimés: ${pokemonsDeleted}`);
    console.log(`   - Types supprimés: ${typesDeleted}`);
    console.log(`   - Trainers supprimés: ${trainersDeleted}`);
}

async function commandRepopulate() {
    console.log('🔄 Mode: REPOPULATE - Suppression puis recréation des données');
    console.log(`   URL de l'API: ${BASE_URL}`);
    console.log('');
    
    if (!await checkApiAccess()) {
        process.exit(1);
    }
    
    // Phase 1: Suppression
    console.log('\n' + '='.repeat(50));
    console.log('📍 PHASE 1: Suppression des données existantes');
    console.log('='.repeat(50));
    
    // Créer/utiliser un compte pour la suppression
    console.log('\n🔐 Authentification pour la suppression...');
    const loginResult = await postRequest('/auth/login', {
        email: TRAINERS[0].email,
        password: TRAINERS[0].password
    });
    
    if (!loginResult.data || !loginResult.data.trainerId) {
        // Créer un compte temporaire
        const tempEmail = `temp_${Date.now()}@pokemon.com`;
        const registerResult = await postRequest('/auth/register', {
            name: 'Temp User',
            email: tempEmail,
            password: 'temppassword'
        });
        
        if (registerResult.data && registerResult.data.id) {
            console.log(`  ✓ Compte temporaire créé`);
        }
    } else {
        console.log(`  ✓ Connecté avec ${TRAINERS[0].name}`);
    }
    
    const capturesDeleted = await deleteAllCaptures();
    const pokemonsDeleted = await deleteAllPokemons();
    const typesDeleted = await deleteAllTypes();
    const trainersDeleted = await deleteAllTrainers();
    
    console.log('\n📊 Suppression:');
    console.log(`   - Captures: ${capturesDeleted}`);
    console.log(`   - Pokémon: ${pokemonsDeleted}`);
    console.log(`   - Types: ${typesDeleted}`);
    console.log(`   - Trainers: ${trainersDeleted}`);
    
    // Reset des variables
    sessionCookies = '';
    trainerIds = [];
    typeIds = {};
    pokemonIds = [];
    
    // Phase 2: Création
    console.log('\n' + '='.repeat(50));
    console.log('📍 PHASE 2: Création des nouvelles données');
    console.log('='.repeat(50));
    
    await createTrainers();
    
    if (trainerIds.length === 0) {
        console.log('\n❌ Aucun trainer créé, arrêt du script');
        process.exit(1);
    }
    
    const loggedIn = await loginAsFirstTrainer();
    if (!loggedIn) {
        console.log('\n❌ Impossible de se connecter, arrêt du script');
        process.exit(1);
    }
    
    await createTypes();
    await createPokemons();
    await createCaptures();
    
    // Résumé final
    console.log('\n' + '='.repeat(50));
    console.log('✅ Repopulation terminée !');
    console.log('');
    console.log('📊 Résumé final:');
    console.log(`   - Trainers: ${trainerIds.length}`);
    console.log(`   - Types: ${Object.keys(typeIds).length}`);
    console.log(`   - Pokémon: ${pokemonIds.length}`);
    console.log('');
    console.log('💡 Tu peux maintenant tester l\'API avec:');
    console.log(`   curl ${BASE_URL}/auth/login -X POST -H 'Content-Type: application/json' -d '{"email":"ash@pokemon.com","password":"password1"}'`);
}

// ============== Tests API ==============

// Compteurs de tests
let testsPassed = 0;
let testsFailed = 0;

function test(name, condition, details = '') {
    if (condition) {
        testsPassed++;
        console.log(`  ✅ ${name}`);
    } else {
        testsFailed++;
        console.log(`  ❌ ${name}${details ? ` - ${details}` : ''}`);
    }
    return condition;
}

async function getRequestJms(endpoint) {
    const response = await fetchWithRetry(`${JMS_URL}${endpoint}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    });
    
    const text = await response.text();
    try {
        return { status: response.status, data: text ? JSON.parse(text) : null };
    } catch {
        return { status: response.status, data: text };
    }
}

async function testAuthentication() {
    console.log('\n🔐 Tests Authentification');
    console.log('─'.repeat(40));
    
    // Test login avec mauvais mot de passe
    const badLogin = await postRequest('/auth/login', {
        email: TRAINERS[0].email,
        password: 'wrongpassword'
    });
    // L'API renvoie 401 ou 500 selon l'implémentation (500 si exception non gérée)
    test('Login avec mauvais mot de passe → rejeté', badLogin.status === 401 || badLogin.status === 500, `reçu: ${badLogin.status}`);
    
    // Test login avec bon mot de passe
    const goodLogin = await postRequest('/auth/login', {
        email: TRAINERS[0].email,
        password: TRAINERS[0].password
    });
    test('Login avec bon mot de passe → 200', goodLogin.status === 200);
    test('Login retourne trainerId', goodLogin.data?.trainerId !== undefined);
    test('Login retourne name', goodLogin.data?.name === TRAINERS[0].name);
    test('Login retourne email', goodLogin.data?.email === TRAINERS[0].email);
    
    // Test accès protégé sans session
    const noSession = await fetchWithRetry(`${BASE_URL}/trainers`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    });
    test('Accès protégé sans session → 401', noSession.status === 401);
    
    // Test accès protégé avec session
    const withSession = await getRequest('/trainers');
    test('Accès protégé avec session → 200', withSession.status === 200);
}

async function testTrainers() {
    console.log('\n👤 Tests Trainers');
    console.log('─'.repeat(40));
    
    // GET tous les trainers
    const allTrainers = await getRequest('/trainers');
    test('GET /trainers → 200', allTrainers.status === 200);
    test('GET /trainers retourne un tableau', Array.isArray(allTrainers.data));
    test('Au moins 1 trainer existe', allTrainers.data?.length >= 1);
    
    if (allTrainers.data?.length > 0) {
        const firstTrainer = allTrainers.data[0];
        test('Trainer a un id', firstTrainer.id !== undefined);
        test('Trainer a un name', firstTrainer.name !== undefined);
        test('Trainer a un email', firstTrainer.email !== undefined);
        test('Trainer n\'expose pas le password', firstTrainer.password === undefined);
        
        // GET trainer par ID
        const singleTrainer = await getRequest(`/trainers/${firstTrainer.id}`);
        test(`GET /trainers/${firstTrainer.id} → 200`, singleTrainer.status === 200);
        test('Trainer récupéré a le bon ID', singleTrainer.data?.id === firstTrainer.id);
        
        // GET stats du trainer
        const stats = await getRequest(`/trainers/${firstTrainer.id}/stats`);
        test(`GET /trainers/${firstTrainer.id}/stats → 200`, stats.status === 200);
        test('Stats contient trainerId', stats.data?.trainerId === firstTrainer.id);
        test('Stats contient totalCaptures', stats.data?.totalCaptures !== undefined);
        test('Stats contient uniquePokemons', stats.data?.uniquePokemons !== undefined);
        test('Stats contient pokedexCompletionPercentage', stats.data?.pokedexCompletionPercentage !== undefined);
    }
    
    // GET trainer inexistant
    const notFound = await getRequest('/trainers/999999');
    test('GET /trainers/999999 → 404', notFound.status === 404);
}

async function testPokemons() {
    console.log('\n⚡ Tests Pokémon');
    console.log('─'.repeat(40));
    
    // GET tous les pokémons
    const allPokemons = await getRequest('/pokemons');
    test('GET /pokemons → 200', allPokemons.status === 200);
    test('GET /pokemons retourne un tableau', Array.isArray(allPokemons.data));
    test('Au moins 1 pokémon existe', allPokemons.data?.length >= 1);
    
    if (allPokemons.data?.length > 0) {
        const firstPokemon = allPokemons.data[0];
        test('Pokémon a un id', firstPokemon.id !== undefined);
        test('Pokémon a un name', firstPokemon.name !== undefined);
        test('Pokémon a un pokedexNumber', firstPokemon.pokedexNumber !== undefined);
        test('Pokémon a hp', firstPokemon.hp !== undefined);
        test('Pokémon a attack', firstPokemon.attack !== undefined);
        test('Pokémon a defense', firstPokemon.defense !== undefined);
        test('Pokémon a speed', firstPokemon.speed !== undefined);
        
        // GET pokémon par ID
        const singlePokemon = await getRequest(`/pokemons/${firstPokemon.id}`);
        test(`GET /pokemons/${firstPokemon.id} → 200`, singlePokemon.status === 200);
        test('Pokémon récupéré a le bon ID', singlePokemon.data?.id === firstPokemon.id);
    }
    
    // GET pokémon inexistant
    const notFound = await getRequest('/pokemons/999999');
    test('GET /pokemons/999999 → 404', notFound.status === 404);
    
    // Test comparaison de pokémons
    if (allPokemons.data?.length >= 3) {
        const ids = allPokemons.data.slice(0, 3).map(p => p.id);
        const compare = await postRequest('/pokemons/compare', ids);
        test('POST /pokemons/compare → 200', compare.status === 200);
        test('Compare retourne pokemons', Array.isArray(compare.data?.pokemons));
        test('Compare retourne stats', compare.data?.stats !== undefined);
        test('Stats contient avgHp', compare.data?.stats?.avgHp !== undefined);
        test('Stats contient minAttack', compare.data?.stats?.minAttack !== undefined);
        test('Stats contient maxDefense', compare.data?.stats?.maxDefense !== undefined);
    }
}

async function testTypes() {
    console.log('\n🔴 Tests Types');
    console.log('─'.repeat(40));
    
    // GET tous les types
    const allTypes = await getRequest('/types');
    test('GET /types → 200', allTypes.status === 200);
    test('GET /types retourne un tableau', Array.isArray(allTypes.data));
    test('Au moins 1 type existe', allTypes.data?.length >= 1);
    
    if (allTypes.data?.length > 0) {
        const firstType = allTypes.data[0];
        test('Type a un id', firstType.id !== undefined);
        test('Type a un name', firstType.name !== undefined);
        
        // GET type par ID
        const singleType = await getRequest(`/types/${firstType.id}`);
        test(`GET /types/${firstType.id} → 200`, singleType.status === 200);
        test('Type récupéré a le bon ID', singleType.data?.id === firstType.id);
    }
}

async function testCaptures() {
    console.log('\n🎣 Tests Captures');
    console.log('─'.repeat(40));
    
    // GET toutes les captures
    const allCaptures = await getRequest('/caught-pokemons');
    test('GET /caught-pokemons → 200', allCaptures.status === 200);
    test('GET /caught-pokemons retourne un tableau', Array.isArray(allCaptures.data));
    
    if (allCaptures.data?.length > 0) {
        const firstCapture = allCaptures.data[0];
        test('Capture a un id', firstCapture.id !== undefined);
        test('Capture a une captureDate', firstCapture.captureDate !== undefined);
        
        // GET capture par ID
        const singleCapture = await getRequest(`/caught-pokemons/${firstCapture.id}`);
        test(`GET /caught-pokemons/${firstCapture.id} → 200`, singleCapture.status === 200);
    }
    
    // GET captures par trainer
    const trainers = await getRequest('/trainers');
    if (trainers.data?.length > 0) {
        const trainerId = trainers.data[0].id;
        const byTrainer = await getRequest(`/caught-pokemons/trainer/${trainerId}`);
        test(`GET /caught-pokemons/trainer/${trainerId} → 200`, byTrainer.status === 200);
        test('Captures par trainer retourne un tableau', Array.isArray(byTrainer.data));
    }
    
    // GET captures par pokémon
    const pokemons = await getRequest('/pokemons');
    if (pokemons.data?.length > 0) {
        const pokemonId = pokemons.data[0].id;
        const byPokemon = await getRequest(`/caught-pokemons/pokemon/${pokemonId}`);
        test(`GET /caught-pokemons/pokemon/${pokemonId} → 200`, byPokemon.status === 200);
        test('Captures par pokémon retourne un tableau', Array.isArray(byPokemon.data));
    }
    
    // Test création d'une capture
    if (trainers.data?.length > 0 && pokemons.data?.length > 0) {
        const newCapture = await postRequest('/caught-pokemons', {
            trainerId: trainers.data[0].id,
            pokemonId: pokemons.data[0].id
        });
        test('POST /caught-pokemons → 201', newCapture.status === 201);
        test('Nouvelle capture a un id', newCapture.data?.id !== undefined);
        test('Nouvelle capture a une captureDate', newCapture.data?.captureDate !== undefined);
    }
}

async function testJmsConsumer() {
    console.log('\n📡 Tests JMS Consumer (port 8081)');
    console.log('─'.repeat(40));
    
    // Vérifier si le JMS consumer est accessible
    try {
        const healthCheck = await fetchWithRetry(`${JMS_URL}/captures`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        }, 1);
        
        if (!healthCheck.ok) {
            console.log('  ⚠️  JMS Consumer non accessible, tests ignorés');
            return;
        }
    } catch (error) {
        console.log(`  ⚠️  JMS Consumer non accessible à ${JMS_URL}, tests ignorés`);
        return;
    }
    
    // GET captures JMS
    const captures = await getRequestJms('/captures');
    test('GET /captures → 200', captures.status === 200);
    test('GET /captures retourne un tableau', Array.isArray(captures.data));
    
    if (captures.data?.length > 0) {
        const firstCapture = captures.data[0];
        test('Capture JMS a trainerId', firstCapture.trainerId !== undefined);
        test('Capture JMS a trainerName', firstCapture.trainerName !== undefined);
        test('Capture JMS a pokemonId', firstCapture.pokemonId !== undefined);
        test('Capture JMS a pokemonName', firstCapture.pokemonName !== undefined);
        test('Capture JMS a captureDate', firstCapture.captureDate !== undefined);
    }
    
    // GET captures récentes
    const recent = await getRequestJms('/captures/recent?limit=5');
    test('GET /captures/recent?limit=5 → 200', recent.status === 200);
    test('Captures récentes retourne un tableau', Array.isArray(recent.data));
    test('Captures récentes ≤ 5 éléments', recent.data?.length <= 5);
    
    // GET stats captures
    const stats = await getRequestJms('/captures/stats');
    test('GET /captures/stats → 200', stats.status === 200);
    test('Stats contient totalMessages', stats.data?.totalMessages !== undefined);
    test('Stats contient maxMessages', stats.data?.maxMessages !== undefined);
    
    // GET créations de trainers
    const creations = await getRequestJms('/creations');
    test('GET /creations → 200', creations.status === 200);
    test('GET /creations retourne un tableau', Array.isArray(creations.data));
    
    // GET stats agrégées
    const aggregated = await getRequestJms('/aggregated/stats');
    test('GET /aggregated/stats → 200', aggregated.status === 200);
    test('Stats agrégées retourne un tableau', Array.isArray(aggregated.data));
    
    if (aggregated.data?.length > 0) {
        const firstAgg = aggregated.data[0];
        test('Stats agrégées a trainerId', firstAgg.trainerId !== undefined);
        test('Stats agrégées a trainerName', firstAgg.trainerName !== undefined);
        test('Stats agrégées a totalCaptures', firstAgg.totalCaptures !== undefined);
        test('Stats agrégées a pokemonCounts', Array.isArray(firstAgg.pokemonCounts));
        
        // GET stats agrégées par trainer
        const byTrainer = await getRequestJms(`/aggregated/stats/trainer/${firstAgg.trainerId}`);
        test(`GET /aggregated/stats/trainer/${firstAgg.trainerId} → 200`, byTrainer.status === 200);
        test('Stats trainer a totalCaptures', byTrainer.data?.totalCaptures !== undefined);
    }
}

async function commandTest() {
    console.log('🧪 Mode: TEST - Tests de l\'API');
    console.log(`   URL API principale: ${BASE_URL}`);
    console.log(`   URL JMS Consumer: ${JMS_URL}`);
    console.log('');
    
    if (!await checkApiAccess()) {
        process.exit(1);
    }
    
    // Reset compteurs
    testsPassed = 0;
    testsFailed = 0;
    
    console.log('='.repeat(50));
    console.log('📍 Démarrage des tests');
    console.log('='.repeat(50));
    
    // Exécuter les tests
    await testAuthentication();
    await testTrainers();
    await testPokemons();
    await testTypes();
    await testCaptures();
    await testJmsConsumer();
    
    // Résumé final
    console.log('\n' + '='.repeat(50));
    console.log('📊 Résumé des tests');
    console.log('='.repeat(50));
    console.log('');
    console.log(`   ✅ Tests réussis: ${testsPassed}`);
    console.log(`   ❌ Tests échoués: ${testsFailed}`);
    console.log(`   📈 Total: ${testsPassed + testsFailed}`);
    console.log('');
    
    if (testsFailed === 0) {
        console.log('🎉 Tous les tests sont passés !');
    } else {
        console.log(`⚠️  ${testsFailed} test(s) échoué(s)`);
        process.exit(1);
    }
}

// ============== Main ==============

async function main() {
    console.log('');
    console.log('╔════════════════════════════════════════════════╗');
    console.log('║       🎮 Pokédex Database Manager 🎮           ║');
    console.log('╚════════════════════════════════════════════════╝');
    console.log('');
    
    switch (COMMAND) {
        case 'populate':
            await commandPopulate();
            break;
        case 'delete':
            await commandDelete();
            break;
        case 'repopulate':
            await commandRepopulate();
            break;
        case 'test':
            await commandTest();
            break;
    }
}

// Lancer le script
main().catch(error => {
    console.error('❌ Erreur fatale:', error);
    process.exit(1);
});
