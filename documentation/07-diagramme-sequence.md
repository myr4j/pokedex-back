
sequenceDiagram
    participant Client as Client HTTP
    participant AuthFilter as AuthFilter
    participant CaughtR as CaughtPokemonResource
    participant CaughtS as CaughtPokemonService
    participant EM as EntityManager (JPA)
    participant DB as PostgreSQL
    participant CaptureP as CaptureMessageProducer
    participant Artemis as ActiveMQ Artemis
    participant JmsListener as JmsMessageListener
    participant MessageLog as MessageLogService
    participant Aggregator as CaptureAggregator
    participant CapturesR as CapturesResource

    Note over Client,CapturesR: Flux de Capture d'un Pokémon

    %% Étape 1: Requête HTTP
    Client->>CaughtR: POST /api/caught-pokemons<br/>{trainerId: 21, pokemonId: 8}
    
    %% Étape 2: Vérification d'authentification
    CaughtR->>AuthFilter: Vérification @Secured
    AuthFilter->>AuthFilter: Session HTTP valide ?
    alt Session invalide
        AuthFilter-->>Client: 401 UNAUTHORIZED
    else Session valide
        AuthFilter-->>CaughtR: Autorisation accordée
        
        %% Étape 3: Délégation au service
        CaughtR->>CaughtS: createCaughtPokemon(21, 8)
        
        %% Étape 4: Récupération des entités
        CaughtS->>EM: find(Trainer.class, 21)
        EM->>DB: SELECT * FROM trainers WHERE id = 21
        DB-->>EM: Trainer(id=21, name="Ash Ketchum")
        EM-->>CaughtS: Trainer
        
        CaughtS->>EM: find(Pokemon.class, 8)
        EM->>DB: SELECT * FROM pokemons WHERE id = 8
        DB-->>EM: Pokemon(id=8, name="Wartortle")
        EM-->>CaughtS: Pokemon
        
        %% Étape 5: Création et persistance
        CaughtS->>CaughtS: new CaughtPokemon(trainer, pokemon)
        CaughtS->>EM: persist(caughtPokemon)
        EM->>DB: INSERT INTO caught_pokemons<br/>(trainer_id, pokemon_id, capture_date)
        DB-->>EM: OK
        EM-->>CaughtS: CaughtPokemon persisté
        
        %% Étape 6: Envoi du message JMS
        CaughtS->>CaughtS: new CaptureMessage(21, "Ash", 8, "Wartortle")
        CaughtS->>CaptureP: sendCaptureMessage(message)
        CaptureP->>Artemis: ObjectMessage → Queue "captures"
        Artemis-->>CaptureP: Message accepté
        CaptureP-->>CaughtS: Message envoyé
        
        %% Retour synchrone
        CaughtS-->>CaughtR: CaughtPokemon
        CaughtR-->>Client: 201 CREATED + JSON
        
        Note over Client,CapturesR: Le client reçoit la réponse immédiatement
        
        %% Étape 7: Traitement asynchrone par le consumer
        Artemis->>JmsMessageListener: Message disponible
        activate JmsMessageListener
        JmsMessageListener->>JmsMessageListener: handleCaptureMessage()
        JmsMessageListener->>JmsMessageListener: Vérification type (Content-based Router)
        JmsMessageListener->>JmsMessageListener: Filtrage (Message Filter)
        JmsMessageListener->>JmsMessageListener: Log du message
        JmsMessageListener->>MessageLog: addCaptureMessage(captureMessage)
        MessageLog->>MessageLog: Stockage en mémoire (max 100)
        MessageLog-->>JmsMessageListener: Message stocké
        deactivate JmsMessageListener
        
        Note over Client,CapturesR: Le message est maintenant disponible pour consultation
        
        %% Étape 8: Consultation (optionnelle)
        Client->>CapturesR: GET /api/captures
        CapturesR->>MessageLog: getAllCaptureMessages()
        MessageLog-->>CapturesR: List<CaptureMessage>
        CapturesR-->>Client: 200 OK + JSON
        
        %% Étape 9: Statistiques agrégées (optionnelle)
        Client->>CapturesR: GET /api/aggregated/stats/trainer/21
        CapturesR->>MessageLog: getAllCaptureMessages()
        MessageLog-->>CapturesR: List<CaptureMessage>
        CapturesR->>Aggregator: aggregateByTrainer(21, messages)
        Aggregator->>Aggregator: Regroupement par trainer<br/>Calcul des statistiques
        Aggregator-->>CapturesR: AggregatedCaptureStats
        CapturesR-->>Client: 200 OK + JSON (statistiques)
    end

