# JMS Consumer Mini-API

Mini-API qui consomme les messages JMS de capture depuis ActiveMQ Artemis et les expose via REST.

## Démarrage

### 1. Démarrer ActiveMQ Artemis

```bash
cd ..
docker compose up -d artemis
```

### 2. Lancer le consumer

```bash
cd pokedex-jms-consumer
mvn clean package -DskipTests
mvn embedded-glassfish:run
```

L'API sera disponible sur : `http://localhost:8081/api`


## Endpoints REST

### GET /api/health
Vérifie que l'API est opérationnelle.

```bash
curl http://localhost:8081/api/health
```

### GET /api/captures
Récupère tous les messages de capture de pokemon.

```bash
curl http://localhost:8081/api/captures
```

### GET /api/captures/recent?limit=N
Récupère les N captures les plus récentes (par défaut 10, max 100).

```bash
curl http://localhost:8081/api/captures/recent?limit=5
```

### GET /api/captures/stats
Récupère les statistiques des captures.

```bash
curl http://localhost:8081/api/captures/stats
```

### GET /api/creations
Récupère tous les messages de création de trainer.

```bash
curl http://localhost:8081/api/creations
```

### GET /api/creations/recent?limit=N
Récupère les N créations de trainer les plus récentes (par défaut 10, max 100).

```bash
curl http://localhost:8081/api/creations/recent?limit=5
```

### GET /api/creations/stats
Récupère les statistiques des créations.

```bash
curl http://localhost:8081/api/creations/stats
```

### GET /api/aggregated/stats
Récupère les statistiques agrégées de toutes les captures (pattern Aggregator).

```bash
curl http://localhost:8081/api/aggregated/stats
```

**Réponse exemple :**
```json
[
  {
    "trainerId": 1,
    "trainerName": "Ash Ketchum",
    "totalCaptures": 5,
    "pokemonCounts": [
      {"pokemonId": 25, "pokemonName": "Pikachu", "count": 2},
      {"pokemonId": 1, "pokemonName": "Bulbasaur", "count": 3}
    ]
  }
]
```

### GET /api/aggregated/stats/trainer/{trainerId}
Récupère les statistiques agrégées pour un trainer spécifique.

```bash
curl http://localhost:8081/api/aggregated/stats/trainer/1
```

## Format des messages

### Messages de capture

```json
{
  "trainerId": 1,
  "trainerName": "Ash Ketchum",
  "pokemonId": 25,
  "pokemonName": "Pikachu",
  "captureDate": "2026-01-21T10:30:00"
}
```

### Messages de création de trainer

```json
{
  "trainerId": 1,
  "trainerName": "Ash Ketchum",
  "trainerEmail": "ash@pokemon.com",
  "registrationDate": "2026-01-21T10:30:00"
}
```
