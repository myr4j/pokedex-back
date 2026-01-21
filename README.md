# Pokedex Backend - Jakarta EE

REST API for managing a Pokedex with authentication and endpoint security.

## Quick Start

### Prerequisites
- Java 21
- Docker

## Architecture
- **JAX-RS** : REST API (Jersey)
- **JPA/Hibernate** : Data persistence
- **EJB** : Business services (@Stateless)
- **CDI** : Dependency injection
- **GlassFish** : Jakarta EE application server
- **PostgreSQL** : Database
- **ActiveMQ Artemis** : Message broker (JMS)


### 1. Start services (PostgreSQL + Artemis)
```bash
docker compose up -d
```

### 2. Run the application
```bash
mvn clean package
mvn embedded-glassfish:run
```

API is available at: `http://localhost:8080/api`

### 3. Populate database (optional)
```bash
./populate-db.sh http://localhost:8080/api
```

## Features

### Authentication
- **POST** `/api/auth/register` - Register (name, email, password)
- **POST** `/api/auth/login` - Login (email, password)
- **POST** `/api/auth/logout` - Logout
- Password hashing with BCrypt
- HTTP sessions with JSESSIONID cookies (expire after 30 minutes of inactivity (default GlassFish behavior))
- Endpoint protection with `@Secured`

### Trainers
- **POST** `/api/trainers` - Create a trainer
- **GET** `/api/trainers` - List all trainers
- **GET** `/api/trainers/{id}` - Get trainer details
- **PUT** `/api/trainers/{id}` - Update a trainer
- **DELETE** `/api/trainers/{id}` - Delete a trainer
- **GET** `/api/trainers/{id}/stats` - Get trainer statistics

### Pokémons
- **POST** `/api/pokemons` - Create a pokémon
- **GET** `/api/pokemons` - List all pokémons
- **GET** `/api/pokemons/{id}` - Get pokémon details
- **PUT** `/api/pokemons/{id}` - Update a pokémon
- **DELETE** `/api/pokemons/{id}` - Delete a pokémon
- **POST** `/api/pokemons/compare` - Compare multiple pokémons (stats)

### Types
- **POST** `/api/types` - Create a type
- **GET** `/api/types` - List all types
- **GET** `/api/types/{id}` - Get type details
- **PUT** `/api/types/{id}` - Update a type
- **DELETE** `/api/types/{id}` - Delete a type

### Captures (CaughtPokemons)
- **POST** `/api/caught-pokemons` - Record a capture (trainerId, pokemonId)
- **GET** `/api/caught-pokemons` - List all captures
- **GET** `/api/caught-pokemons/{id}` - Get capture details
- **GET** `/api/caught-pokemons/trainer/{trainerId}` - Get trainer's captures
- **GET** `/api/caught-pokemons/pokemon/{pokemonId}` - Get trainers who caught a pokémon
- **DELETE** `/api/caught-pokemons/{id}` - Delete a capture


**Usage example:**
```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Ash Ketchum","email":"ash@pokemon.com","password":"pikachu123"}' \
  -c cookies.txt

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ash@pokemon.com","password":"pikachu123"}' \
  -c cookies.txt

# 3. Use the API (with session cookie)
curl http://localhost:8080/api/pokemons -b cookies.txt
```
## Project Structure

```
src/main/java/com/example/
├── config/          # Configuration (ApplicationConfig)
├── domain/          # JPA entities (Trainer, Pokemon, Type, CaughtPokemon)
├── dto/             # Data Transfer Objects
├── rest/            # REST resources (endpoints)
├── security/        # Authentication (@Secured, AuthFilter)
└── service/         # Business services EJB

src/test/java/       # Unit tests
```
