# Solution — Online Shop

## Démarrage de l'environnement de développement

```bash
docker compose -f docker-compose.dev.yml up --build -d
```

## Commandes utiles

### Se connecter à la base de données PostgreSQL

```bash
docker compose -f docker-compose.dev.yml exec db psql -U shop -d shop
```

### Réinitialiser la base de données

Supprime et recrée le schéma public. Au prochain démarrage du backend, Flyway rejoue toutes les migrations depuis V1.

```bash
docker compose -f docker-compose.dev.yml exec -T db psql -U shop -d shop \
  -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO shop;"
```

> Cette commande est également disponible via la config de lancement VS Code **ShopApplication (reset DB)**.

## Lancer le backend

```bash
# Depuis le conteneur shop-backend (VS Code Remote SSH)
mvn spring-boot:run
```

## Lancer les frontends

```bash
# Depuis chaque conteneur frontend (VS Code Remote SSH)
npm run --prefix /workspace/vendor-backoffice dev -- --host --port 5173
npm run --prefix /workspace/buyer-portal      dev -- --host --port 5173
npm run --prefix /workspace/admin-console     dev -- --host --port 5173
```

Accès via Nginx :

| App | URL |
|---|---|
| Portail acheteur | http://buyer.localhost:5173 |
| Back-office vendeur | http://vendor.localhost:5174 |
| Console admin | http://admin.localhost:5175 |


## Générer `openapi.yaml`

Depuis le conteneur `shop-backend` (VS Code Remote SSH) :

```bash
cd /workspace && mvn test -Popenapi
```

Démarre un contexte Spring avec H2 (sans PostgreSQL ni Redis) et écrit `openapi.yaml` à côté du `pom.xml`.


## Tests end-to-end (Playwright)

Les tests tournent dans un **container dédié** (`shop-e2e`) via le profil Docker Compose `e2e`.  
Le script `run-e2e.sh` orchestre l'ensemble : kill des processus dev existants, démarrage propre, attente readiness, exécution des tests, kill après.

### Lancer tous les tests

```bash
# Depuis le répertoire solution/
bash run-e2e.sh
```

> **Pré-requis** : les conteneurs `shop-*` doivent tourner (`docker compose -f docker-compose.dev.yml up -d`).  
> Spring Boot et Vite sont démarrés automatiquement par le script.

### Options Playwright

```bash
# Un projet spécifique
bash run-e2e.sh -- --project=admin-console
bash run-e2e.sh -- --project=vendor-portal
bash run-e2e.sh -- --project=buyer-portal

# Un test précis
bash run-e2e.sh -- --grep "login"

# Limiter les workers (réduit la charge sur Vite)
bash run-e2e.sh -- --workers=2
```

### Rapport HTML

Le rapport est généré dans `frontend/e2e/playwright-report/index.html` après chaque run.  
Il inclut un screenshot pour chaque test (succès et échec).

### Configuration

| Fichier | Rôle |
|---|---|
| `frontend/e2e/.env` | URLs et credentials pour run hors container |
| `frontend/e2e/.env.docker` | URLs Docker internes (utilisé par `test:docker`) |
| `docker-compose.dev.yml` service `e2e` | Image Playwright, env vars, volume |
| `frontend/e2e/playwright.config.js` | Projets, timeouts, reporter |

### Base de données

La base est **réinitialisée automatiquement avant et après** chaque run (données de seed Flyway conservées).
