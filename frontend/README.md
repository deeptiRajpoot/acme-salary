# ACME Salary Management - Frontend

Angular 21 (standalone components, signals, zoneless) with Angular Material and Chart.js (ng2-charts).
Built against the API contract in `../docs/openapi.yaml` (base path `/api`).

## Prerequisites
Node 20.19+ (or 22.12+) and npm.

## Run
```bash
npm ci
npm start            # http://localhost:4200, /api is proxied to http://localhost:8080 (proxy.conf.json)
```
Start the backend first (`cd ../backend && ./mvnw spring-boot:run`).

## Scripts
| Script | What it does |
|---|---|
| `npm start` | dev server with API proxy |
| `npm test` | unit tests, single run (Vitest through `ng test`, jsdom) |
| `npm run lint` | angular-eslint (includes size, depth and member-accessibility rules) |
| `npm run build` | production build to `dist/acme-salary/browser` |
| `npm run gen:api` | regenerate `src/app/core/api/schema.d.ts` from `../docs/openapi.yaml` |

## Production build
`npm run build` writes static files to `dist/acme-salary/browser`. Serve them from the site root; the Spring Boot
backend does this (static resources plus SPA fallback): the Dockerfile copies that folder into the jar, or for local use
copy it to `backend/static/`.

## API types
`src/app/core/api/schema.d.ts` is generated (do not edit). `core/api/types.ts` holds thin aliases. After changing the
contract run `npm run gen:api` and fix any compile errors; they show exactly where the UI needs to adapt.
Errors: `error.interceptor.ts` turns RFC 7807 responses into a typed `ApiError` (`status`, `message`, `fieldErrors`).

## Layout
```
src/app/
  core/api/     typed API services, ApiError + interceptor, generated schema
  core/         format helpers/pipes, snackbar notifier
  shared/       panel (skeleton + error/retry), confirm dialog, field-error, validators
  features/
    dashboard/  KPIs, distribution, median pay by group (chart/table), pay equity, outliers
    employees/  server-side table, add/edit/salary dialogs, detail + salary history
    import/     CSV upload and result view
```
Each feature is a lazy-loaded route. The employees table pages, sorts and filters on the server only.
