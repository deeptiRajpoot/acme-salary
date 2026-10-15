# Architecture

```mermaid
flowchart LR
  HR[HR Manager] --> UI[React SPA]
  UI -->|REST /api| API[Spring Boot]
  subgraph API[Spring Boot]
    C[Controllers] --> S[Services]
    S --> R[JPA repositories]
    S --> A[Pure analytics: Percentiles, Stats, OutlierDetector, PayGapCalculator, FxConverter]
  end
  R --> DB[(SQLite)]
  Seed[Seeder: 10,000 employees, seed 42] --> DB
```

- One deployable: the built React app is served by Spring Boot as static files.
- `salary_usd` is persisted alongside local salary, so sorting and aggregates stay in SQL and fast.
- Salary changes only via `POST /employees/{id}/salary`, which appends an immutable history row.
- Analytics math lives in dependency-free static classes, unit-tested without a database.
