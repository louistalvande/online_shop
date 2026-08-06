# CLAUDE.md — Solution (Code Generation Guide)

## Architecture reference

The full ARCADIA specification lives in `../architecture/`. Read it before generating any code:

- `00-introduction/introduction.adoc` — project scope
- `01-operational-analysis/oa.adoc` — stakeholder needs
- `02-system-analysis/sa.adoc` — system functions, actors, class model
- `03-logical-architecture/la.adoc` — logical components, interfaces, class diagram
- `04-physical-architecture/pa.adoc` — physical stack, deployment, API sequences, UI wireframes

If what is asked is missing or in opposition to the specification, suggest to update it before generating code.

### Modification order

Every feature or change must cascade through the architecture levels in this order before touching source code:

1. `exigences.adoc` — add or update the operational need (BES-*)
2. `oa.adoc` — update operational constraints (COp-*), capabilities (CAP-*), use-case diagram and activity table
3. `sa.adoc` — add or update system functions (FS-*), constraints (CS-*), class model
4. `la.adoc` — update logical components, allocated functions, data model
5. `pa.adoc` — update physical descriptions, UI screens, REST endpoint tables
6. `us.adoc` — add or update user stories (US-*) and traceability matrix
7. **Solution source code** — implement backend, frontend, i18n, migrations, tests

Never skip a level. If a level does not change for a given feature, note it explicitly before moving on.

---

## Tech stack

| Layer | Technology |
|---|---|
| Frontend (×3) | React 18 + TypeScript + Vite |
| Backend | Spring Boot 3 (Java 21), Spring Data JPA, Hibernate |
| Database | PostgreSQL 16 + Flyway |
| Cache / Sessions | Redis 7 (Spring Data Redis) |
| Payments | Stripe Java SDK |
| Email | SendGrid Java SDK |
| Reverse proxy | Nginx |
| Containers | Docker Compose (dev), Kubernetes (prod) |

---

## Backend layered architecture

Every domain package follows the strict **Controller → Service → Repository** pattern. No layer may be skipped.

```
Controller        Receives HTTP requests, validates input, delegates to Service.
                  Returns DTOs — never JPA entities directly.

Service           Contains all business logic and transaction boundaries (@Transactional).
                  Calls one or more Repositories. Throws typed exceptions.

Repository        Spring Data JPA interface (extends JpaRepository<Entity, Long>).
                  No business logic — queries only.
```

### Layer rules

- **Controllers** are defined as an **interface** (`OrderController`) carrying the `@RequestMapping` annotations, with a single implementation (`OrderControllerImpl`, annotated `@RestController`). The impl injects the **Service interface**, never the impl or any Repository.
- **Services** are defined as an **interface** (`OrderService`) with a single implementation (`OrderServiceImpl`, annotated `@Service`). The impl owns `@Transactional` boundaries, uses domain entities internally, and converts to/from DTOs at its public boundary.
- **Repositories** are defined as an **interface** (`OrderRepository`) extending `JpaRepository`. Spring Data generates the standard impl automatically. When custom queries are needed beyond `@Query`, add a `OrderRepositoryCustom` interface and its `impl/OrderRepositoryImpl` — the main repository interface extends both.
- **DTOs** live in a `dto/` sub-package. Separate request and response types.
- **Entities** live in an `entity/` sub-package, annotated with `@Entity`.
- **Exceptions** live in an `exception/` sub-package. A global `@ControllerAdvice` in `common/` maps them to HTTP responses.

### Package layout (one domain — example: `order`)

```
order/
├── controller/
│   ├── OrderController.java          ← interface (@RequestMapping)
│   └── impl/
│       └── OrderControllerImpl.java  ← @RestController, injects OrderService
├── service/
│   ├── OrderService.java             ← interface
│   └── impl/
│       └── OrderServiceImpl.java     ← @Service @Transactional
├── repository/
│   ├── OrderRepository.java          ← interface extends JpaRepository + OrderRepositoryCustom
│   ├── OrderRepositoryCustom.java    ← interface for custom queries (add only when needed)
│   ├── impl/
│   │   └── OrderRepositoryImpl.java  ← implements OrderRepositoryCustom (add only when needed)
│   └── OrderLineRepository.java      ← interface extends JpaRepository
├── entity/
│   ├── Order.java
│   ├── OrderLine.java
│   └── OrderStatus.java              ← enum
├── dto/
│   ├── CreateOrderRequest.java
│   ├── OrderResponse.java
│   └── ShipmentRequest.java
└── exception/
    ├── OrderNotFoundException.java
    └── InvalidOrderStateException.java
```

