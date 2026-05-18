= Application Web de boutique en ligne / Online Shop Web App
:toc: left
:toc-title: Table des matières / Table of Contents
:toclevels: 3
:sectnums:
:icons: font

== 🇫🇷 Français

Le projet a pour objectif de développer une application web de boutique en ligne dans l'état de l'art actuel, aussi bien en terme d'ergonomie que de cybersécurité. Il débute par la rédaction d'un link:architecture/Boutique%20en%20ligne%20-%20Documentation%20d%27Architecture.pdf[document d'architecture] à partir du document des exigences, avant de passer au développement de la solution logicielle concrète.

La démarche adoptée est volontairement rigoureuse et structurée. Elle s'appuie sur le principe fondamental que *l'architecture précède le code* : avant d'écrire la moindre ligne de code, le système est entièrement modélisé, documenté et validé. Cette approche, issue du génie logiciel industriel, permet de détecter les incohérences et les ambiguïtés en amont, là où elles coûtent le moins cher à corriger.

Le processus global se déroule en quatre phases séquentielles et interdépendantes :

Rédaction du document d'architecture:: Le besoin est analysé en profondeur selon la méthode MBSE ARCADIA, du contexte opérationnel jusqu'à l'architecture physique de déploiement. Ce document constitue la référence technique et fonctionnelle de tout le projet.
Mise en place de l'infrastructure:: Les environnements (développement, staging, production) et les outils CI/CD sont configurés et validés avant le début du développement fonctionnel. Aucune User Story n'est implémentée tant que la plateforme de livraison n'est pas opérationnelle.
Développement itératif des cas d'utilisation:: Les fonctionnalités sont implémentées une par une, chacune suivant un cycle complet d'analyse, de développement, de tests et d'intégration. Chaque itération s'appuie sur l'architecture définie et laisse le système dans un état stable et livrable.
Rédaction de la documentation utilisateur:: Une fois l'ensemble des fonctionnalités développées et validées, la documentation destinée aux utilisateurs finaux est produite sur la base de l'application telle qu'elle a été livrée.

=== Rédaction du document d'architecture

Le document d'architecture est rédigé en suivant la méthodologie ARCADIA (Architecture Analysis & Design Integrated Approach), une méthode MBSE (Model-Based System Engineering).

En voici les grandes phases d'analyse :

image::architecture/arcadia.png[ARCADIA]

La méthode ARCADIA est appliquée en s'appuyant exclusivement sur le langage de modélisation UML. Le document est construit avec AsciiDoc, et les diagrammes sont exprimés avec la notation textuelle de PlantUML, puis générés sous forme visuelle dans le document final.

Le link:architecture/Boutique%20en%20ligne%20-%20Documentation%20d%27Architecture.pdf[document d'architecture] est produit à partir du link:architecture/Boutique%20en%20ligne%20-%20Exigences%20utilisateurs.pdf[**document des exigences utilisateurs**], qui constitue l'entrant formel de la démarche ARCADIA. Ce document recense les besoins opérationnels des acteurs (administrateur, vendeur, acheteur) ainsi que les exigences de sécurité issues des recommandations ANSSI, et sert de référence tout au long des quatre niveaux d'analyse.

NOTE: La solution respecte les **recommandations de sécurité de l'link:https://www.ssi.gouv.fr/[ANSSI]** (Agence nationale de la sécurité des systèmes d'information), notamment le guide link:https://messervices.cyber.gouv.fr/guides/recommandations-relatives-lauthentification-multifacteur-et-aux-mots-de-passe[authentification multifacteur et mots de passe]. Ces recommandations sont déclinées sur les quatre niveaux ARCADIA — OA, SA, LA, PA — et implémentées dans le code : TLS 1.3, hachage BCrypt, protection contre la force brute, en-têtes HTTP de sécurité (CSP, HSTS, X-Frame-Options) et journalisation d'audit.

==== Analyse du besoin

===== Analyse du Besoin Opérationnel (OA — Operational Analysis)

Que font les utilisateurs et pourquoi ?
On modélise ici les acteurs (opérateurs, systèmes externes), leurs activités opérationnelles et les échanges entre eux, indépendamment de tout système à construire. On décrit le contexte métier réel.

[cols="1,2", options="header"]
|===
| Diagramme | Utilité dans l'OA

| Diagramme de cas d'utilisation
| Identifie les acteurs et leurs objectifs opérationnels

| Diagramme d'activité
| Modélise les flux de travail opérationnels et les prises de décision

| Diagramme de séquence
| Représente les échanges entre acteurs dans le temps

| Diagramme de communication
| Met en évidence les interactions entre groupes d'acteurs
|===

.Exemple de diagramme OA — Cas d'utilisation opérationnel
image::architecture/diagrams/readme/oa-use-case.svg[OA Use Case]

===== Analyse du Besoin Système (SA — System Need Analysis)

Que doit faire le système pour satisfaire ce besoin ?
On définit les fonctions du système vues de l'extérieur (ce qu'il doit accomplir), les interfaces avec les acteurs, et les scénarios d'usage. On reste volontairement agnostique sur l'architecture interne. Les exigences sont déduites de cette analyse fonctionnelle, et non l'inverse.

