# Warehouse CRUD Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete warehouse management CRUD by adding backend update/delete endpoints and frontend edit/delete interactions on the existing warehouse page.

**Architecture:** Extend the existing Spring Boot + MyBatis warehouse stack instead of introducing new modules. Keep the current `WarehouseDto` as the request/response shape, add update/delete methods in mapper/service/controller, and reuse the current Vue dialog for both create and edit flows. Deletion remains hard delete for unreferenced warehouses, while referenced warehouses return a friendly validation error.

**Tech Stack:** Spring Boot 3, MyBatis XML, Vue 3, Element Plus

---

## Chunk 1: Backend Warehouse CRUD

### Task 1: Extend mapper and service support

**Files:**
- Modify: `backend/src/main/java/com/grain/platform/mapper/WarehouseMapper.java`
- Modify: `backend/src/main/resources/mapper/WarehouseMapper.xml`
- Modify: `backend/src/main/java/com/grain/platform/service/WarehouseService.java`

- [ ] Add `update` and `deleteById` mapper methods
- [ ] Add `update` and `delete` service methods
- [ ] Validate that target warehouse exists before update/delete
- [ ] Convert referenced-warehouse delete failures into a friendly business message

### Task 2: Expose controller endpoints

**Files:**
- Modify: `backend/src/main/java/com/grain/platform/controller/WarehouseController.java`

- [ ] Add `PUT /api/warehouses/{id}`
- [ ] Add `DELETE /api/warehouses/{id}`
- [ ] Keep current list/options/create behavior unchanged

## Chunk 2: Frontend Warehouse CRUD

### Task 3: Extend warehouse API helpers

**Files:**
- Modify: `frontend/src/api/grain.js`

- [ ] Add `updateWarehouse`
- [ ] Add `deleteWarehouse`

### Task 4: Upgrade warehouse page interactions

**Files:**
- Modify: `frontend/src/views/WarehouseView.vue`

- [ ] Reuse the dialog for create/edit modes
- [ ] Add per-row edit/delete action buttons
- [ ] Add delete confirmation
- [ ] Track selected warehouse by row click instead of always showing the first row
- [ ] Refresh list and selected detail after every successful mutation

## Chunk 3: Verification

### Task 5: Static verification

**Files:**
- No code changes

- [ ] Run `mvn -q -DskipTests compile` in `backend/`
- [ ] Run `npm run build` in `frontend/`

### Task 6: Runtime smoke

**Files:**
- No code changes

- [ ] Start backend on `8081`
- [ ] Create a temporary warehouse through API
- [ ] Update the temporary warehouse through API
- [ ] Delete the temporary warehouse through API
- [ ] Confirm warehouse list reflects the create/update/delete cycle
