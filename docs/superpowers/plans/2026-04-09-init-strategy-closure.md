# Init Strategy Closure Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stop automatic demo-database rebuilds on every backend startup while adding an explicit local reset entry for the MySQL demo schema.

**Architecture:** Change the default Spring Boot startup path to preserve existing data by setting SQL init to `never`, then introduce a separate PowerShell reset script that executes `backend/src/main/resources/db/schema.sql` on demand. Update startup messaging and docs so local developers understand the new two-step workflow.

**Tech Stack:** Spring Boot 3 config, PowerShell, MySQL CLI, Markdown docs

---

## Chunk 1: Config And Reset Entry

### Task 1: Close the default startup init behavior

**Files:**
- Modify: `backend/src/main/resources/application.yml`

- [ ] Change `spring.sql.init.mode` from `always` to `never`
- [ ] Keep `schema-locations` pointing at `classpath:db/schema.sql`
- [ ] Add a short comment only if needed for clarity

### Task 2: Add explicit demo reset script

**Files:**
- Create: `scripts/reset-demo-db.ps1`

- [ ] Accept optional host / port / username / password / database parameters with current local defaults
- [ ] Resolve `backend/src/main/resources/db/schema.sql`
- [ ] Check that `mysql` CLI exists
- [ ] Pipe the schema file into `mysql --default-character-set=utf8mb4`
- [ ] Exit non-zero on failure and print a clear success message on completion

## Chunk 2: Developer Entry Points And Docs

### Task 3: Update backend start scripts

**Files:**
- Modify: `scripts/start-backend.ps1`
- Modify: `scripts/start-backend.sh`

- [ ] Print that normal startup no longer auto-resets demo data
- [ ] Point users at `scripts/reset-demo-db.ps1` when they need a clean demo database
- [ ] Preserve existing `8081` startup behavior

### Task 4: Update docs and truth-source references

**Files:**
- Modify: `README.md`
- Modify: `zzz-docs/验证/数据库优先MVP-回归验证清单.md`
- Modify: `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`

- [ ] Explain the new default startup behavior
- [ ] Add the explicit reset command to local verification prerequisites
- [ ] Link this plan from the devflow active plan index

## Chunk 3: Verification And Mission Record

### Task 5: Static verification

**Files:**
- No code changes

- [ ] Run `mvn -q -DskipTests compile` in `backend/`

### Task 6: Runtime smoke

**Files:**
- No code changes

- [ ] Run `.\scripts\reset-demo-db.ps1`
- [ ] Start backend on `8081`
- [ ] Request `http://127.0.0.1:8081/api/dashboard/overview`
- [ ] Confirm startup works after the config change

### Task 7: Update devflow records

**Files:**
- Modify: `.devflow/grain-platform-bootstrap/state.md`
- Modify: `.devflow/grain-platform-bootstrap/decision-log.md`
- Modify: `.devflow/grain-platform-bootstrap/checkpoints.md`

- [ ] Record the chosen init strategy
- [ ] Record fresh verification evidence
- [ ] Set the next priority after this change