Apply the same layout to every domain packages

---

## OpenAPI specification

The backend exposes its full OpenAPI 3 spec via **springdoc-openapi**:

- Runtime: `GET /api-docs` (JSON), `GET /swagger-ui.html` (UI)
- Static export: `backend/openapi.yaml` — committed to the repository and kept in sync with the code.

### Rules

- Every controller interface method must carry `@Operation(summary = "...")` and `@ApiResponse` annotations so the generated spec is self-documenting.
- Every DTO field must carry `@Schema(description = "...")`.
- The static `openapi.yaml` must be regenerated whenever an endpoint is added, removed, or changed (run `./mvnw test -Popenapi` from inside the `shop-backend` container — this uses `OpenApiGeneratorTest` with an H2 context, no running server required).
- The frontend apps must import types from the generated spec (via `openapi-typescript` or equivalent) — do not hand-write API client types.

---

## API conventions

- All REST endpoints in **English** only.
- Base path: `/api/`
- Authentication: JWT Bearer token (access + refresh).
- Error responses: `{ "error": "...", "message": "..." }` with appropriate HTTP status.
- Pagination: `?page=0&size=20` (Spring Pageable).

---

## Frontend apps

Each SPA communicates exclusively with the backend REST API. The IHM wireframes in `../architecture/04-physical-architecture/diagrams/ihm/` define the screens to implement.

| App | Directory | Screens |
|---|---|---|
| Buyer Portal | `frontend/buyer-portal/` | login, registration, catalog, product detail, cart, checkout (address + payment), order confirmation, pending wire transfer, order tracking, cancellation, complaint, my account |
| Vendor Back-office | `frontend/vendor-backoffice/` | dashboard, order detail, shipment, post-shipment cancellation, wire transfer, catalog, product, complaints, reports |
| Admin Console | `frontend/admin-console/` | accounts, carriers, carrier form |

---

## Internationalisation (i18n)

All three SPAs must support **French (fr), Spanish (es) and English (en)**. French is the default locale.

### Frontend

- Use **react-i18next** (`i18next` + `react-i18next`).
- Translation files: `src/i18n/fr.json`, `src/i18n/es.json` and `src/i18n/en.json` in each SPA.
- All user-visible strings must go through `t("key")` — no hardcoded UI text.
- A language toggle must be visible on every app (e.g., header).
- Keys are in English, dot-notation, grouped by feature: `order.status.pending`, `auth.login.submit`, etc.

### Backend

- User-facing error messages (returned in API responses) must be localised via Spring's `MessageSource`.
- Message files: `src/main/resources/messages.properties` (default = French), `messages_es.properties`, `messages_en.properties`.
- The client sends its locale in the `Accept-Language` header; the backend resolves the appropriate message.
- Internal log messages stay in English only.

---

## Dev environment — VS Code Remote SSH into containers

In dev mode each service runs in its own Docker container with an SSH server, so VS Code can connect to it via **Remote - SSH** and edit, build, and run code directly inside the container.

Each SPA has its own container: `buyer-portal`, `vendor-backoffice`, `admin-console`. The `./frontend` directory is bind-mounted at `/workspace` in all three so the shared `@workspace/theme` package remains resolvable by npm workspaces.

### Dev Dockerfiles

See `backend/Dockerfile.dev` and `frontend/Dockerfile.dev` (shared by all three SPA containers, takes `NGINX_CONF` build arg). Per-SPA nginx configs: `frontend/<spa>/nginx.dev.conf` — proxies `/api` to `backend:8080` and `/` to the Vite dev server on port 5173 with WebSocket upgrade for HMR.

