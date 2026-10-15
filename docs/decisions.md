# Decisions & Trade-offs

| Decision | Choice | Why | Cost |
|---|---|---|---|
| Backend | Spring Boot 3 on Java 17, package-by-feature | Fast to build, strong testing story | Not full hexagonal; fine at this scope |
| DB | SQLite | Zero-ops deploy, single file, plenty for 10k rows | No built-in percentile; computed in Java |
| Analytics | SQL aggregates + pure Java percentile/outlier functions | Pure functions are fast and deterministic to test | In-memory per group; fine at 10k rows |
| Money | Local currency + ISO code; USD for comparison via static rates | Multi-country org | Static rates, not live FX |
| API contract | OpenAPI first (`docs/openapi.yaml`) | Backend and frontend built in parallel | Contract must stay in sync |
| Auth | Out of scope | Single persona | First post-MVP item |
| Frontend framework | Angular 21 (standalone, signals, Angular Material, Chart.js)
