# ACME Salary Management

Web app for an HR manager to manage salaries for 10,000 employees across countries and answer
**"how does the org pay people?"** Pay analytics first, CRUD second.

- **Backend:** Java 17+, Spring Boot 3.5, JPA, Flyway, SQLite → [backend/README.md](backend/README.md)
- **Frontend:** Angular 21 (standalone, signals) + TypeScript, Angular Material, Chart.js (ng2-charts) → [frontend/README.md](frontend/README.md)
- **Contract:** [docs/openapi.yaml](docs/openapi.yaml) (both sides built against it)

## Run locally
```bash
# terminal 1: API on :8080 (seeds 10,000 employees on first start)
cd backend && ./mvnw spring-boot:run
# terminal 2: UI on :4200, proxies /api to :8080 (Node 20.19+)
cd frontend && npm ci && npm start
```
Or as one container: `docker build -t acme-salary . && docker run -p 8080:8080 -v acme-data:/data acme-salary`

## Login
Sign-up is **invite-only**. A default HR profile is created on startup: **username `hr` / password `Hr@12345`**
and a default Owner: **`owner` / `Acme@7878`** (override with `acme.auth.hr.*` / `acme.auth.owner.*` username|password|email). Signed-in users create single-use, 7-day invite links under
*Invite users*; the invitee opens the link, picks a name/username/password, and gets the invited email and designation
(HR or Owner; only an Owner can invite an Owner).
Auth endpoints: `POST /api/auth/signin|signup|signout|invites`, `GET /api/auth/me`, `GET /api/auth/invites/{token}`.
Note: the other `/api/*` endpoints are not yet enforced server-side.

## Test
`cd backend && ./mvnw test` (70 tests) · `cd frontend && npm test` (33 tests, Vitest via `ng test`)

## Docs
[Requirements](docs/requirements.md) · [Plan](docs/plan.md) · [Decisions](docs/decisions.md) ·
[Architecture](docs/architecture.md) · [AI usage log](docs/ai/log.md) · [Performance](backend/PERFORMANCE.md)


## Enhancements in mind
### Security and auth
- **Password management:** add change-password and token-based reset endpoints (replace the `mailto:` "Forgot password?" link) and force a password change on first login for seeded accounts.
- **Seeded credentials:** fail startup in a `prod` profile unless `acme.auth.*.password` is set from the environment; make `ensureUser` update the stored hash when the configured password changes.
- **Brute force:** rate limiting or lockout on `/api/auth/signin`.
- **Audit log:** record who viewed or changed what (salary changes, invites, imports, role changes) and expose it to the Owner role.
- **Role-based access:** confirm HR vs Owner is enforced on every sensitive endpoint, with a backend test per role and endpoint.

### Backend quality
- **Database:** move from SQLite to Postgres for deployment (Flyway is already in place) and test against it with Testcontainers.
- **Performance:** server-side pagination, sorting and filtering for the employee list; cache or precompute dashboard stats (percentiles, pay equity, outliers) and invalidate on writes.
- **Operations:** Actuator health checks, structured logs with request IDs, a consistent error format.

### Features
- **Dashboard filters:** country, department and level filters, plus CSV or PDF export of the panels.
- **Import:** dry-run preview with a row-level error download before committing.
- **Currency conversion:** show the FX rate and date used for the USD figures.
- **Compensation bands:** salary bands per level, flagging out-of-band employees alongside the z-score outliers.

### Frontend and tooling
- **Two frontends:** `frontend` (Angular) and `frontend-react` both exist; pick one or document why both are kept.
- **End-to-end tests:** Playwright coverage for sign-in, add employee, import and the dashboard.
- **CI and containers:** pipeline running lint, unit tests, build and Docker image build; a `docker-compose` file for backend, frontend and database.
- **Accessibility:** keyboard and screen-reader checks for dialogs and tables.
- **Docs:** keep the OpenAPI spec (springdoc) current and add an architecture note.
