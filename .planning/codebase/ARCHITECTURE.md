# Architecture

**Analysis Date:** 2026-04-10

## Pattern Overview

**Overall:** 单体 Spring Boot REST API（端口见 `backend/src/main/resources/application.yml`）+ 独立 Vue 3 SPA（Vite 开发服务器），前后端通过 HTTP/JSON 与少量文件上传/下载交互。

**Key Characteristics:**

- 后端采用经典三层：**Controller → Service → Mapper（MyBatis）**，持久化 MySQL。
- 统一 JSON 信封：`ApiResponse<T>`（`code` / `message` / `data`），见 `backend/src/main/java/com/grain/platform/common/ApiResponse.java`。
- 全局异常由 `GlobalExceptionHandler` 转为 `ApiResponse`，校验失败走 `MethodArgumentNotValidException` 分支。
- 未引入 Spring Security；认证为演示向：`AuthController` 登录写库校验，`/api/auth/me` 可通过请求头 `X-Demo-Username` 切换当前用户（缺省行为见 `AuthController` 实现）。
- 跨域由 `CorsConfig` 注册 `CorsFilter`，允许常见方法与通配源模式。

## Layers

**Controller（REST 适配层）:**

- Purpose: 映射 HTTP 路径与动词，绑定查询参数 / `@RequestBody` / `MultipartFile`，返回 `ApiResponse` 或少数场景下的 `ResponseEntity`（如模板文件流）。
- Location: `backend/src/main/java/com/grain/platform/controller/`
- Contains: `*Controller.java`（如 `GrainTempController`、`AuthController`、`PredictionController` 等）。
- Depends on: 对应 `service` 包中的 `@Service`。
- Used by: 浏览器或 `frontend` 中 axios 调用。

**Service（业务与编排）:**

- Purpose: 事务边界内的业务规则、分页计算、跨表编排；复杂解析（如 Excel）可拆到专门 Service。
- Location: `backend/src/main/java/com/grain/platform/service/`
- Contains: `GrainTempService`、`GrainTempImportService`、`SensorDataService`、`PredictionService`、`AuthService` 等。
- Depends on: `mapper` 接口、其他 Service、必要时 Apache POI（粮温导入）。
- Used by: Controller；Service 之间可注入（如 `PredictionService` 使用 `GrainTempService` 拉取序列）。

**Mapper（数据访问，MyBatis）:**

- Purpose: SQL 与结果映射；接口在 Java，XML 在 classpath。
- Location:
  - 接口: `backend/src/main/java/com/grain/platform/mapper/*.java`
  - XML: `backend/src/main/resources/mapper/*.xml`
- Contains: `@Mapper` 接口；查询可直接映射到 DTO（如 `GrainTempRecordItemDto`）或实体。
- Depends on: MySQL（数据源配置在 `application.yml`，勿将凭据写入文档或提交到公共说明）。
- Used by: Service。

**DTO / Entity / VO:**

- Purpose:
  - `dto/*`: 入参、出参、列表项等 API 契约（多为 `record`）。
  - `entity/*`: 与表结构对应的持久化模型。
  - `vo/*`: 少量响应封装（如 `vo/common/IdVO.java`）。
- Location: `backend/src/main/java/com/grain/platform/dto/`、`entity/`、`vo/`

**Common & Config:**

- `common/GlobalExceptionHandler.java`、`common/PageResult.java`
- `config/CorsConfig.java`

## Data Flow

**典型 JSON 请求（以仪表盘为例）:**

1. 前端 `frontend/src/api/http.js` 的 `request()` 发起 HTTP，响应经 `unwrapPayload` 解 `ApiResponse.data`。
2. `DashboardController`（`backend/src/main/java/com/grain/platform/controller/DashboardController.java`）接收请求。
3. `DashboardService` 编排，调用 `DashboardMapper` 等读库。
4. 返回 `ApiResponse.success(dto)`。

**粮温导入（grain-temp import）端到端:**

