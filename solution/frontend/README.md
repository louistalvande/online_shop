# Workspace

Three React + Vite apps sharing the same repo.

| App | Base URL | Port |
|-----|----------|------|
| buyer-portal | http://localhost:5173/ | 5173 |
| vendor-backoffice | http://localhost:5174/vendor/ | 5174 |
| admin-console | http://localhost:5175/admin/ | 5175 |

All apps proxy `/api` requests to `http://localhost:8080` by default (override with `API_TARGET`).

## Install

```bash
cd buyer-portal && npm install
cd ../vendor-backoffice && npm install
cd ../admin-console && npm install
```

## Dev

```bash
# Run all three (separate terminals)
cd buyer-portal && npm run dev
cd vendor-backoffice && npm run dev
cd admin-console && npm run dev
```

## Build

```bash
cd buyer-portal && npm run build
cd vendor-backoffice && npm run build
cd admin-console && npm run build
```

## Preview (built output)

```bash
cd buyer-portal && npm run preview
cd vendor-backoffice && npm run preview
cd admin-console && npm run preview
```
