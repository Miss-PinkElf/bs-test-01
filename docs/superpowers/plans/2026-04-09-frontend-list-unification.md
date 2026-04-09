# Frontend List Unification Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Unify backend page list presentation around Element Plus tables with client-side pagination, while converting dashboard recent alerts to a scrollbar-based incremental loading list.

**Architecture:** Keep all changes in `frontend/` and avoid backend API changes. Add lightweight composables for client-side pagination and incremental list loading, then apply them to dashboard, users, warehouses, data, and prediction pages. Preserve existing business data flow and only adjust presentation and interaction.

**Tech Stack:** Vue 3, Element Plus, Vite

---

## Chunk 1: Shared list presentation helpers

### Task 1: Add client-side pagination composable

**Files:**
- Create: `frontend/src/composables/useClientPagination.js`

- [ ] Add reactive current-page / page-size state
- [ ] Add computed paged list and total count
- [ ] Add page / size change handlers and clamp logic when source data shrinks

### Task 2: Add incremental scroll-list composable

**Files:**
- Create: `frontend/src/composables/useIncrementalList.js`

- [ ] Add visible-count state and visible list projection
- [ ] Add reset and load-more behavior for scroll-driven consumption
- [ ] Keep it generic for dashboard alert streams

## Chunk 2: Apply unified table + pagination to backend pages

### Task 3: Upgrade dashboard list sections

**Files:**
- Modify: `frontend/src/views/DashboardView.vue`

- [ ] Convert warehouse health card list to `el-table`
- [ ] Add client-side pagination for latest grain summaries and warehouse health tables
- [ ] Replace recent alerts stack list with `el-scrollbar` incremental loading

### Task 4: Upgrade user and warehouse list pages

**Files:**
- Modify: `frontend/src/views/UsersView.vue`
- Modify: `frontend/src/views/WarehouseView.vue`

- [ ] Add `el-pagination` below user table
- [ ] Add `el-pagination` below warehouse table
- [ ] Keep current cards / dialogs intact

### Task 5: Upgrade environment data page tables

**Files:**
- Modify: `frontend/src/views/DataView.vue`

- [ ] Add pagination for grain summary table
- [ ] Add pagination for raw grain record table
- [ ] Add pagination for env data table

### Task 6: Upgrade prediction page history and result lists

**Files:**
- Modify: `frontend/src/views/PredictionView.vue`

- [ ] Add pagination for prediction result table
- [ ] Replace history archive card stack with `el-table`
- [ ] Add pagination for prediction history table

## Chunk 3: Verification

### Task 7: Static verification

**Files:**
- No code changes

- [ ] Run `npm run build` in `frontend/`
- [ ] Confirm no compile errors after composable and page refactor
