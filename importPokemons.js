/**
 * ============================================================
 *  Script d’import des Pokémon dans MongoDB depuis PokeAPI
 * ============================================================
 *
 * Description :
 *    Ce script supprime et recrée entièrement la collection
 *    "pokemons" puis importe automatiquement les X premiers Pokémon
 *    depuis l’API PokeAPI selon le schéma défini dans le projet.
 *
 * Schéma inséré dans MongoDB :
 *    {
 *      numeroPokedex: Number,
 *      nom: String,
 *      generation: Number,
 *      region: String,
 *      type: String,
 *      stats: { ... },
 *      capacites: [String],
 *      evolutions: {
 *        preEvolution: String | null,
 *        evolutionSuivante: String | null,
 *        condition: String | null
 *      }
 *    }
 *
 * Dépendances :
 *    npm install mongodb axios dotenv
 *
 * Usage :
 *    Créer un fichier .env contenant :
 *        MONGO_URI=mongodb://localhost:27017
 *
 *    Lancer le script :
 *        node importPokemons.js
 *
 */

require("dotenv").config();
const { MongoClient } = require("mongodb");
const axios = require("axios");

// ===== PARAMÈTRES =====
const limit = 10;
const db_name = "pokedex";
const collections = ["types", "trainers"]; // pokemons sera géré manuellement

// ===== TABLE GÉNÉRATION → RÉGION =====
function getRegionByGeneration(gen) {
  switch (gen) {
    case 1: return "Kanto";
    case 2: return "Johto";
    case 3: return "Hoenn";
    case 4: return "Sinnoh";
    case 5: return "Unys";
    case 6: return "Kalos";
    case 7: return "Alola";
    case 8: return "Galar";
    case 9: return "Paldea";
    default: return "Inconnue";
  }
}

// ===== FONCTIONS API =====
async function getPokemon(id) {
  return (await axios.get(`https://pokeapi.co/api/v2/pokemon/${id}`)).data;
}

async function getSpecies(id) {
  return (await axios.get(`https://pokeapi.co/api/v2/pokemon-species/${id}`)).data;
}

async function getEvolutionChain(url) {
  return (await axios.get(url)).data;
}

// ===== Extraction évolutions =====
function extractEvolutionData(chain, target) {
  const getId = (url) => Number(url.match(/\/pokemon-species\/(\d+)\//)[1]);

  if (chain.species.name === target) {
    const evo = chain.evolves_to[0];

    return {
      preEvolution: null,  // sera rempli plus bas
      evolutionSuivante: evo ? getId(evo.species.url) : null,
      condition: evo ? evo.evolution_details[0]?.trigger?.name || null : null
    };
  }

  for (const evo of chain.evolves_to) {
    const result = extractEvolutionData(evo, target);
    if (result) {
      result.preEvolution = getId(chain.species.url);
      return result;
    }
  }
  return null;
}

// ===== SCRIPT PRINCIPAL =====
async function run() {
  const client = new MongoClient(process.env.MONGO_URI);

  try {
    await client.connect();
    const db = client.db(db_name);
    console.log("Connecté à MongoDB");

    // Supprimer et recréer la collection pokemons
    try {
      await db.collection("pokemons").drop();
      console.log("Collection pokemons supprimée");
    } catch {}
    await db.createCollection("pokemons");
    console.log("Nouvelle collection pokemons créée");

    // Créer les autres collections si besoin
    for (const col of collections) {
      try {
        await db.createCollection(col);
        console.log(`→ Collection ${col} créée`);
      } catch {}
    }

    const pokemonsCol = db.collection("pokemons");

    // Import Pokémon
    for (let i = 1; i <= limit; i++) {
      console.log(`\nImport Pokémon ${i}`);

      const poke = await getPokemon(i);
      const species = await getSpecies(i);

      const name = poke.name.charAt(0).toUpperCase() + poke.name.slice(1);
      const type = poke.types[0]?.type?.name || "unknown";

      // Stats
      const stats = {
        hp: poke.stats[0].base_stat,
        attaque: poke.stats[1].base_stat,
        defense: poke.stats[2].base_stat,
        attaqueSpecial: poke.stats[3].base_stat,
        defenseSpecial: poke.stats[4].base_stat,
        vitesse: poke.stats[5].base_stat,
      };

      const moves = poke.moves
        .filter(m => m.version_group_details.some(v => v.move_learn_method.name === "level-up"))
        .map(m => m.move.name);

      let genNumber = null;
      if (species.generation?.url) {
        const match = species.generation.url.match(/\/generation\/(\d+)\//);
        genNumber = match ? Number(match[1]) : null;
      }
      const region = getRegionByGeneration(genNumber);

      const evoChainURL = species.evolution_chain.url;
      const evolChain = await getEvolutionChain(evoChainURL);
      const evolutions = extractEvolutionData(evolChain.chain, poke.name) || {
        preEvolution: null,
        evolutionSuivante: null,
        condition: null
      };

      // Document final
      const doc = {
        numeroPokedex: i,
        nom: name,
        generation: genNumber,
        region,
        type,
        stats,
        capacites: moves,
        evolutions
      };

      await pokemonsCol.insertOne(doc);
      console.log(`Pokémon ${name} importé`);
    }

    console.log("\nImport terminé !");
  } catch (err) {
    console.error("Erreur :", err);
  } finally {
    await client.close();
  }
}

run();
