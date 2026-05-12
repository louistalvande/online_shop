# CLAUDE.md — Architecture Document (ARCADIA MBSE)

## Contexte

Ce dossier contient un document d'architecture système produit selon la méthode **ARCADIA** (Architecture Analysis and Design Integrated Approach), une approche MBSE (Model-Based Systems Engineering).

Le document est rédigé en **AsciiDoc** et les diagrammes sont générés avec **PlantUML**.

---

## Méthode ARCADIA — Rappel des niveaux

L'architecture suit les quatre niveaux d'analyse d'ARCADIA, dans cet ordre :

1. **OA — Operational Analysis** : besoins et activités des parties prenantes, contexte opérationnel
2. **SA — System Analysis** : fonctions attendues du système, interfaces avec les acteurs
3. **LA — Logical Architecture** : décomposition fonctionnelle logique, composants logiques et échanges
4. **PA — Physical Architecture** : décomposition physique, composants matériels/logiciels, déploiement

Chaque niveau doit être traité dans son propre fichier AsciiDoc et son propre répertoire de diagrammes.

---

## Structure des fichiers

```
.
├── CLAUDE.md
├── main.adoc                  ← point d'entrée principal (includes tous les chapitres)
├── 00-introduction/
│   └── introduction.adoc
├── 01-operational-analysis/
│   ├── oa.adoc
│   └── diagrams/
│       ├── oa-context.puml
│       ├── oa-activity.puml
│       └── oa-capability.puml
├── 02-system-analysis/
│   ├── sa.adoc
│   └── diagrams/
│       ├── sa-functional-chain.puml
│       └── sa-dataflow.puml
├── 03-logical-architecture/
│   ├── la.adoc
│   └── diagrams/
│       ├── la-component.puml
│       └── la-sequence.puml
├── 04-physical-architecture/
│   ├── pa.adoc
│   └── diagrams/
│       ├── pa-deployment.puml
│       └── pa-component.puml
└── glossary.adoc
```

---

## Conventions AsciiDoc

### En-tête de chaque fichier `.adoc`

```asciidoc
= Titre du chapitre
:doctype: book
:toc:
:toclevels: 3
:sectnums:
:imagesdir: diagrams
:plantuml-format: svg
```

### Inclusion d'un diagramme PlantUML

```asciidoc
.Titre du diagramme
[plantuml, nom-du-fichier, svg]
----
include::diagrams/nom-du-fichier.puml[]
----
```

### Structure type d'un chapitre ARCADIA

La structure est identique pour les quatre niveaux ; seuls le vocabulaire et les diagrammes varient.

```asciidoc
== Niveau XX — Nom du niveau

=== Objectif

Courte description de ce que ce niveau modélise et de son périmètre dans la démarche ARCADIA.

=== Éléments identifiés

Tableau des éléments propres à ce niveau (adapter l'intitulé de la colonne « Type » selon le niveau) :

[cols="1,2,3", options="header"]
|===
| Identifiant | Nom | Description

| XX-YYY-001  | Nom de l'élément | Description courte
|===

// OA  → Activités opérationnelles, acteurs opérationnels, capabilities
// SA  → Fonctions système, acteurs externes, chaînes fonctionnelles
// LA  → Composants logiques, fonctions allouées, interfaces logiques
// PA  → Composants physiques/logiciels, nœuds de déploiement, interfaces physiques

=== Diagrammes

// Inclure les diagrammes pertinents pour ce niveau.
// Voir le tableau « Types de diagrammes par niveau » dans les conventions PlantUML.

==== [Titre du premier diagramme]

.[Légende du diagramme]
[plantuml, xx-nom-du-diagramme, svg]
----
include::diagrams/xx-nom-du-diagramme.puml[]
----

==== [Titre du second diagramme]

.[Légende du diagramme]
[plantuml, xx-nom-du-second-diagramme, svg]
----
include::diagrams/xx-nom-du-second-diagramme.puml[]
----

=== Traçabilité

Tableau de traçabilité vers le niveau précédent (sauf pour OA) :

[cols="1,2,1,2", options="header"]
|===
| Identifiant (ce niveau) | Nom | Issu de (niveau N-1) | Justification

| XX-YYY-001 | Nom | XX-ZZZ-00x | Raffinement / allocation de ...
|===
```

---

## Conventions PlantUML

### Style global à inclure dans chaque `.puml`

