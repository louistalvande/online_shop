# CLAUDE.md — Solution (Code Generation Guide)

## Architecture reference

The full ARCADIA specification lives in `../architecture/`. Read it before generating any code:

- `00-introduction/introduction.adoc` — project scope
- `01-operational-analysis/oa.adoc` — stakeholder needs
- `02-system-analysis/sa.adoc` — system functions, actors, class model
- `03-logical-architecture/la.adoc` — logical components, interfaces, class diagram
- `04-physical-architecture/pa.adoc` — physical stack, deployment, API sequences, UI wireframes

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

Apply the same layout to every domain package: `account`, `catalog`, `cart`, `order`, `payment`, `shipment`, `notification`, `report`.

---

## Project structure

```
solution/
├── backend/
│   ├── src/main/java/com/shop/
│   │   ├── common/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── JwtFilter.java
│   │   │   └── SecurityConfig.java
│   │   ├── account/
│   │   │   ├── controller/
│   │   │   │   ├── AccountController.java
│   │   │   │   └── impl/
│   │   │   │       └── AccountControllerImpl.java
│   │   │   ├── service/
│   │   │   │   ├── AccountService.java
│   │   │   │   └── impl/
│   │   │   │       └── AccountServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   └── AccountRepository.java
│   │   │   ├── entity/
│   │   │   │   └── Account.java
│   │   │   ├── dto/
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   └── AccountResponse.java
│   │   │   └── exception/
│   │   │       └── AccountNotFoundException.java
│   │   ├── catalog/
│   │   │   ├── controller/
│   │   │   │   ├── ProductController.java
│   │   │   │   └── impl/
│   │   │   │       └── ProductControllerImpl.java
│   │   │   ├── service/
│   │   │   │   ├── ProductService.java
│   │   │   │   └── impl/
│   │   │   │       └── ProductServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   └── ProductRepository.java
│   │   │   ├── entity/
│   │   │   │   └── Product.java
│   │   │   ├── dto/
│   │   │   │   ├── ProductRequest.java
│   │   │   │   └── ProductResponse.java
│   │   │   └── exception/
│   │   │       └── ProductNotFoundException.java
│   │   ├── cart/
│   │   │   ├── controller/
│   │   │   │   ├── CartController.java
│   │   │   │   └── impl/
│   │   │   │       └── CartControllerImpl.java
│   │   │   ├── service/
│   │   │   │   ├── CartService.java
│   │   │   │   └── impl/
│   │   │   │       └── CartServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   └── CartItemRepository.java
│   │   │   ├── entity/
│   │   │   │   └── CartItem.java
│   │   │   └── dto/
│   │   │       ├── AddCartItemRequest.java
│   │   │       └── CartResponse.java
│   │   ├── order/
│   │   │   ├── controller/
│   │   │   │   ├── OrderController.java
│   │   │   │   └── impl/
│   │   │   │       └── OrderControllerImpl.java
│   │   │   ├── service/
│   │   │   │   ├── OrderService.java
│   │   │   │   └── impl/
│   │   │   │       └── OrderServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   ├── OrderRepository.java
│   │   │   │   └── OrderLineRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderLine.java
│   │   │   │   └── OrderStatus.java
│   │   │   ├── dto/
│   │   │   │   ├── CreateOrderRequest.java
│   │   │   │   ├── OrderResponse.java
│   │   │   │   └── ShipmentRequest.java
│   │   │   └── exception/
│   │   │       ├── OrderNotFoundException.java
│   │   │       └── InvalidOrderStateException.java
│   │   ├── payment/
│   │   │   ├── controller/
│   │   │   │   ├── PaymentController.java
│   │   │   │   └── impl/
│   │   │   │       └── PaymentControllerImpl.java
│   │   │   ├── service/
│   │   │   │   ├── PaymentService.java
│   │   │   │   └── impl/
│   │   │   │       └── PaymentServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   └── PaymentRepository.java
│   │   │   ├── entity/
│   │   │   │   └── Payment.java
│   │   │   └── dto/
│   │   │       └── PaymentResponse.java
│   │   ├── shipment/
│   │   │   ├── controller/
│   │   │   │   ├── ShipmentController.java
│   │   │   │   └── impl/
│   │   │   │       └── ShipmentControllerImpl.java
│   │   │   ├── service/
│   │   │   │   ├── ShipmentService.java
│   │   │   │   └── impl/
│   │   │   │       └── ShipmentServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   ├── ShipmentRepository.java
│   │   │   │   └── CarrierRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── Shipment.java
│   │   │   │   └── Carrier.java
│   │   │   └── dto/
│   │   │       ├── DeclareShipmentRequest.java
│   │   │       └── TrackingResponse.java
│   │   ├── notification/
│   │   │   └── service/
│   │   │       ├── NotificationService.java
│   │   │       └── impl/
│   │   │           └── NotificationServiceImpl.java
│   │   └── report/
│   │       ├── controller/
│   │       │   ├── ReportController.java
│   │       │   └── impl/
│   │       │       └── ReportControllerImpl.java
│   │       ├── service/
│   │       │   ├── ReportService.java
│   │       │   └── impl/
│   │       │       └── ReportServiceImpl.java
│   │       └── dto/
│   │           └── SalesReportResponse.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/
│   │       └── V1__init_schema.sql
│   └── pom.xml
├── frontend/
│   ├── buyer-portal/               ← Buyer Portal (SPA)
│   │   └── src/
│   │       └── i18n/
│   │           ├── en.json
│   │           └── fr.json
│   ├── vendor-backoffice/          ← Vendor Back-office (SPA)
│   │   └── src/
│   │       └── i18n/
│   │           ├── en.json
│   │           └── fr.json
│   └── admin-console/              ← Admin Console (SPA)
│       └── src/
│           └── i18n/
│               ├── en.json
│               └── fr.json
└── docker-compose.yml
```

