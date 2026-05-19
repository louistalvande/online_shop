# COTS Report — Application e-commerce

Versions sourced from `backend/pom.xml` and `frontend/*/package.json`.

CVE status column: `OK` = no known critical/high CVE at the pinned version per last scan;
`⚠` = known CVE(s) — see notes; `—` = managed by Spring Boot BOM (scan covers the resolved version).

## Runtime

| Component | Version | Role | CVE status |
|---|---|---|---|
| Eclipse Temurin (JDK) | 21 LTS | Java runtime — API Spring Boot container | OK |
| Node.js | 20 LTS | JavaScript runtime — Vite build, dev container | OK |
| Maven Wrapper (`mvnw`) | 3.9 | Reproducible backend build, tests, OpenAPI generation | OK |

## Backend framework

| Component | Version | Role | CVE status |
|---|---|---|---|
| Spring Boot | **3.3.4** | Main application framework — autoconfiguration, lifecycle, BOM | OK |
| Spring Web MVC | — | REST controllers, filters, dispatch | — |
| Spring Security | — | JWT authentication, CSRF protection, role-based access control | — |
| Spring Data JPA | — | JPA repository abstraction — PostgreSQL access | — |
| Hibernate ORM | — | JPA implementation — entity mapping, JPQL | — |
| Spring Data Redis | — | Redis access — cart persistence, JWT revocation | — |
| Spring AOP | — | Security event logging via aspects | — |
| Jakarta Bean Validation (Hibernate Validator) | — | Incoming DTO validation (`@Valid`) | — |
| Lombok | — | Java boilerplate reduction (getters, constructors, builders) | — |
| springdoc-openapi webmvc-ui | **2.6.0** | Automatic OpenAPI 3 spec generation and Swagger UI | OK |

## Security & authentication

| Component | Version | Role | CVE status |
|---|---|---|---|
| JJWT (`jjwt-api` / `jjwt-impl` / `jjwt-jackson`) | **0.12.6** | JWT creation, HMAC-SHA-256 signing and validation | OK |

## Persistence & migrations

| Component | Version | Role | CVE status |
|---|---|---|---|
| PostgreSQL (server) | **16** | Main relational database — all business entities | OK |
| PostgreSQL JDBC Driver | — | JDBC connector Java ↔ PostgreSQL | — |
| Flyway Core + flyway-database-postgresql | — | Versioned SQL schema migrations | — |
| Redis (server) | **7** | Distributed cache — sessions, carts, brute-force counters | OK |

## External integrations

| Component | Version | Role | CVE status |
|---|---|---|---|
| Stripe Java SDK | **26.7.0** | Card payment submission, webhook reception, refunds (IFS-04) | OK |
| SendGrid Java SDK | **4.10.2** | Multilingual transactional email delivery (IFS-06) | OK |

## Frontend framework

| Component | Version | Role | CVE status |
|---|---|---|---|
| React | **18.3.1** | UI framework — three SPAs | OK |
| TypeScript | **5.5.3** | Static typing for all three SPAs | OK |
| Vite | **5.4.1** | Bundler and HMR dev server | OK |
| @vitejs/plugin-react | **4.3.1** | Babel / React Fast Refresh integration in Vite | OK |
| i18next | **24.0.0** | Internationalisation engine — fr / en support | OK |
| react-i18next | **15.0.0** | React ↔ i18next binding | OK |

## Infrastructure & deployment

| Component | Version | Role | CVE status |
|---|---|---|---|
| Nginx | stable (latest) | Reverse proxy, TLS termination, static SPA serving | OK |
| Docker Engine | 26+ | Component containerisation | OK |
| Docker Compose | 2.x | Local multi-container orchestration (development) | OK |
| Kubernetes | 1.30+ | Production orchestration | OK |

---

## Vulnerability management

### Backend — OWASP Dependency Check (Maven)

Add the plugin to `backend/pom.xml` under `<build><plugins>`:

```xml
<plugin>
  <groupId>org.owasp</groupId>
  <artifactId>dependency-check-maven</artifactId>
  <version>10.0.3</version>
  <configuration>
    <failBuildOnCVSS>7</failBuildOnCVSS>   <!-- fail on High/Critical -->
    <suppressionFile>owasp-suppressions.xml</suppressionFile>
    <formats>HTML,JSON</formats>
  </configuration>
  <executions>
    <execution>
      <goals><goal>check</goal></goals>
    </execution>
  </executions>
</plugin>
```

Run inside the `shop-backend` container:

```bash
./mvnw dependency-check:check
# Report: target/dependency-check-report.html
```

The `owasp-suppressions.xml` file at the root of `backend/` must document every accepted false positive with a justification and an expiry date.

### Frontend — npm audit

Run inside the `shop-frontend` container (repeat for each app):

```bash
cd /workspace/buyer-portal      && npm audit --audit-level=high
cd /workspace/vendor-backoffice && npm audit --audit-level=high
cd /workspace/admin-console     && npm audit --audit-level=high
```

Auto-fix safe upgrades:

```bash
npm audit fix
```

Do **not** run `npm audit fix --force` without reviewing the changes — it may introduce breaking major-version upgrades.

### Scan cadence

| Trigger | Action |
|---|---|
| Every dependency version bump (PR) | Run both scans in CI; block merge on CVSS ≥ 7 |
| Weekly scheduled CI job | Full scan on `main`; notify on new findings |
| New CVE published affecting a listed component | Patch within 30 days (High), 7 days (Critical) |

### Accepted false positives

Document accepted false positives in `backend/owasp-suppressions.xml`. Each entry must include:

```xml
<suppress until="YYYY-MM-DD">
  <notes>Reason this CVE does not apply or is mitigated.</notes>
  <cve>CVE-XXXX-XXXXX</cve>
</suppress>
```
