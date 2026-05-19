# Application Web E-commerce / E-commerce Web Application
### Conçu avec une méthode d'ingénierie système MBSE / Built with Model-Based Engineering Method

![MBSE](https://img.shields.io/badge/Method-MBSE-blue)
![ARCADIA](https://img.shields.io/badge/Framework-ARCADIA-4A90D9)
![AsciiDoc](https://img.shields.io/badge/Docs-AsciiDoc-E40046)
![PlantUML](https://img.shields.io/badge/Diagrams-PlantUML-brightgreen)
![AI](https://img.shields.io/badge/Agentic-AI-8A2BE2)
![Engineering](https://img.shields.io/badge/Software-Engineering-orange)

## Table of Contents / Sommaire

<table border="0">
<tr>
<td valign="top">

### 🇫🇷 Français
- [Français](#français-)
- [1. Rédaction du document d'architecture](#1-rédaction-du-document-darchitecture)
- [2. Mise en place de l'infrastructure](#2-mise-en-place-de-linfrastructure-de-la-solution)
- [3. Développement itératif](#3-développement-des-cas-dutilisation-de-manière-itérative)
- [4. Documentation utilisateur](#4-rédaction-de-la-documentation-utilisateur)

</td>
<td valign="top">

### 🇬🇧 English
- [English](#english-)
- [1. Writing the Architecture Document](#1-writing-the-architecture-document)
- [2. Setting Up the Infrastructure](#2-setting-up-the-solution-infrastructure)
- [3. Iterative Development](#3-iterative-development-of-use-cases)
- [4. User Documentation](#4-writing-the-user-documentation)

</td>
</tr>
</table>

---

## Introduction 🇫🇷

Le projet a pour objectif de développer une application web de site e-commerce dans l'état de l'art actuel, aussi bien en terme d'ergonomie que de cybersécurité ([recommandations ANSSI](https://fr.wikipedia.org/wiki/Agence_nationale_de_la_s%C3%A9curit%C3%A9_des_syst%C3%A8mes_d%27information)), en suivant un processus d'ingénierie logiciel éprouvé. Il débute par la rédaction d'un [document d'architecture](architecture/Boutique%20en%20ligne%20-%20Documentation%20d%27Architecture.pdf) à partir des [exigences](architecture/Boutique%20en%20ligne%20-%20Exigences%20d%27utilisateurs.pdf) avant de passer au développement de la solution logicielle concrète.

La démarche adoptée est volontairement rigoureuse et structurée. Un site e-commerce n'est pas le cas d'usage le plus représentatif pour une méthode issue du génie logiciel industriel — mais c'est précisément ce qui en fait un exemple intéressant : démontrer que la démarche s'adapte à tout type de projet, y compris web. Elle présente également un avantage concret dans le contexte actuel : disposer d'une architecture documentée et structurée permet de cadrer efficacement la génération de code par une IA, en lui fournissant un contexte précis et des contraintes claires plutôt que de partir d'une page blanche. Elle s'appuie sur le principe fondamental que *l'architecture précède le code* : avant d'écrire la moindre ligne de code, le système est entièrement modélisé, documenté et validé. Cette approche, issue du génie logiciel industriel, permet de détecter les incohérences et les ambiguïtés en amont, là où elles coûtent le moins cher à corriger. Cette démarche peut très bien prendre en compte de nouvelles exigences en cours de développement, à condition qu'elles repassent par l'ensemble du processus de conception afin de maintenir la documentation d'architecture à jour.

L'architecture est modélisée selon la méthode [ARCADIA](https://fr.wikipedia.org/wiki/Arcadia_(ing%C3%A9nierie)) (MBSE - Model-Based System Engineering), sans recourir à des outils lourds tels qu'Enterprise Architect, Rhapsody ou Capella. La stack retenue est volontairement légère : AsciiDoc pour la rédaction du document d'architecture, PlantUML pour tous les diagrammes UML en notation textuelle versionnée avec le code, et Git pour la traçabilité. AsciiDoc et PlantUML étant des formats textuels, une IA agentique peut les lire, les analyser et les produire nativement — sans interface graphique, sans plugin, sans export. Concrètement, à partir du document des exigences, elle génère les quatre vues ARCADIA (OA, SA, LA, PA) avec leurs diagrammes UML, et propage tout changement d'exigence à travers toute la chaîne — architecture, diagrammes, code — sans rupture de traçabilité.

L'absence d'outil MBSE dédié est aussi une liberté : sans contrainte logicielle, la méthode peut être appliquée à la carte — en ne retenant que les niveaux d'analyse et les diagrammes réellement utiles au contexte du projet, plutôt que de suivre la démarche dans son intégralité par obligation.

Le processus global se déroule en quatre phases séquentielles et interdépendantes :

- **Rédaction du document d'architecture** — Le besoin utilisateur est analysé en profondeur selon la méthode [ARCADIA](https://fr.wikipedia.org/wiki/Arcadia_(ing%C3%A9nierie)) (méthode MBSE - Model-Based System Engineering), du contexte opérationnel jusqu'à l'architecture physique de déploiement. Ce document constitue la référence technique et fonctionnelle de tout le projet.
- **Mise en place de l'infrastructure** — Les environnements (développement, gestion de version, production) sont configurés et validés avant le début du développement fonctionnel. Aucune User Story n'est implémentée tant que la plateforme de livraison n'est pas opérationnelle.
- **Développement itératif des cas d'utilisation** — Les fonctionnalités sont implémentées une par une, chacune suivant un cycle complet d'analyse, de développement, de tests et d'intégration. Chaque itération s'appuie sur l'architecture définie et laisse le système dans un état stable et livrable.
- **Rédaction de la documentation utilisateur** — Une fois l'ensemble des fonctionnalités développées et validées, la documentation destinée aux utilisateurs finaux est produite sur la base de l'application telle qu'elle a été livrée.

### 1. Rédaction du document d'architecture

Le document d'architecture est rédigé en suivant la méthodologie ARCADIA (Architecture Analysis & Design Integrated Approach).

En voici les grandes phases d'analyse :

![ARCADIA](architecture/arcadia.png)

La méthode ARCADIA est suivie en s'appuyant uniquement sur le langage de modélisation UML. Le document est construit avec AsciiDoc, et les diagrammes avec la notation textuelle de PlantUML, puis générés sous forme visuelle dans le document final.

Le [document d'architecture](architecture/Boutique%20en%20ligne%20-%20Documentation%20d%27Architecture.pdf) est produit à partir du [**document des exigences utilisateurs**](architecture/Boutique%20en%20ligne%20-%20Exigences%20utilisateurs.pdf), qui constitue l'entrant formel de la démarche ARCADIA. Ce document recense les besoins opérationnels des acteurs (administrateur, vendeur, acheteur) ainsi que les exigences de sécurité issues des recommandations ANSSI, et sert de référence tout au long des quatre niveaux d'analyse.

> **Note :** La solution suit dans les grandes lignes les **recommandations de sécurité de l'[ANSSI](https://www.ssi.gouv.fr/)** (Agence nationale de la sécurité des systèmes d'information), notamment le guide [authentification multifacteur et mots de passe](https://messervices.cyber.gouv.fr/guides/recommandations-relatives-lauthentification-multifacteur-et-aux-mots-de-passe) et le guide [sécurité des sites web](https://cyber.gouv.fr/guide-sites-web). Ces recommandations sont déclinées sur les quatre niveaux ARCADIA de l'architecture — OA, SA, LA, PA — et implémentées dans le code : TLS 1.3, hachage BCrypt, protection contre la force brute, en-têtes HTTP de sécurité (CSP, HSTS, X-Frame-Options) et journalisation d'audit.

#### Analyse du besoin

##### 1.1. Analyse du Besoin Opérationnel (OA — Operational Analysis)

Que font les utilisateurs et pourquoi ?
On modélise ici les acteurs (opérateurs, systèmes externes), leurs activités opérationnelles et les échanges entre eux, indépendamment de tout système à construire. On décrit le contexte métier réel.

| Diagramme | Utilité dans l'OA |
|-----------|-------------------|
| **Diagramme de cas d'utilisation** | Identifie les acteurs et leurs objectifs opérationnels |
| **Diagramme d'activité** | Modélise les flux de travail opérationnels et les prises de décision |
| **Diagramme de séquence** | Représente les échanges entre acteurs dans le temps |
| **Diagramme de communication** | Met en évidence les interactions entre groupes d'acteurs |

**Exemple de diagramme OA — Cas d'utilisation opérationnel :**

![OA Use Case](architecture/diagrams/readme/oa-use-case.svg)

##### 1.2. Analyse du Besoin Système (SA — System Need Analysis)

Que doit faire le système pour satisfaire ce besoin ?
On définit les fonctions du système vues de l'extérieur (ce qu'il doit accomplir), les interfaces avec les acteurs, et les scénarios d'usage. On reste volontairement agnostique sur l'architecture interne. Les exigences sont déduites de cette analyse fonctionnelle, et non l'inverse.

| Diagramme | Utilité dans la SA |
|-----------|-------------------|
| **Diagramme de cas d'utilisation** | Définit les fonctions du système exposées aux acteurs |
| **Diagramme de séquence** | Décrit les scénarios d'usage (flux nominal et alternatifs) |
| **Diagramme d'états-transitions** | Modélise les états du système vus de l'extérieur (ex. cycle de vie d'une commande) |
| **Diagramme d'activité** | Détaille les flux fonctionnels déclenchés par les acteurs |
| **Diagramme de contexte** *(frontière système)* | Représente le système comme une boîte noire avec toutes ses interfaces |

**Exemple de diagramme SA — Contexte système :**

![SA Context](architecture/diagrams/readme/sa-context.svg)

#### Analyse du système

##### 1.3. Architecture Logique (LA — Logical Architecture)

Comment organiser les fonctions en composants logiques ?
On décompose le système en composants logiques (sans préjuger des technologies) et on alloue les fonctions à ces composants. On travaille les flux d'échanges internes, la cohérence du découpage, et on identifie les premiers choix d'architecture. C'est le cœur de la conception système.

| Diagramme | Utilité dans la LA |
|-----------|-------------------|
| **Diagramme de composants** | Décompose le système en composants logiques et leurs interfaces |
| **Diagramme de séquence** | Représente les échanges internes entre composants logiques |
| **Diagramme de classes** | Modélise les structures de données logiques et leurs relations |
| **Diagramme d'activité** | Alloue les flux fonctionnels aux composants logiques |
| **Diagramme d'états-transitions** | Modélise le cycle de vie interne des entités clés (Commande, Panier, Paiement…) |
| **Diagramme de structure composite** | Représente les ports internes et connecteurs entre composants |

**Exemple de diagramme LA — Architecture logique des composants :**

![LA Components](architecture/diagrams/readme/la-components.svg)

##### 1.4. Architecture Physique (PA — Physical Architecture)

Quelles solutions concrètes implémentent l'architecture logique ?
On mappe les composants logiques sur des composants physiques réels (matériel, logiciel, réseaux). On gère les contraintes de déploiement, de performance, de redondance. On prépare la répartition du travail entre les parties prenantes.

| Diagramme | Utilité dans la PA |
|-----------|-------------------|
| **Diagramme de déploiement** | Mappe les composants sur des nœuds physiques (serveurs, conteneurs, cloud…) |
| **Diagramme de composants** | Représente les modules physiques, bibliothèques et leurs dépendances |
| **Diagramme de paquetages** | Organise le code en unités déployables |
| **Diagramme de séquence** | Valide les flux techniques de bout en bout (ex. paiement avec Stripe) |
| **Diagramme réseau** *(notation nœuds UML)* | Décrit la topologie d'infrastructure et les protocoles de communication |

**Exemple de diagramme PA — Architecture de déploiement :**

![PA Deployment](architecture/diagrams/readme/pa-deployment.svg)

### 2. Mise en place de l'infrastructure de la solution

Une fois la solution documentée en détail, l'infrastructure de l'application est développée et déployée dans un environnement de test.

Cette phase comprend la configuration des environnements (développement, staging, production), la mise en place des outils CI/CD, ainsi que la définition et le provisionnement des ressources nécessaires (serveurs, bases de données, services cloud, etc.). L'infrastructure est validée par une série de tests techniques visant à garantir la stabilité, la disponibilité et la sécurité de la plateforme avant le début du développement fonctionnel.

### 3. Développement des cas d'utilisation de manière itérative

Les [cas d'utilisation](architecture/05-backlog/us.adoc) sont implémentés de manière itérative, l'un après l'autre, en suivant un cycle de développement structuré et reproductible.

Pour chaque User Story, le cycle suivant est appliqué dans son intégralité :

- **Analyse & conception** — prise en compte des exigences fonctionnelles définies dans le document d'architecture ; en cas de doute ou d'incohérence, mise au point avec l'utilisateur avant de poursuivre.
- **Développement** — implémentation du code source en respectant l'architecture définie.
- **Tests unitaires & d'intégration** — vérification du bon fonctionnement de chaque composant, isolément puis en interaction avec le reste du système.
- **Tests end-to-end (E2E)** — validation du parcours utilisateur complet, de bout en bout, afin de s'assurer que la fonctionnalité répond aux critères d'acceptation définis.
- **Revue & intégration** — validation finale et fusion dans la branche principale, avant de passer à la User Story suivante.

Cette approche garantit une livraison progressive et maîtrisée, tout en limitant les risques de régression au fil des itérations.

### 4. Rédaction de la documentation utilisateur

Une fois l'ensemble des cas d'utilisation développés et validés, la documentation destinée aux utilisateurs finaux est produite sur la base des fonctionnalités implémentées.

Cette documentation comprend notamment :

- un guide utilisateur décrivant les principales fonctionnalités du site e-commerce (navigation, recherche de produits, gestion du panier, tunnel d'achat, suivi de commande, etc.) ;
- des guides pas à pas illustrant les parcours utilisateurs clés ;
- le cas échéant, une documentation administrateur à destination des gestionnaires de la boutique (gestion du catalogue, des commandes, des utilisateurs, etc.).

L'objectif est de fournir une documentation claire, accessible et maintenable, cohérente avec l'état final de l'application livrée.

---

<a name="english"></a>

## Introduction 🇬🇧

The project aims to develop an e-commerce web application built to current best practices, in terms of both user experience and cybersecurity ([ANSSI recommendations](https://en.wikipedia.org/wiki/Agence_nationale_de_la_s%C3%A9curit%C3%A9_des_syst%C3%A8mes_d%27information)), following a proven software engineering process. It begins with the writing of an [architecture document](architecture/Boutique%20en%20ligne%20-%20Documentation%20d%27Architecture.pdf) from the [requirements](architecture/Boutique%20en%20ligne%20-%20Exigences%20d%27utilisateurs.pdf), before moving on to the concrete development of the software solution.

The approach is deliberately rigorous and structured. An e-commerce website is not the most representative use case for a method rooted in industrial software engineering — but that is precisely what makes it an interesting example: demonstrating that the approach adapts to any type of project, including web applications. It also offers a concrete advantage in today's context: having a documented and structured architecture makes it possible to effectively frame AI-assisted code generation, providing precise context and clear constraints rather than starting from a blank page. It rests on the fundamental principle that *architecture precedes code*: before a single line of code is written, the system is fully modelled, documented, and validated. This approach, rooted in industrial software engineering, makes it possible to detect inconsistencies and ambiguities early — where they are least costly to fix. This approach can very well accommodate new requirements arising during development, provided they go through the full design process in order to keep the architecture documentation up to date.

The architecture is modelled using the [ARCADIA](https://en.wikipedia.org/wiki/Arcadia_(methodology)) method (MBSE - Model-Based System Engineering), without resorting to heavy tools such as Enterprise Architect, Rhapsody or Capella. The chosen stack is deliberately lightweight: AsciiDoc for writing the architecture document, PlantUML for all UML diagrams in text-based notation versioned alongside the code, and Git for traceability. Since AsciiDoc and PlantUML are text-based formats, an agentic AI can read, analyse and produce them natively — no graphical interface, no plugin, no export. In practice, from the requirements document, it generates all four ARCADIA views (OA, SA, LA, PA) with their UML diagrams, and propagates any requirement change across the entire chain — architecture, diagrams, code — with no break in traceability.

The absence of a dedicated MBSE tool is also a freedom: without software constraints, the method can be applied selectively — retaining only the analysis levels and diagrams that are genuinely useful for the project's context, rather than following the full process out of obligation.

The overall process unfolds across four sequential, interdependent phases:

- **Writing the architecture document** — The user requirements are analysed in depth using the [ARCADIA](https://en.wikipedia.org/wiki/Arcadia_(methodology)) method (MBSE - Model-Based System Engineering), from the operational context down to the physical deployment architecture. This document serves as the technical and functional reference for the entire project.
- **Setting up the infrastructure** — The environments (development, version control, production) and CI/CD tooling are configured and validated before any functional development begins. No User Story is implemented until the delivery platform is fully operational.
- **Iterative development of use cases** — Features are implemented one by one, each following a complete cycle of analysis, development, testing, and integration. Every iteration builds on the defined architecture and leaves the system in a stable, deliverable state.
- **Writing the user documentation** — Once all features have been developed and validated, end-user documentation is produced based on the application as it has been delivered.

### 1. Writing the Architecture Document

The architecture document is written following the ARCADIA methodology (Architecture Analysis & Design Integrated Approach).

![ARCADIA](architecture/arcadia.png)

The ARCADIA method is followed using exclusively UML modelling language. The document is written in AsciiDoc, and diagrams are expressed using PlantUML textual notation, then rendered visually in the final document.

The [architecture document](architecture/Boutique%20en%20ligne%20-%20Documentation%20d%27Architecture.pdf) is produced from the [**user requirements document**](architecture/Boutique%20en%20ligne%20-%20Exigences%20utilisateurs.pdf), which constitutes the formal input to the ARCADIA process. This document captures the operational needs of all actors (administrator, vendor, buyer) as well as the security requirements derived from ANSSI recommendations, and serves as the reference throughout all four levels of analysis.

> **Note:** The solution broadly follows **[ANSSI](https://www.ssi.gouv.fr/en/) security recommendations** (French national cybersecurity agency), including the guide on [multi-factor authentication and passwords](https://messervices.cyber.gouv.fr/guides/recommandations-relatives-lauthentification-multifacteur-et-aux-mots-de-passe) and the [web security guide](https://cyber.gouv.fr/guide-sites-web). These recommendations are traced across all four ARCADIA levels — OA, SA, LA, PA — and implemented in the code: TLS 1.3, BCrypt hashing, brute-force protection, HTTP security headers (CSP, HSTS, X-Frame-Options), and audit logging.

#### Needs Analysis

##### 1.1. Operational Analysis (OA)

What do users do, and why?
This phase models the actors (operators, external systems), their operational activities, and the exchanges between them, independently of any system to be built. It describes the real business context.

| Diagram | Purpose in OA |
|---------|---------------|
| **Use Case Diagram** | Identifies actors and their operational goals |
| **Activity Diagram** | Models operational workflows and decision-making |
| **Sequence Diagram** | Represents timed exchanges between actors |
| **Communication Diagram** | Highlights interactions between groups of actors |

**OA Diagram Example — Operational Use Case:**

![OA Use Case](architecture/diagrams/readme/oa-use-case.svg)

##### 1.2. System Need Analysis (SA)

What must the system do to satisfy this need?
This phase defines the system's functions as seen from the outside (what it must accomplish), its interfaces with actors, and the usage scenarios. The internal architecture is deliberately left unspecified at this stage. Requirements are derived from this functional analysis — not the other way around.

| Diagram | Purpose in SA |
|---------|---------------|
| **Use Case Diagram** | Defines the system functions exposed to actors |
| **Sequence Diagram** | Describes usage scenarios (nominal and alternative flows) |
| **State Machine Diagram** | Models system states as seen from the outside (e.g. order lifecycle) |
| **Activity Diagram** | Details functional flows triggered by actors |
| **Context Diagram** *(system boundary)* | Represents the system as a black box with all its interfaces |

**SA Diagram Example — System Context:**

![SA Context](architecture/diagrams/readme/sa-context.svg)

#### System Analysis

##### 1.3. Logical Architecture (LA)

How should functions be organised into logical components?
The system is broken down into logical components (without assuming any specific technology) and functions are allocated to those components. Internal exchange flows are defined, the decomposition is checked for consistency, and initial architectural decisions are made. This is the core of system design.

| Diagram | Purpose in LA |
|---------|---------------|
| **Component Diagram** | Decomposes the system into logical components and their interfaces |
| **Sequence Diagram** | Represents internal exchanges between logical components |
| **Class Diagram** | Models logical data structures and their relationships |
| **Activity Diagram** | Allocates functional flows to logical components |
| **State Machine Diagram** | Models the internal lifecycle of key entities (Order, Cart, Payment…) |
| **Composite Structure Diagram** | Represents internal ports and connectors between components |

**LA Diagram Example — Logical Component Architecture:**

![LA Components](architecture/diagrams/readme/la-components.svg)

##### 1.4. Physical Architecture (PA)

Which concrete solutions implement the logical architecture?
Logical components are mapped onto real physical components (hardware, software, networks). Deployment, performance, and redundancy constraints are addressed, and the division of work between stakeholders is defined.

| Diagram | Purpose in PA |
|---------|---------------|
| **Deployment Diagram** | Maps components onto physical nodes (servers, containers, cloud…) |
| **Component Diagram** | Represents physical modules, libraries, and their dependencies |
| **Package Diagram** | Organises code into deployable units |
| **Sequence Diagram** | Validates end-to-end technical flows (e.g. payment with Stripe) |
| **Network Diagram** *(UML node notation)* | Describes infrastructure topology and communication protocols |

**PA Diagram Example — Deployment Architecture:**

![PA Deployment](architecture/diagrams/readme/pa-deployment.svg)

### 2. Setting Up the Solution Infrastructure

Once the solution has been documented in detail, the application infrastructure is built and deployed in a test environment.

This phase covers the configuration of environments (development, staging, production), the setup of CI/CD tooling, and the provisioning of the required resources (servers, databases, cloud services, etc.). The infrastructure is validated through a series of technical tests to ensure the platform's stability, availability, and security prior to the start of functional development.

### 3. Iterative Development of Use Cases

[Use cases](architecture/05-backlog/us.adoc) are implemented one by one in an iterative manner, following a structured and repeatable development cycle.

For each User Story, the following cycle is applied in full:

- **Analysis & Design** — review of the functional requirements defined in the architecture document; any ambiguity or inconsistency is clarified with the stakeholder before proceeding.
- **Development** — implementation of the source code in compliance with the defined architecture.
- **Unit & Integration Testing** — verification that each component functions correctly in isolation and in interaction with the rest of the system.
- **End-to-End (E2E) Testing** — full validation of the user journey from start to finish, ensuring the feature meets the defined acceptance criteria.
- **Review & Integration** — final validation and merge into the main branch, before moving on to the next User Story.

This approach ensures progressive, controlled delivery while minimising the risk of regression across iterations.

### 4. Writing the User Documentation

Once all use cases have been developed and validated, end-user documentation is produced based on the implemented features.

This documentation includes in particular:

- a user guide describing the main features of the e-commerce website (browsing, product search, cart management, checkout flow, order tracking, etc.);
- step-by-step guides illustrating key user journeys;
- where applicable, an administrator guide intended for shop managers (catalogue management, order processing, user administration, etc.).

The goal is to deliver documentation that is clear, accessible, and maintainable, consistent with the final state of the delivered application.