---

## OpenAPI specification

The backend exposes its full OpenAPI 3 spec via **springdoc-openapi**:

- Runtime: `GET /api-docs` (JSON), `GET /swagger-ui.html` (UI)
- Static export: `backend/openapi.yaml` — committed to the repository and kept in sync with the code.

### Rules

- Every controller interface method must carry `@Operation(summary = "...")` and `@ApiResponse` annotations so the generated spec is self-documenting.
- Every DTO field must carry `@Schema(description = "...")`.
- The static `openapi.yaml` must be regenerated whenever an endpoint is added, removed, or changed (run `./mvnw springdoc-openapi:generate`).
- The frontend apps must import types from the generated spec (via `openapi-typescript` or equivalent) — do not hand-write API client types.

---

## API conventions

- All REST endpoints in **English** only.
- Base path: `/api/`
- Authentication: JWT Bearer token (access + refresh).
- Error responses: `{ "error": "...", "message": "..." }` with appropriate HTTP status.
- Pagination: `?page=0&size=20` (Spring Pageable).

### Key endpoints (from PA sequences)

```
POST   /api/auth/login
POST   /api/auth/register

GET    /api/products
GET    /api/products/{id}
POST   /api/products                  (vendor)
PUT    /api/products/{id}             (vendor)

GET    /api/cart
POST   /api/cart/items
DELETE /api/cart/items/{id}

POST   /api/orders
GET    /api/orders/{id}
POST   /api/orders/{id}/confirm-transfer    (buyer — wire transfer)
PUT    /api/orders/{id}/shipment            (vendor — declare shipment)
GET    /api/orders/{id}/tracking            (buyer — get tracking URL)
POST   /api/orders/{id}/cancel             (buyer or vendor)
POST   /api/orders/{id}/return             (buyer — return request)

POST   /api/payments/stripe/webhook
GET    /api/reports/sales                   (vendor)
```

---

## Domain model

Defined precisely in `../architecture/03-logical-architecture/la.adoc` (section Diagramme de classes).

### Critical entities

**Product** — stock is merged into the product (no separate Stock entity):
```java
String reference
String name
String description
BigDecimal unitPriceInclTax
int availableQuantity
int stockAlertThreshold
String category
boolean active
```

**Order** — always has at least one OrderLine:
```java
// Order
String reference
LocalDateTime orderDate
OrderStatus status             // see order states below
BigDecimal totalAmountInclTax
String paymentMethod           // "card" | "wire_transfer"
// relations
Account buyer
List<OrderLine> lines
Payment payment
Shipment shipment
```

```java
// OrderLine (persisted detail — do not skip this entity)
Product product
int quantity
BigDecimal unitPriceInclTax
```

**Account.status**: `PENDING | ACTIVE | SUSPENDED | DELETED`

