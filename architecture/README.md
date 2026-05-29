# Architecture — génération des documents

Les commandes suivantes sont à exécuter depuis le répertoire `architecture/`.

## PDF

```bash
asciidoctor-pdf -r asciidoctor-diagram main.adoc -o "../Application e-commerce sécurisée - Documentation d'Architecture.pdf"
```

## HTML

```bash
asciidoctor -r asciidoctor-diagram -a data-uri -a webfonts! -o "../Application e-commerce sécurisée - Documentation d'Architecture.html" main.adoc
```

## Document des besoins opérationnels (PDF)

```bash
asciidoctor-pdf "exigences.adoc" -o "../Application e-commerce sécurisée - Documentation des exigences.pdf"
```
