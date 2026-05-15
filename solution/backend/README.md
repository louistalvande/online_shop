
# shop-api

Spring Boot REST API with PostgreSQL, Redis, JWT auth, Stripe, and SendGrid.

## Requirements

- Java 21
- Maven 3.9+
- PostgreSQL
- Redis

## Commands

### Run locally (inside backend container)

```bash
mvn spring-boot:run
```

### Build (inside backend container)

```bash
mvn package
mvn package -DskipTests
```

### Test (inside backend container)

```bash
mvn test
```

### Docker

```bash
# Production image
docker build -t shop-api .
docker run -p 8080:8080 \
  -e DB_HOST=localhost \
  -e DB_NAME=shop \
  -e DB_USER=shop \
  -e DB_PASSWORD=secret \
  -e REDIS_HOST=localhost \
  shop-api

# Dev image (SSH on :22, app on :8080)
docker build -f Dockerfile.dev -t shop-api-dev .
docker run -p 22:22 -p 8080:8080 shop-api-dev
```

## Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_NAME` | `shop` | Database name |
| `DB_USER` | `shop` | Database user |
| `DB_PASSWORD` | `dev` | Database password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |

## Database

All commands use Docker. Run from the **repo root** (`online_shop/`).

### Run migrations

Flyway runs automatically on backend startup. To trigger manually inside the running container:

```powershell
docker compose -f solution/docker-compose.dev.yml exec backend bash -c "mvn flyway:migrate -Ddb.host=db"
```

### Reset — clean schema (keep database)

Drops all tables then re-runs migrations on backend restart:

```powershell
docker compose -f solution/docker-compose.dev.yml exec db psql -U shop -d shop -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO shop;"
docker compose -f solution/docker-compose.dev.yml restart backend
```

### Reset — drop and recreate database

Connect to `postgres` (you can't drop the active database):

```powershell
docker compose -f solution/docker-compose.dev.yml exec db psql -U shop -d postgres -c "DROP DATABASE shop;"
docker compose -f solution/docker-compose.dev.yml exec db psql -U shop -d postgres -c "CREATE DATABASE shop;"
docker compose -f solution/docker-compose.dev.yml restart backend
```

Flyway re-runs all migrations automatically on backend startup.

### Seed admin account

Password: `@Password26`

```powershell
docker compose -f solution/docker-compose.dev.yml exec db psql -U shop -d shop -c "INSERT INTO accounts (id, email, password_hash, first_name, last_name, role, status, created_at) VALUES (gen_random_uuid(), 'admin@onlineshop.com', '\$2a\$10\$ChRjfMxH2qKlmI5L.uqmV.SXW8NKJI59ML0gLTbPjXvObG2.lEOD6', 'Admin', 'System', 'ADMIN', 'ACTIVE', NOW());"
```

## API docs

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Health check: `http://localhost:8080/actuator/health`
