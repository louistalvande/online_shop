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
cd "c:\Users\mltal\Documents\Travail_Louis\online_shop\solution" && docker compose -f docker-compose.dev.yml exec -T db psql -U shop -d shop -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO shop;"
```

> Cette commande est également disponible via la config de lancement VS Code **ShopApplication (reset DB)**.

## Lancer le backend

```bash
cd backend
./mvnw spring-boot:run
```

## Lancer les frontends

```bash
cd frontend/buyer-portal      && npm install && npm run dev -- --host --port 5173
cd frontend/vendor-backoffice && npm install && npm run dev -- --host --port 5174
cd frontend/admin-console     && npm install && npm run dev -- --host --port 5175
```

Accès via Nginx (nécessite `docker compose -f docker-compose.dev.yml up`) :

| App | URL |
|---|---|
| Portail acheteur | http://buyer.localhost |
| Back-office vendeur | http://vendor.localhost |
| Console admin | http://admin.localhost |


## Générer `openapi.yaml`

Se connecter au conteneur `shop-backend` via VS Code Remote SSH, puis exécuter :

```bash
cd /workspace && ./mvnw test -Popenapi
```

Démarre un contexte Spring avec H2 (sans PostgreSQL ni Redis) et écrit `openapi.yaml` à côté du `pom.xml`.


## Tests end-to-end (Playwright)

Les tests s'exécutent dans le conteneur `frontend` via `docker compose exec`. Les conteneurs Docker doivent être démarrés et le backend Spring Boot doit être lancé.

### Première installation (une seule fois)

```bash
docker compose -f docker-compose.dev.yml exec frontend bash -c "cd /workspace/e2e && npm install && npx playwright install chromium --with-deps"
```

### Lancer les tests

```bash
docker compose -f docker-compose.dev.yml exec frontend bash -c "cd /workspace/e2e && npm test"
```

```bash
# Console admin uniquement
docker compose -f docker-compose.dev.yml exec frontend bash -c "cd /workspace/e2e && npm run test:admin"

# Portail acheteur uniquement
docker compose -f docker-compose.dev.yml exec frontend bash -c "cd /workspace/e2e && npm run test:buyer"
```

Les URLs et credentials sont configurés dans `frontend/e2e/.env`.