1. 前端：`frontend/src/api/grain.js` 使用 `axios.post` 将 `FormData` 发到 `POST /api/grain-temp/import`（`multipart/form-data`）；模板下载用 `window.open` 打开 `GET /api/grain-temp/import/template`。
2. `GrainTempController.importData` 接收 `MultipartFile`，委托 `GrainTempService.importData` → `GrainTempImportService.importData`。
3. `GrainTempImportService`：
   - `parse(file)`：按扩展名走 CSV 或 Excel；Excel 区分「固定模板」（检测 `warehouseId` + `zoneCode` 标记行）与「行式表」。
   - 校验：单文件内 `warehouseId` 与 `collectedAt` 必须一致；仓库存在性由 `WarehouseMapper` 校验。
   - 对每行：按 `(warehouseId, zoneCode, layerNo, pointNo)` 查找或创建 `GrainTempPoint`，组装 `GrainTempRecord` 列表；`grainTempRecordMapper.upsertBatch`；再 `buildSummary` 并 `grainTempSummaryMapper.upsert`。
4. 返回 `GrainTempImportResultDto` 封装批次号、条数、预警摘要等。

**粮温记录分页查询:**

1. `GET /api/grain-temp/records`：`GrainTempController` 收集 `warehouseId`、时间范围、`zoneCode`、`layerNo`、`pointNo`、`tempMin`、`tempMax`、`keyword`、`pageNum`、`pageSize`。
2. `GrainTempService.listRecordPage` 规范化页码并调用 `GrainTempRecordMapper.countByCondition` 与 `selectPageByCondition`。
3. 返回 `PageResult<GrainTempRecordItemDto>`（`list` / `pageNum` / `pageSize` / `total`）。

**筛选选项:**

- `GET /api/grain-temp/records/filter-options` → `GrainTempService.recordFilterOptions` → Mapper 去重查询 `zoneCodes` / `layerNos` / `pointNos`。

**State Management:**

- 前端会话：`frontend/src/stores/auth.js`（Pinia）+ `frontend/src/utils/session.js` 持久化登录态；路由守卫在 `frontend/src/router/index.js` 中根据 `meta.public` / `requiresAuth` 与 `getSession()` 跳转。

## Key Abstractions

**ApiResponse:**

- Purpose: 统一成功与错误形态。
- Examples: `backend/src/main/java/com/grain/platform/common/ApiResponse.java`
- Pattern: `success(data)` / `error(code, message)`；与前端 `unwrapPayload` 配对。

**PageResult:**

- Purpose: 服务端分页列表载体。
- Examples: `backend/src/main/java/com/grain/platform/common/PageResult.java`
- Pattern: `list`、`pageNum`、`pageSize`、`total`。

**GrainTempImportService 解析策略:**

- Purpose: 将多种文件形态转为内部 `ImportRow` record，再写库。
- Examples: `backend/src/main/java/com/grain/platform/service/GrainTempImportService.java`
- Pattern: `parse` 分派 CSV / Excel；Excel `isFixedTemplateSheet` 后 `parseFixedTemplate` 按 zone 矩阵块解析。

## Entry Points

**后端 JVM 入口:**

- Location: `backend/src/main/java/com/grain/platform/GrainPlatformApplication.java`
- Triggers: `spring-boot:run` 或打包 jar 执行。
- Responsibilities: 组件扫描、内嵌 Tomcat、加载 `application.yml` 与 MyBatis 映射。

**前端应用入口:**

- Location: `frontend/src/main.js`
- Triggers: Vite `npm run dev` / `vite build` 产物由静态服务器托管。
- Responsibilities: 创建 Vue 应用，挂载 Pinia、Router、Element Plus。

**路由根:**

- Location: `frontend/src/App.vue`（仅 `<router-view />`），实际页面由 `frontend/src/router/index.js` 定义。

## Error Handling

**Strategy:** `@RestControllerAdvice` 集中处理，业务非法参数用 `IllegalArgumentException` 映射为 HTTP 语义上的 400 等价（通过 `code` 字段表达）。

**Patterns:**

- `IllegalArgumentException` → `ApiResponse.error(400, message)`（`GlobalExceptionHandler`）。
- `MethodArgumentNotValidException` → 取首个字段错误文案。
- 其他 `Exception` → 记录日志后 `500` 等价响应。

## Cross-Cutting Concerns

**Logging:** SLF4J（如 `AuthController`、`PredictionController` 中的 `Logger`）。

**Validation:** Jakarta Validation（`@Valid`）作用于部分 `@RequestBody` record DTO。

**Authentication:** 演示级：`AuthService` + 数据库用户；无 JWT 过滤器链；部分接口依赖前端会话与可选请求头。

---

*Architecture analysis: 2026-04-10*