### `docker-compose.dev.yml`

See `solution/docker-compose.dev.yml`. Port map:

| Service | HTTP | SSH |
|---|---|---|
| backend | 8080 | 2222 |
| buyer-portal | 5173 | 2223 |
| vendor-backoffice | 5174 | 2224 |
| admin-console | 5175 | 2225 |

### Workflow

```bash
# Start all dev containers
docker compose -f docker-compose.dev.yml up --build -d

# In VS Code: Remote-SSH → Connect to Host → shop-backend / shop-buyer-portal / shop-vendor-backoffice / shop-admin-console
# Open /workspace — source is live-mounted from the host

# Inside backend container — build and run
./mvnw spring-boot:run          # Spring Boot DevTools watches for class changes

# Inside each SPA container — install deps and start Vite (Nginx already running on :80)
cd /workspace/buyer-portal      && npm install && npm run dev -- --host --port 5173
# (vendor-backoffice and admin-console: same command, run inside their own container)

# Access each app via its host port:
#   http://localhost:5173  (buyer-portal)
#   http://localhost:5174  (vendor-backoffice)
#   http://localhost:5175  (admin-console)
# Or via subdomain if /etc/hosts maps *.localhost:
#   http://buyer.localhost:5173 / http://vendor.localhost:5174 / http://admin.localhost:5175
```

---

## Development commands

```bash
# Backend (local, no Docker)
cd backend
./mvnw spring-boot:run

# Regenerate openapi.yaml
./mvnw test -Popenapi

# Frontend (local, no Docker)
cd frontend/buyer-portal   # or vendor-backoffice / admin-console
npm install
npm run dev

# Full stack (prod-like)
docker compose up --build

# Run DB migrations only
./mvnw flyway:migrate

# Repair Flyway metadata (fix checksum mismatches after a migration file was edited)
./mvnw flyway:repair
```

---

## What Claude must do in this directory
- Strictly enforce Controller → Service → Repository: no layer skip, no Repository injection in a Controller.
- Generate code that strictly matches the domain model and business rules above.
- Verify identifiers against the architecture before creating new entities or endpoints.
- Before implementing a feature, verify it in the architecture document, and ask to update it before generating code.
- After architecture changes approved, implement modify code, and add TU for Controller and Service with Mockito framework. Add end2end test with playwright in js.
- **After every feature implementation**, regenerate `openapi.yaml` by running `./mvnw test -Popenapi` inside the `shop-backend` container and commit the updated file alongside the code change.
- **End-to-end tests** live in `frontend/e2e/` and are written in JavaScript with Playwright. One spec file per User Story: `e2e/<domain>/<us-id>.spec.js` (e.g. `e2e/carrier/adm-07.spec.js`). Each spec must cover the nominal path and the main error cases defined in the acceptance criteria. E2E tests must be updated or extended whenever the feature they cover is modified.
- At the end of the code generation, review the entire changes, test if it compiles, and fix or improve if possible.
- Never deviate from the domain model and business rules defined in the architecture — read it first.
- **All code and all comments must be in English** — class names, method names, variables, field names, inline comments, Javadoc, SQL columns, API paths, Git commit messages.
- User-facing strings (UI labels, email templates, API error messages) must be localised in **fr**, **es**, and **en** — never hardcoded in a single language.
- **Every public method and every public field must have a Javadoc comment.** Minimum: one sentence describing what it does. For methods: document parameters (`@param`), return value (`@return`), and checked exceptions (`@throws`) when present. Private/package-private members do not require Javadoc.
- Flyway migration scripts must be sequential: `V1__init_schema.sql`, `V2__...`, etc.
- **Before the first release**, fold all schema changes into the existing latest migration file instead of creating a new version. A new `Vn__` file is only justified once the previous version has been applied to a deployed environment and cannot be modified.
- When generating a frontend screen, cross-reference the corresponding wireframe in `../architecture/04-physical-architecture/diagrams/ihm/`.
- Do not implement a carrier API integration — IFS-05 is a plain outbound URL, nothing more.
