# Performance (10,000 employees, SQLite file, JDK 25, laptop, local curl, warm JVM)

Measured with `curl -w %{time_total}` against the packaged jar after seeding.

| Request | Time |
|---|---|
| `GET /api/employees?size=25` | 8-13 ms |
| `GET /api/employees?size=25&page=399&sort=lastName,asc` (last page) | 8 ms |
| `GET /api/employees?size=200&sort=salaryUsd,desc&page=10` | 23-34 ms |
| `GET /api/employees?country=US&department=Engineering&level=L3&sort=hireDate,desc` | 15 ms |
| `GET /api/employees?q=smith` (contains-search, full scan) | 30-36 ms |
| `GET /api/analytics/summary` | 32-36 ms |
| `GET /api/analytics/by-country` | 24 ms |
| `GET /api/analytics/pay-gap` | 32 ms |
| `GET /api/analytics/outliers` | 33-38 ms |
| `GET /api/export/employees` (all 10,000 rows, ~1 MB, streamed) | 0.31 s |
| Seeder (10,000 employees + 10,000 history rows, JDBC batches, one transaction) | ~0.30 s |

The first request after boot can take ~130 ms (JIT/class loading). Employee search stays far below the 200 ms target.

## Why it is fast
- Persisted `salary_usd` plus indexes on country, department, level, salary_usd, last_name (with id), unique email.
- Server-side paging with a stable `id` tie-break; count and page are two indexed queries.
- Analytics read one narrow projection (id, name, country, dept, level, gender, salary_usd) and compute in Java.
- WAL journal mode and a small Hikari pool.

## Known limits
`q` uses `LIKE '%text%'`, which scans the table; fine at 10k rows. Beyond ~1M rows, use FTS5 or prefix search.
Analytics recompute per request (~30 ms); add caching or SQL aggregates if the data grows.