```plantuml
!theme plain
skinparam defaultFontName "DejaVu Sans"
skinparam shadowing false
skinparam componentStyle rectangle
skinparam ArrowColor #444444
skinparam BackgroundColor #FAFAFA
skinparam BorderColor #888888
```

### Types de diagrammes par niveau ARCADIA

| Niveau | Types de diagrammes recommandés          | Syntaxe PlantUML      |
|--------|------------------------------------------|-----------------------|
| OA     | Contexte opérationnel, activités, capabilities | `usecase`, `activity` |
| SA     | Chaînes fonctionnelles, flux de données  | `sequence`, `usecase` |
| LA     | Composants logiques, interactions        | `component`, `sequence` |
| PA     | Déploiement, composants physiques        | `deployment`, `component` |

### Exemple — Diagramme de contexte OA (usecase)

```plantuml
@startuml oa-context
!theme plain
skinparam shadowing false

title Analyse Opérationnelle — Contexte

actor "Opérateur" as OP
actor "Système externe A" as SEA

rectangle "Système [périmètre]" {
  usecase "Activité principale 1" as UC1
  usecase "Activité principale 2" as UC2
}

OP --> UC1
OP --> UC2
SEA --> UC1

@enduml
```

### Exemple — Diagramme de composants LA

```plantuml
@startuml la-component
!theme plain
skinparam shadowing false
skinparam componentStyle rectangle

title Architecture Logique — Composants

package "Sous-système A" {
  [Composant A1]
  [Composant A2]
}

package "Sous-système B" {
  [Composant B1]
}

[Composant A1] --> [Composant B1] : Interface IFx
[Composant A2] --> [Composant A1] : flux données

@enduml
```

---

## Maquettes IHM avec Salt (PlantUML)

Salt est le sous-langage de maquettage filaire intégré à PlantUML. Les fichiers `.puml` Salt sont stockés dans le même répertoire `diagrams/` que les autres diagrammes, avec le préfixe `ihm-`.

### Quand utiliser Salt dans un doc ARCADIA

Les maquettes Salt appartiennent exclusivement au niveau **PA**, rattachées au composant physique ou logiciel frontend qui les implémente. C'est à ce niveau que l'on sait quel nœud de déploiement porte l'IHM et quelles fonctions logiques y sont allouées.


### Nommage des fichiers

```
diagrams/ihm-<niveau>-<nom-fonctionnel>.puml
```

Exemples : `ihm-sa-saisie-commande.puml`, `ihm-la-tableau-de-bord.puml`

### Inclusion dans AsciiDoc

```asciidoc
==== Maquette IHM — [Nom de l'écran]

.Maquette filaire : [Nom de l'écran] — niveau [SA/LA/PA]
[plantuml, ihm-sa-nom-ecran, svg]
----
include::diagrams/ihm-sa-nom-ecran.puml[]
----

// Relier explicitement la maquette à un élément ARCADIA identifié :
// Cette maquette illustre la fonction SA-FNC-00x « Nom de la fonction ».
```

### Style global Salt

Ajouter en tête de chaque fichier Salt :

```plantuml
@startsalt
' Pas de skinparam dans Salt — le style est contrôlé par la structure
' Utiliser des séparateurs et boîtes pour structurer l'écran
@endsalt
```

### Primitives Salt essentielles

| Primitive          | Syntaxe                          | Rendu                              |
|--------------------|----------------------------------|------------------------------------|
| Champ texte        | `"placeholder     "`             | Champ de saisie                    |
| Champ texte nommé  | `"valeur initiale"`              | Champ pré-rempli                   |
| Bouton             | `[Libellé]`                      | Bouton cliquable                   |
| Case à cocher      | `[X] Libellé` / `[ ] Libellé`   | Cochée / décochée                  |
| Radio              | `(X) Option` / `( ) Option`     | Sélectionnée / non sélectionnée    |
| Liste déroulante   | `^Item sélectionné^`             | Dropdown                           |
| Séparateur         | `--` ou `==`                     | Ligne de séparation légère / forte |
| Onglets            | `{/ Onglet1 | Onglet2 | Onglet3}` | Barre d'onglets                   |
| Arborescence       | `{T + Nœud \n ++ Feuille}`       | Arbre expandable                   |
| Tableau            | `{# Col1 | Col2 \n valeur | val}` | Grille de données                 |
| Scroll vertical    | `{SI ... }`                      | Zone scrollable                    |

### Exemple — Formulaire (composant frontend PA)

