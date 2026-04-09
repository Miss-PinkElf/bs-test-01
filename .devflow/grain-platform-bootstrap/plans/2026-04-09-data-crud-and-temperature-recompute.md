# Data CRUD And Temperature Recompute Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete CRUD for `grain_temp_record` and `sensor_data`, and automatically recompute `grain_temp_summary` real warnings whenever temperature records change.

**Architecture:** Extend the existing Spring Boot + MyBatis service layer instead of introducing new modules. Temperature CRUD operates on raw point records and triggers a same-warehouse same-time summary rebuild; humidity and CO2 continue to use direct single-row CRUD on `sensor_data`. The Vue data page becomes the single management entry for create, update, delete, query, and linked refresh.

**Tech Stack:** Spring Boot 3, MyBatis XML mapper, MySQL, Vue 3, Element Plus, Axios, ECharts

---

## Chunk 1: Backend CRUD Surface

### Task 1: Add request DTOs for create/update payloads

**Files:**
- Create: `backend/src/main/java/com/grain/platform/dto/grain/GrainTempRecordUpsertRequest.java`
- Create: `backend/src/main/java/com/grain/platform/dto/sensor/SensorDataUpdateRequest.java`

- [ ] Define validated payloads for temperature record create/update and sensor data update
- [ ] Keep fields aligned with current UI scope only

### Task 2: Extend mapper capabilities

**Files:**
- Modify: `backend/src/main/java/com/grain/platform/mapper/GrainTempPointMapper.java`
- Modify: `backend/src/main/resources/mapper/GrainTempPointMapper.xml`
- Modify: `backend/src/main/java/com/grain/platform/mapper/GrainTempRecordMapper.java`
- Modify: `backend/src/main/resources/mapper/GrainTempRecordMapper.xml`
- Modify: `backend/src/main/java/com/grain/platform/mapper/GrainTempSummaryMapper.java`
- Modify: `backend/src/main/resources/mapper/GrainTempSummaryMapper.xml`
- Modify: `backend/src/main/java/com/grain/platform/mapper/SensorDataMapper.java`
- Modify: `backend/src/main/resources/mapper/SensorDataMapper.xml`

- [ ] Add record lookup by id and by warehouse/time
- [ ] Add insert/update/delete for `grain_temp_record`
- [ ] Add summary delete by warehouse/time for empty-temperature edge case
- [ ] Add insert/update/delete for `sensor_data`

### Task 3: Implement service-layer CRUD and recompute

**Files:**
- Modify: `backend/src/main/java/com/grain/platform/service/GrainTempService.java`
- Modify: `backend/src/main/java/com/grain/platform/service/SensorDataService.java`

- [ ] Add create/update/delete methods for temperature records
- [ ] Recompute same-batch summary and real warning after every temperature change
- [ ] Delete stale summary when a warehouse/time slice no longer has raw points
- [ ] Add update/delete methods for sensor data

### Task 4: Expose controller endpoints

**Files:**
- Modify: `backend/src/main/java/com/grain/platform/controller/GrainTempController.java`
- Modify: `backend/src/main/java/com/grain/platform/controller/SensorDataController.java`

- [ ] Add POST/PUT/DELETE endpoints for temperature records
- [ ] Add PUT/DELETE endpoints for sensor data
- [ ] Preserve existing import/list API behavior

## Chunk 2: Frontend Data Management UI

### Task 5: Extend API layer

**Files:**
- Modify: `frontend/src/api/grain.js`

- [ ] Add create/update/delete functions for temperature records
- [ ] Add update/delete functions for sensor data
- [ ] Normalize temperature record payloads for editing forms

### Task 6: Upgrade data page dialogs and tables

**Files:**
- Modify: `frontend/src/views/DataView.vue`

- [ ] Grain mode: switch from summary-only management to raw-record management with CRUD actions
- [ ] Keep summary trend display, but use raw-record table for maintenance
- [ ] Env mode: support create, edit, delete in the existing page
- [ ] Refresh list and chart after every successful mutation

## Chunk 3: Verification

### Task 7: Static verification

**Files:**
- No code changes

- [ ] Run `mvn -q -DskipTests compile` in `backend/`
- [ ] Run `npm run build` in `frontend/`

### Task 8: Runtime smoke

**Files:**
- No code changes

- [ ] Start backend on `8081`
- [ ] Verify temperature record create/update/delete triggers summary and dashboard changes
- [ ] Verify humidity/CO2 create/update/delete refresh list and chart
- [ ] Verify prediction/task endpoints remain unaffected
