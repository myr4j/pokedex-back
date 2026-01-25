# Résumé des Contributions Individuelles

Ce document présente un résumé des contributions de chaque membre du groupe au projet Pokedex Backend. Il est important de noter que le travail s'est effectué de manière collaborative, avec beaucoup de peer programming en classe et d'entraide entre les membres.

## Méthodologie de Travail

Le projet a été développé selon une approche collaborative :

- **Peer Programming** : Beaucoup de fonctionnalités ont été développées en binôme ou en groupe lors des séances de cours, permettant un partage de connaissances et une meilleure qualité du code
- **Entraide** : Les membres du groupe se sont régulièrement aidés mutuellement pour résoudre les problèmes techniques
- **Documentation** : La documentation a été rédigée collectivement au fur et à mesure du développement, avec des contributions de tous les membres

Les commits individuels reflètent principalement qui a effectué le commit final, mais ne représentent pas nécessairement un travail 100% individuel.

## Contributions par Membre

### Myriem Tagnit Hammou

**Contributions principales :**
- **Configuration GlassFish et BD** :  Configuration du serveur GlassFish embarqué et configuration pour l'utilisation de la BD PostgreSQL du Docker.
- **Système de sécurité** : Implémentation de l'authentification avec email et mot de passe, sécurisation des endpoints
- **Système de messagerie JMS** : Implémentation complète du système de messagerie asynchrone avec producteurs JMS, consumer séparé, et intégration avec ActiveMQ Artemis


### Yanis Hamitouche

**Contributions principales :**

- **Réflexion sur la problématique et besoins métier** : En tant que fan de Pokémon, Yanis a apporté une expertise du domaine et a contribué significativement à la définition de la problématique, des besoins métier et des fonctionnalités à implémenter
- **Fonctionnalités avancées** : Développement de fonctionnalités métier avancées (comparaison de Pokémon, statistiques)
- **Tests de la couche REST** : Implémentation et exécution des tests pour la couche REST
- **Collaboration** : Participation active au développement collaboratif et aux sessions de peer programming


### Rahim Ouar

**Contributions principales :**

- **Rôle de chef de projet** : Rahim a assumé un rôle de coordination et d'organisation du projet, facilitant la communication entre les membres et la planification des tâches
- **Définition des entités JPA** : Modélisation et création des entités du domaine (Trainer, Pokemon, Type, CaughtPokemon) avec leurs relations
- **Requêtes avec Criteria Builder** : Refactorisation des requêtes JPA pour utiliser Criteria Builder au lieu de requêtes JPQL/QL, améliorant la maintenabilité et la flexibilité
- **Collaboration** : Participation active au développement collaboratif et aux sessions de peer programming

### Raphael Krattli

**Contributions principales :**

- **CRUD CaughtPokemon** : Implémentation complète du CRUD pour les captures de Pokémon (endpoints REST, service, persistance)
- **Collaboration** : Participation active au développement collaboratif et aux sessions de peer programming


## Conclusion

Ce projet a été réalisé dans un esprit de collaboration et d'entraide. Chaque membre a apporté ses compétences et connaissances au groupe, et le résultat final est le fruit d'un travail d'équipe où les frontières entre contributions individuelles sont souvent floues. Les commits Git reflètent principalement qui a effectué le commit final, mais ne représentent qu'une partie de la réalité du travail collaboratif effectué.