```plantuml
@startsalt
' Composant PA-PHY-003 — Application web frontend
' Illustre les fonctions allouées : LA-CMP-005 (saisie commande), LA-CMP-006 (validation)
title PA-PHY-003 — Saisie d'une commande

{+
  == Nouvelle commande
  Référence produit | "REF-         "
  Quantité          | "1            "
  Client            | ^Sélectionner un client^
  --
  Livraison urgente | [ ] Oui
  --
  Commentaire       | {SI
                       "                    "
                       "                    "
                     }
  --
  [Annuler]  | [Valider la commande]
}

@endsalt
```

### Exemple — Tableau de bord (composant frontend PA)

```plantuml
@startsalt
' Composant PA-PHY-003 — Application web frontend
' Illustre la fonction allouée : LA-CMP-007 (supervision temps réel)
title PA-PHY-003 — Tableau de bord opérateur

{+
  {/ Supervision | Alertes | Historique }
  ==
  {#
    Équipement     | État      | Dernière MAJ
    Capteur A1     | <b>OK     | 10:42
    Capteur B3     | <color:red>ALERTE | 10:38
    Actionneur C2  | OK        | 10:41
  }
  --
  [Acquitter alerte]  [Rafraîchir]  [Exporter]
}

@endsalt
```

### Exemple — Navigation entre écrans (composant frontend PA)

Utiliser un diagramme d'activité ou de séquence PlantUML classique pour représenter l'enchaînement des écrans ; Salt ne modélise qu'un écran à la fois.

```plantuml
@startuml pa-ihm-navigation
!theme plain
skinparam shadowing false

title PA — Navigation IHM frontend

[*] --> Connexion
Connexion --> Tableau_de_bord : authentification OK
Tableau_de_bord --> Saisie_commande : action utilisateur
Saisie_commande --> Tableau_de_bord : validation / annulation
Tableau_de_bord --> [*] : déconnexion

@enduml
```

### Règles pour les maquettes Salt

- Un fichier Salt = un écran ou une modale. Un composant IHM peut et doit être documenté par autant de fichiers Salt que d'écrans significatifs qu'il expose.
- Le titre du `@startsalt` doit référencer l'identifiant ARCADIA de la fonction ou du composant illustré.
- Ne pas modéliser la charte graphique finale (couleurs, polices) : Salt est filaire par nature.
- Toujours accompagner la maquette d'un paragraphe textuel explicitant les éléments clés et leur lien avec les exigences.
- Pour les enchaînements d'écrans, utiliser un diagramme d'activité ou d'états PlantUML séparé.

---

## Règles de rédaction

- Rédiger en **français** sauf si le projet impose l'anglais.
- Chaque élément ARCADIA (activité, fonction, composant, interface) doit avoir un **identifiant unique** : `OA-ACT-001`, `SA-FNC-003`, `LA-CMP-002`, `PA-PHY-005`.
- Les interfaces entre composants doivent être nommées et décrites.
- Chaque diagramme doit avoir un titre et une légende (caption AsciiDoc).
- Ne pas dupliquer du contenu entre niveaux : référencer (`<<identifiant>>`) plutôt que répéter.
- Les acronymes doivent être définis dans `glossary.adoc` à leur première apparition.

---

## Ce que Claude doit faire dans ce dossier

- Générer ou compléter des sections AsciiDoc en respectant la structure et les conventions ci-dessus.
- Créer des diagrammes PlantUML cohérents avec le niveau ARCADIA concerné.
- Respecter la numérotation des identifiants existants (vérifier avant d'en créer de nouveaux).
- Proposer systématiquement les quatre niveaux si une nouvelle fonctionnalité système est décrite.
- Signaler toute incohérence entre niveaux (ex. : un composant LA sans correspondance en SA).
- Ne jamais mélanger des éléments de niveaux différents dans un même diagramme.
- Mettre à jour `main.adoc` si un nouveau fichier `.adoc` est créé.
- Pour les maquettes Salt : les générer uniquement au niveau PA, rattachées au composant frontend concerné (identifiant `PA-PHY-xxx`). Toujours indiquer en commentaire les fonctions LA allouées à ce composant qu'illustre la maquette.
- Un composant ou une fonction IHM peut donner lieu à autant de maquettes Salt que nécessaire (un fichier `.puml` par écran, modale ou état significatif) ; les regrouper dans un sous-répertoire `diagrams/ihm/` si leur nombre dépasse trois.
- Pour les enchaînements d'écrans, générer un diagramme d'activité ou d'états séparé plutôt que de multiplier les fichiers Salt.
