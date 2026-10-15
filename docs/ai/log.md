# AI usage log

**Tool:** Claude Code. **Approach:** contract-first, two parallel agents, human review at each hand-off.

| Step | What I asked | What I reviewed / corrected |
|---|---|---|
| Plan | Compare approaches, pick a differentiator | Chose pay analytics + audit trail over plain CRUD; changed stack to Java + React |
| Contract | Wrote `docs/openapi.yaml` first | Frontend agent flagged missing `required` fields and sort names; I tightened the contract |
| Backend agent | Spring Boot API, seed, analytics, tests in `backend/` | Agent reported 404, z-score and pay-gap rules the contract lacked; documented them |
| Frontend agent | React UI against the contract, mocked API in tests | Types regenerated from the final contract; type-check, tests and build re-run by me |
| Integration | Booted the jar with the built UI, curled real endpoints | Verified 10,000 rows, history on salary change, 400/404 problem responses |

## Corrections made to AI output
- Backend agent switched the project to JDK 25 / Spring Boot 3.5 on its own; I set it back to Java 17 (matches the machine and Dockerfile) and re-ran all 70 tests.
- Agents were told not to touch each other's folders; no merge conflicts resulted.