[cols="1,2", options="header"]
|===
| Diagramme | Utilité dans la SA

| Diagramme de cas d'utilisation
| Définit les fonctions du système exposées aux acteurs

| Diagramme de séquence
| Décrit les scénarios d'usage (flux nominal et alternatifs)

| Diagramme d'états-transitions
| Modélise les états du système vus de l'extérieur (ex. cycle de vie d'une commande)

| Diagramme d'activité
| Détaille les flux fonctionnels déclenchés par les acteurs

| Diagramme de contexte _(frontière système)_
| Représente le système comme une boîte noire avec toutes ses interfaces
|===

.Exemple de diagramme SA — Contexte système
image::architecture/diagrams/readme/sa-context.svg[SA Context]

==== Analyse du système

===== Architecture Logique (LA — Logical Architecture)

Comment organiser les fonctions en composants logiques ?
On décompose le système en composants logiques (sans préjuger des technologies) et on alloue les fonctions à ces composants. On travaille les flux d'échanges internes, la cohérence du découpage, et on identifie les premiers choix d'architecture. C'est le cœur de la conception système.

[cols="1,2", options="header"]
|===
| Diagramme | Utilité dans la LA

| Diagramme de composants
| Décompose le système en composants logiques et leurs interfaces

| Diagramme de séquence
| Représente les échanges internes entre composants logiques

| Diagramme de classes
| Modélise les structures de données logiques et leurs relations

| Diagramme d'activité
| Alloue les flux fonctionnels aux composants logiques

| Diagramme d'états-transitions
| Modélise le cycle de vie interne des entités clés (Commande, Panier, Paiement…)

| Diagramme de structure composite
| Représente les ports internes et connecteurs entre composants
|===

.Exemple de diagramme LA — Architecture logique des composants
image::architecture/diagrams/readme/la-components.svg[LA Components]

===== Architecture Physique (PA — Physical Architecture)

Quelles solutions concrètes implémentent l'architecture logique ?
On mappe les composants logiques sur des composants physiques réels (matériel, logiciel, réseaux). On traite les contraintes de déploiement, de performance et de redondance, et on prépare la répartition du travail entre les parties prenantes.

[cols="1,2", options="header"]
|===
| Diagramme | Utilité dans la PA

| Diagramme de déploiement
| Mappe les composants sur des nœuds physiques (serveurs, conteneurs, cloud…)

| Diagramme de composants
| Représente les modules physiques, bibliothèques et leurs dépendances

| Diagramme de paquetages
| Organise le code en unités déployables

| Diagramme de séquence
| Valide les flux techniques de bout en bout (ex. paiement avec Stripe)

| Diagramme réseau _(notation nœuds UML)_
| Décrit la topologie d'infrastructure et les protocoles de communication
|===

.Exemple de diagramme PA — Architecture de déploiement
image::architecture/diagrams/readme/pa-deployment.svg[PA Deployment]

=== Mise en place de l'infrastructure de la solution

Une fois la solution documentée en détail, l'infrastructure de l'application est développée et déployée dans un environnement de test.

Cette phase comprend la configuration des environnements (développement, staging, production), la mise en place des outils CI/CD, ainsi que la définition et le provisionnement des ressources nécessaires (serveurs, bases de données, services cloud, etc.). L'infrastructure est validée par une série de tests techniques visant à garantir la stabilité, la disponibilité et la sécurité de la plateforme avant le début du développement fonctionnel.

=== Développement des cas d'utilisation de manière itérative

