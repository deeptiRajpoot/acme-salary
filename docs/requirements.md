# Requirements — ACME Salary Management

**Persona:** HR Manager at ACME (10,000 employees, multiple countries), currently working in Excel.

## Goal
Replace Excel with a web app where the HR manager can maintain salary data and quickly answer: *how does the org pay people, and is it fair?*

## Scope & features
| # | Feature | Why |
|---|---|---|
| 1 | Employee list: server-side search, filter (country, department, level), sort, paging | 10k rows must stay fast and usable |
| 2 | Add / edit / remove employee | Core maintenance |
| 3 | Salary change with reason; append-only history per employee | Auditability, answers "what changed and why" |
| 4 | Multi-currency salaries, normalized to USD for comparison | Employees span countries |
| 5 | Pay dashboard: headcount, payroll total, median/P25/P75/P90 by country, department, level | Directly answers "how do we pay people" |
| 6 | Pay-equity signals: compa-ratio vs band, gender/tenure gaps, statistical outliers | Highest-value insight for HR |
| 7 | CSV import (validated, row-level error report) and export | Migration path from Excel; keeps HR in control |
| 8 | Seed script generating 10,000 realistic, reproducible employees | Demo and testing |

## Deliberately left out (and why)
- **Payroll, tax, deductions:** a different, regulated domain; not needed to understand pay distribution.
- **Authentication / roles / SSO:** single persona for this phase; adds weight without product insight. Would be first post-MVP item.
- **Approval workflows:** HR manager is the sole editor; workflow adds complexity with no stated need.
- **Benefits, bonuses, equity:** base salary only, to keep analytics clean and comparable.
- **Live FX rates:** a static rates table is enough for comparison; real feed is an integration risk.
- **Mobile app / multi-tenant:** desktop web, one organization.

## Success criteria
Employee search/page returns in <200 ms at 10k rows; every salary change is traceable; HR can answer pay-by-country/department/level questions in under a minute without Excel; unit tests cover core logic and run in seconds.

## Key assumptions
One salary per employee (annual base); USD as reporting currency; gender and tenure are available fields for equity analysis.