### Order states

```
PENDING_WIRE_TRANSFER → PENDING → IN_PREPARATION → SHIPPED → DELIVERED
                                                 ↘ CANCELLED
                                    SHIPPED      → PENDING_RETURN → CANCELLED
```

---

## Key business rules

These are non-obvious rules derived from the architecture — implement them exactly:

### Wire transfer
1. Buyer selects "wire transfer" at checkout → order created with status `PENDING_WIRE_TRANSFER`.
2. Buyer confirms having sent the transfer via `POST /orders/{id}/confirm-transfer`.
3. **Vendor manually validates** receipt of funds via the back-office → status moves to `PENDING`.
4. There is no automatic bank webhook — the vendor confirms manually (FS-V09).

### Shipment declaration
1. Vendor physically brings the parcel to the post office.
2. Vendor retrieves the tracking number from the deposit receipt.
3. Vendor enters the tracking number in the back-office (`PUT /orders/{id}/shipment`).
4. **Tracking number is mandatory** — the form must reject empty values.
5. `GET /orders/{id}/tracking` returns an outbound URL to the carrier's tracking site (no carrier API integration — IFS-05 is a web link only).

### Post-shipment cancellation (FS-V08)
When a buyer requests cancellation after shipment, the vendor chooses one of three options:
- **Accept with return condition**: buyer must send the parcel back before refund.
- **Accept without return condition**: immediate refund, no return required.
- **Refuse**: cancellation request denied.

### Stock management
Stock is an attribute of `Product`, not a separate entity. When an order is confirmed, decrement `availableQuantity` on each `Product`. When an order is cancelled/returned, restore the quantity.

### Account activation
New buyer accounts start with status `PENDING` until email verification. Admin can suspend (`SUSPENDED`) or delete (`DELETED`) any account.

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

All three SPAs must support **French (fr) and English (en)**. French is the default locale.

### Frontend

- Use **react-i18next** (`i18next` + `react-i18next`).
- Translation files: `src/i18n/fr.json` and `src/i18n/en.json` in each SPA.
- All user-visible strings must go through `t("key")` — no hardcoded UI text.
- A language toggle must be visible on every app (e.g., header).
- Keys are in English, dot-notation, grouped by feature: `order.status.pending`, `auth.login.submit`, etc.

### Backend

- User-facing error messages (returned in API responses) must be localised via Spring's `MessageSource`.
- Message files: `src/main/resources/messages.properties` (default = French), `messages_en.properties`.
- The client sends its locale in the `Accept-Language` header; the backend resolves the appropriate message.
- Internal log messages stay in English only.

---

## Development commands

```bash
# Backend
cd backend
./mvnw spring-boot:run

# Regenerate openapi.yaml
./mvnw springdoc-openapi:generate

# Frontend (any SPA)
cd frontend/buyer-portal   # or vendor-backoffice / admin-console
npm install
npm run dev

# Full stack with Docker Compose
docker compose up --build

# Run DB migrations only
./mvnw flyway:migrate
```

---

## What Claude must do in this directory

- Strictly enforce Controller → Service → Repository: no layer skip, no Repository injection in a Controller.
- Generate code that strictly matches the domain model and business rules above.
- Verify identifiers against the architecture before creating new entities or endpoints.
- Before implementing a feature, verify it in the architecture document, and ask to update it before generating code.
- Never create a separate `Stock` entity — stock lives on `Product` (`availableQuantity`, `stockAlertThreshold`).
- Never skip `OrderLine` — an `Order` without line items is an architecture violation.
- **All code and all comments must be in English** — class names, method names, variables, field names, inline comments, Javadoc, SQL columns, API paths, Git commit messages.
- User-facing strings (UI labels, email templates, API error messages) must be localised in both **fr** and **en** — never hardcoded in a single language.
- **Every public method and every public field must have a Javadoc comment.** Minimum: one sentence describing what it does. For methods: document parameters (`@param`), return value (`@return`), and checked exceptions (`@throws`) when present. Private/package-private members do not require Javadoc.
- Flyway migration scripts must be sequential: `V1__init_schema.sql`, `V2__...`, etc.
- When generating a frontend screen, cross-reference the corresponding wireframe in `../architecture/04-physical-architecture/diagrams/ihm/`.
- Do not implement a carrier API integration — IFS-05 is a plain outbound URL, nothing more.
