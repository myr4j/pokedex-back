graph TB
    subgraph Client["Client HTTP"]
        ClientWeb[Client Web/Mobile]
    end

    subgraph ModulePrincipal["Module Principal (pokedex-back)"]
        subgraph CoucheREST["Couche REST (JAX-RS)"]
            AuthResource[AuthResource]
            TrainerResource[TrainerResource]
            PokemonResource[PokemonResource]
            TypeResource[TypeResource]
            CaughtPokemonResource[CaughtPokemonResource]
        end
        
        subgraph CoucheSecurite["Couche Sécurité"]
            AuthFilter[AuthFilter]
            Secured[@Secured]
        end
        
        subgraph CoucheService["Couche Service (EJB)"]
            AuthService[AuthService]
            TrainerService[TrainerService]
            PokemonService[PokemonService]
            TypeService[TypeService]
            CaughtPokemonService[CaughtPokemonService]
            TrainerStatsService[TrainerStatsService]
            PokemonComparisonService[PokemonComparisonService]
        end
        
        subgraph CoucheMessaging["Couche Messaging (JMS)"]
            CaptureMessageProducer[CaptureMessageProducer]
            TrainerMessageProducer[TrainerMessageProducer]
        end
        
        subgraph CouchePersistence["Couche Persistence (JPA)"]
            EntityManager[EntityManager]
            TrainerEntity[Trainer]
            PokemonEntity[Pokemon]
            TypeEntity[Type]
            CaughtPokemonEntity[CaughtPokemon]
        end
    end

    subgraph ModuleConsumer["Module Consumer JMS (pokedex-jms-consumer)"]
        subgraph CoucheRESTConsumer["Couche REST (JAX-RS)"]
            CapturesResource[CapturesResource]
            CreationsResource[CreationsResource]
            AggregatedStatsResource[AggregatedStatsResource]
            HealthResource[HealthResource]
        end
        
        subgraph CoucheMessagingConsumer["Couche Messaging (JMS Consumer)"]
            JmsMessageListener[JmsMessageListener<br/>EJB Singleton @Startup]
        end
        
        subgraph CoucheServiceConsumer["Couche Service"]
            MessageLogService[MessageLogService]
            CaptureAggregator[CaptureAggregator<br/>Pattern Aggregator]
        end
    end

    subgraph Infrastructure["Infrastructure"]
        PostgreSQL[(PostgreSQL)]
        subgraph Artemis["ActiveMQ Artemis"]
            QueueCaptures[Queue: captures]
            QueueTrainers[Queue: trainers]
        end
    end

    %% Relations Client -> REST
    ClientWeb -->|HTTP POST /api/auth/register| AuthResource
    ClientWeb -->|HTTP POST /api/auth/login| AuthResource
    ClientWeb -->|HTTP GET/POST /api/trainers| TrainerResource
    ClientWeb -->|HTTP GET/POST /api/pokemons| PokemonResource
    ClientWeb -->|HTTP GET/POST /api/types| TypeResource
    ClientWeb -->|HTTP GET/POST /api/caught-pokemons| CaughtPokemonResource
    ClientWeb -->|HTTP GET /api/captures| CapturesResource
    ClientWeb -->|HTTP GET /api/creations| CreationsResource
    ClientWeb -->|HTTP GET /api/aggregated/stats| AggregatedStatsResource

    %% Relations REST -> Sécurité
    AuthResource -.->|utilise| Secured
    TrainerResource -.->|@Secured| Secured
    PokemonResource -.->|@Secured| Secured
    TypeResource -.->|@Secured| Secured
    CaughtPokemonResource -.->|@Secured| Secured
    Secured -->|déclenche| AuthFilter

    %% Relations REST -> Service
    AuthResource -->|délègue| AuthService
    TrainerResource -->|délègue| TrainerService
    TrainerResource -->|délègue| TrainerStatsService
    PokemonResource -->|délègue| PokemonService
    PokemonResource -->|délègue| PokemonComparisonService
    TypeResource -->|délègue| TypeService
    CaughtPokemonResource -->|délègue| CaughtPokemonService

    %% Relations Service -> Persistence
    AuthService -->|utilise| EntityManager
    TrainerService -->|utilise| EntityManager
    PokemonService -->|utilise| EntityManager
    TypeService -->|utilise| EntityManager
    CaughtPokemonService -->|utilise| EntityManager
    TrainerStatsService -->|utilise| EntityManager
    PokemonComparisonService -->|utilise| EntityManager

    %% Relations Persistence -> Entities
    EntityManager -->|gère| TrainerEntity
    EntityManager -->|gère| PokemonEntity
    EntityManager -->|gère| TypeEntity
    EntityManager -->|gère| CaughtPokemonEntity

    %% Relations Persistence -> Database
    TrainerEntity -->|table trainers| PostgreSQL
    PokemonEntity -->|table pokemons| PostgreSQL
    TypeEntity -->|table types| PostgreSQL
    CaughtPokemonEntity -->|table caught_pokemons| PostgreSQL

    %% Relations Service -> Messaging
    CaughtPokemonService -->|envoie message| CaptureMessageProducer
    AuthService -->|envoie message| TrainerMessageProducer

    %% Relations Messaging -> Broker
    CaptureMessageProducer -->|envoie| QueueCaptures
    TrainerMessageProducer -->|envoie| QueueTrainers
    QueueCaptures -->|consomme| JmsMessageListener
    QueueTrainers -->|consomme| JmsMessageListener

    %% Relations Consumer
    JmsMessageListener -->|stocke| MessageLogService
    CapturesResource -->|lit| MessageLogService
    CreationsResource -->|lit| MessageLogService
    AggregatedStatsResource -->|lit| MessageLogService
    AggregatedStatsResource -->|agrège| CaptureAggregator
    CaptureAggregator -->|utilise| MessageLogService

    %% Styles
    classDef restClass fill:#e1f5ff,stroke:#01579b,stroke-width:2px
    classDef serviceClass fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef entityClass fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef messagingClass fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px
    classDef infraClass fill:#fce4ec,stroke:#880e4f,stroke-width:2px
    classDef securityClass fill:#fff9c4,stroke:#f57f17,stroke-width:2px

    class AuthResource,TrainerResource,PokemonResource,TypeResource,CaughtPokemonResource,CapturesResource,CreationsResource,AggregatedStatsResource,HealthResource restClass
    class AuthService,TrainerService,PokemonService,TypeService,CaughtPokemonService,TrainerStatsService,PokemonComparisonService,MessageLogService,CaptureAggregator serviceClass
    class TrainerEntity,PokemonEntity,TypeEntity,CaughtPokemonEntity,EntityManager entityClass
    class CaptureMessageProducer,TrainerMessageProducer,JmsMessageListener,QueueCaptures,QueueTrainers messagingClass
    class PostgreSQL,Artemis infraClass
    class AuthFilter,Secured securityClass