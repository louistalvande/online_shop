# Architecture — génération des documents

Les commandes suivantes sont à exécuter depuis le répertoire `architecture/`.

## PDF

```bash
asciidoctor-pdf -r asciidoctor-diagram main.adoc -o "Boutique en ligne - Documentation d'Architecture.pdf"
```

## HTML

```bash
asciidoctor -r asciidoctor-diagram -o "Boutique en ligne - Documentation d'Architecture.html" main.adoc
```

## Document des besoins opérationnels (HTML)

```bash
asciidoctor -o exigences.html exigences.adoc
```
