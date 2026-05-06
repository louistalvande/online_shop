
# Application Web de boutique en ligne / Online Shop web app

## 🇫🇷 Français
Le projet consiste à créer une application web de boutique en ligne, en commençant par rédiger un document d'architecture et en s'aidant d'une IA pour générer les livrables. 
L'execution du projet va se faire en plusieurs grandes étapes :

### I. Génération du document d'architecture avec la méthodologie MBSE ARCADIA
La méthode ARCADIA sera suivie en s'appuyant uniquement sur le language de modélisation UML. Le document sera manipulé avec Asciidoc, et les diagrammes seront manipulés avec la notation textuelle de PlantUML, puis générés sous forme visuelle dans le document final. 

#### Analyse du besoin
##### 1. Analyse du Besoin Opérationnel (OA — Operational Analysis)
Que font les utilisateurs et pour quoi ?
On modélise ici les acteurs (opérateurs, systèmes externes), leurs activités opérationnelles et les échanges entre eux, indépendamment de tout système à construire. On décrit le contexte métier réel.

| Diagramme | Utilité dans l'OA |
|---|---|
| **Diagramme de cas d'utilisation** | Identifie les acteurs et leurs objectifs opérationnels |
| **Diagramme d'activité** | Modélise les flux de travail opérationnels et les prises de décision |
| **Diagramme de séquence** | Représente les échanges entre acteurs dans le temps |
| **Diagramme de communication** | Met en évidence les interactions entre groupes d'acteurs |


##### 2. Analyse du Besoin Système (SA — System Need Analysis)
Que doit faire le système pour satisfaire ce besoin ?
On définit les fonctions du système vues de l'extérieur (ce qu'il doit accomplir), les interfaces avec les acteurs, et les scénarios d'usage. On reste volontairement agnostique sur l'architecture interne. Les exigences sont déduites de cette analyse fonctionnelle, pas l'inverse.

| Diagramme | Utilité dans la SA |
|---|---|
| **Diagramme de cas d'utilisation** | Définit les fonctions du système exposées aux acteurs |
| **Diagramme de séquence** | Décrit les scénarios d'usage (flux nominal et alternatifs) |
| **Diagramme d'états-transitions** | Modélise les états du système vus de l'extérieur (ex. cycle de vie d'une commande) |
| **Diagramme d'activité** | Détaille les flux fonctionnels déclenchés par les acteurs |
| **Diagramme de contexte** *(frontière système)* | Représente le système comme une boîte noire avec toutes ses interfaces |


#### Analyse du système

##### 3. Architecture Logique (LA — Logical Architecture)
Comment organiser les fonctions en composants logiques ?
On décompose le système en composants logiques (sans préjuger des technologies) et on alloue les fonctions à ces composants. On travaille les flux d'échanges internes, la cohérence du découpage, et on identifie les premiers choix d'architecture. C'est le cœur de la conception système.

| Diagramme | Utilité dans la LA |
|---|---|
| **Diagramme de composants** | Décompose le système en composants logiques et leurs interfaces |
| **Diagramme de séquence** | Représente les échanges internes entre composants logiques |
| **Diagramme de classes** | Modélise les structures de données logiques et leurs relations |
| **Diagramme d'activité** | Alloue les flux fonctionnels aux composants logiques |
| **Diagramme d'états-transitions** | Modélise le cycle de vie interne des entités clés (Commande, Panier, Paiement…) |
| **Diagramme de structure composite** | Représente les ports internes et connecteurs entre composants |


##### 4. Architecture Physique (PA — Physical Architecture)
Quelles solutions concrètes implémentent l'architecture logique ?
On mappe les composants logiques sur des composants physiques réels (matériel, logiciel, réseaux). On gère les contraintes de déploiement, de performance, de redondance. On prépare la répartition du travail entre les parties-prenantes.

| Diagramme | Utilité dans la PA |
|---|---|
| **Diagramme de déploiement** | Mappe les composants sur des nœuds physiques (serveurs, conteneurs, cloud…) |
| **Diagramme de composants** | Représente les modules physiques, bibliothèques et leurs dépendances |
| **Diagramme de paquetages** | Organise le code en unités déployables |
| **Diagramme de séquence** | Valide les flux techniques de bout en bout (ex. paiement avec Stripe) |
| **Diagramme réseau** *(notation nœuds UML)* | Décrit la topologie d'infrastructure et les protocoles de communication |


### II. Génération de la solution

### III. Génération des tests end2end

### IIII. Génération de la documentation utilisateur

## 🇬🇧 English
The project consists of creating an online shop web application, starting by writing an architecture document and using AI to generate the deliverables.
The project will be carried out in several major stages:
Generating the architecture document using the MBSE ARCADIA methodology
Diagrams will be handled using PlantUML textual notation, then rendered as visual outputs.

#### Needs Analysis

##### 1. Operational Analysis (OA)
What do users do and why?
We model here the actors (operators, external systems), their operational activities and the exchanges between them, independently of any system to be built. We describe the real business context. This is a step often overlooked in classical methodologies, yet critical.

##### 2. System Need Analysis (SA)
What must the system do to satisfy this need?
We define the system's functions as seen from the outside (what it must accomplish), the interfaces with actors, and the usage scenarios. We deliberately remain agnostic about the internal architecture. Requirements are derived from this functional analysis, not the other way around.

#### System Analysis

##### 3. Logical Architecture (LA)
How should functions be organized into logical components?
We decompose the system into logical components (without prejudging technologies) and allocate functions to these components. We work on internal exchange flows, the consistency of the breakdown, and identify the first architectural choices. This is the core of system design.

##### 4. Physical Architecture (PA)
What concrete solutions implement the logical architecture?
We map logical components onto real physical components (hardware, software, networks). We handle deployment, performance, and redundancy constraints. We prepare the work allocation between subcontractors.