Les link:architecture/05-backlog/us.adoc[cas d'utilisation] sont implémentés de manière itérative, l'un après l'autre, en suivant un cycle de développement structuré et reproductible.

Pour chaque User Story, le cycle suivant est appliqué dans son intégralité :

Analyse & conception:: Prise en compte des exigences fonctionnelles définies dans le document d'architecture ; en cas de doute ou d'incohérence, mise au point avec l'utilisateur avant de poursuivre.
Développement:: Implémentation du code source en respectant l'architecture définie.
Tests unitaires & d'intégration:: Vérification du bon fonctionnement de chaque composant, isolément puis en interaction avec le reste du système.
Tests end-to-end (E2E):: Validation du parcours utilisateur complet, de bout en bout, afin de s'assurer que la fonctionnalité répond aux critères d'acceptation définis.
Revue & intégration:: Validation finale et fusion dans la branche principale, avant de passer à la User Story suivante.

Cette approche garantit une livraison progressive et maîtrisée, tout en limitant les risques de régression au fil des itérations.

=== Rédaction de la documentation utilisateur

Une fois l'ensemble des cas d'utilisation développés et validés, la documentation destinée aux utilisateurs finaux est produite sur la base des fonctionnalités implémentées.

Cette documentation comprend notamment :

* un guide utilisateur décrivant les principales fonctionnalités de la boutique en ligne (navigation, recherche de produits, gestion du panier, tunnel d'achat, suivi de commande, etc.) ;
* des guides pas à pas illustrant les parcours utilisateurs clés ;
* le cas échéant, une documentation administrateur à destination des gestionnaires de la boutique (gestion du catalogue, des commandes, des utilisateurs, etc.).

L'objectif est de fournir une documentation claire, accessible et maintenable, cohérente avec l'état final de l'application livrée.

'''

== 🇬🇧 English

The project aims to develop an online shop web application. It begins with the writing of an link:architecture/Boutique%20en%20ligne%20-%20Documentation%20d%27Architecture.pdf[architecture document], before moving on to the concrete development of the software solution.

The approach is deliberately rigorous and structured. It rests on the fundamental principle that *architecture precedes code*: before a single line of code is written, the system is fully modelled, documented, and validated. This approach, rooted in industrial software engineering, makes it possible to detect inconsistencies and ambiguities early — where they are least costly to fix.

The overall process unfolds across four sequential, interdependent phases:

Writing the architecture document:: The requirements are analysed in depth using the ARCADIA MBSE method, from the operational context down to the physical deployment architecture. This document serves as the technical and functional reference for the entire project.
Setting up the infrastructure:: The environments (development, staging, production) and CI/CD tooling are configured and validated before any functional development begins. No User Story is implemented until the delivery platform is fully operational.
Iterative development of use cases:: Features are implemented one by one, each following a complete cycle of analysis, development, testing, and integration. Every iteration builds on the defined architecture and leaves the system in a stable, deliverable state.
Writing the user documentation:: Once all features have been developed and validated, end-user documentation is produced based on the application as it has been delivered.

=== Writing the Architecture Document

The architecture document is written following the ARCADIA methodology (Architecture Analysis & Design Integrated Approach), a MBSE (Model-Based System Engineering) method.

image::architecture/arcadia.png[ARCADIA]

The ARCADIA method is applied using exclusively UML modelling language. The document is written in AsciiDoc, and diagrams are expressed using PlantUML textual notation, then rendered visually in the final document.

The link:architecture/Boutique%20en%20ligne%20-%20Documentation%20d%27Architecture.pdf[architecture document] is produced from the link:architecture/Boutique%20en%20ligne%20-%20Exigences%20utilisateurs.pdf[**user requirements document**], which constitutes the formal input to the ARCADIA process. This document captures the operational needs of all actors (administrator, vendor, buyer) as well as the security requirements derived from ANSSI recommendations, and serves as the reference throughout all four levels of analysis.

NOTE: The solution complies with link:https://www.ssi.gouv.fr/en/[ANSSI] security recommendations (French national cybersecurity agency), including the guide on link:https://messervices.cyber.gouv.fr/guides/recommandations-relatives-lauthentification-multifacteur-et-aux-mots-de-passe[multi-factor authentication and passwords]. These recommendations are traced across all four ARCADIA levels — OA, SA, LA, PA — and implemented in the code: TLS 1.3, BCrypt hashing, brute-force protection, HTTP security headers (CSP, HSTS, X-Frame-Options), and audit logging.

==== Needs Analysis

===== Operational Analysis (OA)

What do users do, and why?
This phase models the actors (operators, external systems), their operational activities, and the exchanges between them, independently of any system to be built. It describes the real business context.

[cols="1,2", options="header"]
|===
| Diagram | Purpose in OA

| Use Case Diagram
| Identifies actors and their operational goals

| Activity Diagram
| Models operational workflows and decision-making

| Sequence Diagram
| Represents timed exchanges between actors

| Communication Diagram
| Highlights interactions between groups of actors
|===

.OA Diagram Example — Operational Use Case
image::architecture/diagrams/readme/oa-use-case.svg[OA Use Case]

===== System Need Analysis (SA)

What must the system do to satisfy this need?
This phase defines the system's functions as seen from the outside (what it must accomplish), its interfaces with actors, and the usage scenarios. The internal architecture is deliberately left unspecified at this stage. Requirements are derived from this functional analysis — not the other way around.

[cols="1,2", options="header"]
|===
| Diagram | Purpose in SA

| Use Case Diagram
| Defines the system functions exposed to actors

| Sequence Diagram
| Describes usage scenarios (nominal and alternative flows)

| State Machine Diagram
| Models system states as seen from the outside (e.g. order lifecycle)

| Activity Diagram
| Details functional flows triggered by actors

| Context Diagram _(system boundary)_
| Represents the system as a black box with all its interfaces
|===

.SA Diagram Example — System Context
image::architecture/diagrams/readme/sa-context.svg[SA Context]

==== System Analysis

===== Logical Architecture (LA)

How should functions be organised into logical components?
The system is broken down into logical components (without assuming any specific technology) and functions are allocated to those components. Internal exchange flows are defined, the decomposition is checked for consistency, and initial architectural decisions are made. This is the core of system design.

[cols="1,2", options="header"]
|===
| Diagram | Purpose in LA

| Component Diagram
| Decomposes the system into logical components and their interfaces

| Sequence Diagram
| Represents internal exchanges between logical components

| Class Diagram
| Models logical data structures and their relationships

| Activity Diagram
| Allocates functional flows to logical components

| State Machine Diagram
| Models the internal lifecycle of key entities (Order, Cart, Payment…)

| Composite Structure Diagram
| Represents internal ports and connectors between components
|===

.LA Diagram Example — Logical Component Architecture
image::architecture/diagrams/readme/la-components.svg[LA Components]

===== Physical Architecture (PA)

Which concrete solutions implement the logical architecture?
Logical components are mapped onto real physical components (hardware, software, networks). Deployment, performance, and redundancy constraints are addressed, and the division of work between stakeholders is defined.

[cols="1,2", options="header"]
|===
| Diagram | Purpose in PA

| Deployment Diagram
| Maps components onto physical nodes (servers, containers, cloud…)

| Component Diagram
| Represents physical modules, libraries, and their dependencies

| Package Diagram
| Organises code into deployable units

| Sequence Diagram
| Validates end-to-end technical flows (e.g. payment with Stripe)

| Network Diagram _(UML node notation)_
| Describes infrastructure topology and communication protocols
|===

.PA Diagram Example — Deployment Architecture
image::architecture/diagrams/readme/pa-deployment.svg[PA Deployment]

=== Setting Up the Solution Infrastructure

Once the solution has been documented in detail, the application infrastructure is built and deployed in a test environment.

This phase covers the configuration of environments (development, staging, production), the setup of CI/CD tooling, and the provisioning of the required resources (servers, databases, cloud services, etc.). The infrastructure is validated through a series of technical tests to ensure the platform's stability, availability, and security prior to the start of functional development.

=== Iterative Development of Use Cases

link:architecture/05-backlog/us.adoc[Use cases] are implemented one by one in an iterative manner, following a structured and repeatable development cycle.

For each User Story, the following cycle is applied in full:

Analysis & Design:: Review of the functional requirements defined in the architecture document; any ambiguity or inconsistency is clarified with the stakeholder before proceeding.
Development:: Implementation of the source code in compliance with the defined architecture.
Unit & Integration Testing:: Verification that each component functions correctly in isolation and in interaction with the rest of the system.
End-to-End (E2E) Testing:: Full validation of the user journey from start to finish, ensuring the feature meets the defined acceptance criteria.
Review & Integration:: Final validation and merge into the main branch, before moving on to the next User Story.

This approach ensures progressive, controlled delivery while minimising the risk of regression across iterations.

=== Writing the User Documentation

Once all use cases have been developed and validated, end-user documentation is produced based on the implemented features.

This documentation includes in particular:

* a user guide describing the main features of the online shop (browsing, product search, cart management, checkout flow, order tracking, etc.);
* step-by-step guides illustrating key user journeys;
* where applicable, an administrator guide intended for shop managers (catalogue management, order processing, user administration, etc.).

The goal is to deliver documentation that is clear, accessible, and maintainable, consistent with the final state of the delivered application.
