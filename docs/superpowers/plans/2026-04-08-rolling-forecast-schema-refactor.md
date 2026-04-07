# Rolling Forecast Schema Refactor Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the old short-horizon multi-metric schema with a grain-temperature rolling-forecast schema that supports point imports, summaries, prediction versioning, correction, and alerts.

**Architecture:** Keep user, role, warehouse, and metric dictionary tables stable. Split grain temperature into point definition, raw records, and summary snapshots. Evolve prediction storage into task-version chains plus daily result rows with validation and alert fields.

**Tech Stack:** MySQL 8, Spring Boot, MyBatis, Vue 3, Vite

---

## Chunk 1: Schema Rewrite

### Task 1: Rewrite database tables in `schema.sql`

**Files:**
- Modify: `backend/src/main/resources/db/schema.sql`

- [ ] **Step 1: Replace old prediction-only schema assumptions**
- [ ] **Step 2: Add `grain_temp_point`, `grain_temp_record`, `grain_temp_summary`**
- [ ] **Step 3: Upgrade `prediction_task` to rolling-version structure**
- [ ] **Step 4: Upgrade `prediction_result` to validation/correction structure**
- [ ] **Step 5: Keep `sensor_data` only for humidity and co2**

### Task 2: Seed story-shaped mock data

**Files:**
- Modify: `backend/src/main/resources/db/schema.sql`

- [ ] **Step 1: Seed warehouses, users, roles, metrics**
- [ ] **Step 2: Seed grain temperature point definitions**
- [ ] **Step 3: Seed grain temperature summaries for stable/risky/correction scenarios**
- [ ] **Step 4: Seed rolling prediction tasks for initial and corrected rounds**
- [ ] **Step 5: Seed prediction results with actuals, errors, and warnings**

## Chunk 2: Verification

### Task 3: Validate schema coverage against truth sources

**Files:**
- Check: `zzz-docs/设计文档/数据库设计-滚动预测与高温预警版.md`
- Check: `zzz-docs/设计文档/滚动预测改造-字段与接口变更方案.md`
- Check: `zzz-docs/设计文档/mock数据设计-滚动预测闭环版.md`

- [ ] **Step 1: Confirm new tables and columns exist**
- [ ] **Step 2: Confirm mock data covers import, summary, first forecast, correction, alert**
- [ ] **Step 3: Confirm no old short-horizon assumptions remain in schema comments**

Plan complete and saved to `docs/superpowers/plans/2026-04-08-rolling-forecast-schema-refactor.md`. Ready to execute?
