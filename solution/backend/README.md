
# shop-api

Spring Boot REST API with PostgreSQL, Redis, JWT auth, Stripe, and SendGrid.

## Requirements

- Java 21
- Maven 3.9+
- PostgreSQL
- Redis

## Commands

### Run locally

```bash
./start.sh
# or
mvn spring-boot:run
```

### Build

```bash
mvn package
mvn package -DskipTests
```

### Test

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

## API docs

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Health check: `http://localhost:8080/actuator/health`
