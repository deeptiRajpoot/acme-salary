# ACME Salary Management — Build Plan (Java + React)

## Goal
HR-manager web app over 10,000 employees' salary data (replacing Excel), able to answer "how does the org pay people?". Judged on engineering judgment and product thinking, not complexity.

## The "stand out" thesis
Pay analytics first, CRUD second.
1. **Pay insights dashboard**: distribution by country/department/level, median/P25/P75/P90, compa-ratio vs pay band, outliers (> ±2σ vs peers in same job+level+country), planted gender/tenure gaps in seed so the dashboard tells a story.
2. **Multi-currency**: salary stored in local currency + ISO code; analytics normalize to USD via a rates table.
3. **Salary history / audit trail**: append-only (who, when, old → new, reason).
4. **Scale-aware**: server-side paging/sort/filter/search with indexes; measured timings in `docs/performance.md`.
5. **Excel migration path**: validated CSV import (with error report) and export.
6. **Visible AI use**: `docs/ai/` with prompts, accepted/rejected output and why.

## Stack & architecture
- **Backend**: Java 21, Spring Boot 3 (Web, Data JPA, Validation), Maven, Flyway. Package-by-feature (`employee`, `salary`, `analytics`, `importexport`); controller → service → repository; DTOs as records. Full hexagonal is too heavy for scope (recorded in `docs/decisions.md`).
- **DB**: SQLite (zero-ops deploy). Tables: `employee`, `department`, `job_level`, `country`, `currency_rate`, `salary_history`. Indexes on country, department, job, salary, name.
- **Analytics**: SQL aggregates for counts/avg/min/max; percentiles and outliers as pure Java functions over per-group lists (fine at 10k rows).
- **API**: REST + springdoc OpenAPI. `GET /employees` (page/sort/filter/search), `POST/PUT/DELETE`, `POST /employees/{id}/salary`, `GET /analytics/{summary|by-country|by-department|distribution|outliers}`, CSV import/export. Errors as RFC 7807 `ProblemDetail`.
- **Frontend**: React + TypeScript + Vite, MUI (DataGrid server-side mode), TanStack Query, Recharts, typed client generated from OpenAPI. Pages: Dashboard, Employees (grid + edit drawer), Employee detail (history), Import.
- **Auth**: deliberately out (single persona).

## Out of scope
Payroll/tax, auth/roles/SSO, approval workflows, benefits/equity, multi-tenant, live FX feed, mobile. (Reasoning in REQUIREMENTS.md.)

## Phases & incremental commits
1. `docs: requirements (1 page), plan, decisions skeleton`
2. `chore: scaffold` (Spring Boot + Vite app, `.gitignore`, CI, repo `CLAUDE.md`)
3. `feat(domain): entities + Flyway schema` + tests
4. `feat(seed): deterministic 10k seeder` — fixed RNG seed, realistic countries/currencies/bands, skewed distributions, planted outliers/gaps; JDBC batch insert in one transaction (<5s)
5. `feat(api): employee CRUD + paging/filter/search` + service tests
6. `feat(api): salary change + history` + tests
7. `feat(api): analytics endpoints` + tests (percentiles, FX, outliers as pure functions)
8. `feat(ui): employees grid + edit` (+ Vitest/RTL)
9. `feat(ui): analytics dashboard`
10. `feat: CSV import/export`
11. `perf: indexes + measurements`
12. `chore: Dockerfile + deploy` (single container: Spring Boot serves built React; Render/Fly.io; seed on first boot)
13. `docs: README, Mermaid architecture diagram, AI log, demo link`

## Fast-execution strategy (agentic AI)
- Repo-level `CLAUDE.md` (conventions, commands, test rules), committed as an AI artifact.
- Spec-first: requirements → OpenAPI contract → generated TS client keeps FE/BE in sync.
- Parallelize: one agent on backend slices, one on frontend against the contract; review each diff, keep commits small.
- Test-first on pure logic (percentiles, FX, outliers); skip brittle snapshot tests.
- Keep `docs/ai/log.md`: prompt → result → what you corrected.
- Timeline: Day 1 backend + seed + tests; Day 2 UI + analytics; Day 3 deploy + docs + video.

## Test strategy
JUnit 5 + AssertJ + Mockito; `@DataJpaTest`/in-memory DB for repositories; plain unit tests for services and analytics math; fixed clock and RNG seed. Cover validation, paging/filter, history append, FX normalization, percentile/outlier math, seeder reproducibility. Frontend: Vitest + RTL for grid and form basics. CI runs both.

## Deliverables checklist
- [ ] Requirements doc (1 page)
- [ ] Plan / decisions / Mermaid architecture diagram
- [ ] Seed script (10,000 employees)
- [ ] Tests + CI
- [ ] Dockerfile + live URL
- [ ] Demo video (≤3 min: import → search → raise salary → history → dashboard insight)
- [ ] AI log, performance note
- [ ] Incremental git history
- [ ] Reply to email with repo link

## Verification
`mvn test` and `npm test` green; seed yields exactly 10,000 rows and is reproducible; employee list <200 ms at 10k rows; dashboard numbers cross-checked against raw SQL; deployed URL smoke-tested.

## Defaults (change if needed)
UI lib: MUI · DB: SQLite · Hosting: Render single container · Base currency: USD · Demo: Loom.
