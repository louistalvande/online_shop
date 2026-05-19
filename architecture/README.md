# Architecture — génération des documents

Les commandes suivantes sont à exécuter depuis le répertoire `architecture/`.

## PDF

```bash
asciidoctor-pdf -r asciidoctor-diagram main.adoc -o "Application Ecommerce - Documentation d'Architecture.pdf"
```

## HTML

```bash
asciidoctor -r asciidoctor-diagram -a data-uri -a webfonts! -o "Application Ecommerce - Documentation d'Architecture.html" main.adoc
```

## Document des besoins opérationnels (PDF)

```bash
asciidoctor -o "Application Ecommerce - Exigences utilisateurs.pdf" "Application Ecommerce - Exigences utilisateurs.adoc"
```
