# Codebase Structure

**Analysis Date:** 2026-04-10

## Directory Layout

```
bs-test-01/
├── backend/                    # Spring Boot 3 + MyBatis + MySQL
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/grain/platform/
│       │   ├── GrainPlatformApplication.java
│       │   ├── common/       # ApiResponse, PageResult, GlobalExceptionHandler
│       │   ├── config/       # CorsConfig 等
│       │   ├── controller/   # REST 控制器
│       │   ├── dto/          # 按域分子包：auth, dashboard, grain, prediction, sensor, user, warehouse
│       │   ├── entity/       # 持久化实体
│       │   ├── mapper/       # MyBatis 接口
│       │   ├── service/      # 业务服务
│       │   └── vo/           # 少量 VO（如 IdVO）
│       └── resources/
│           ├── application.yml
│           ├── db/           # 可选 schema 引用（见 yml 中 sql.init）
│           └── mapper/       # MyBatis XML（与接口同名映射）
├── frontend/                 # Vue 3 + Vite + Element Plus + Pinia（主 SPA）
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── styles.css
│       ├── router/index.js
│       ├── layout/ConsoleLayout.vue
│       ├── views/            # 页面级组件
│       ├── api/              # http.js + grain.js（API 与领域封装）
│       ├── stores/auth.js
│       ├── composables/      # 列表分页等可复用逻辑
│       ├── utils/
│       └── mock/             # 演示/兜底数据
├── frontend-next/            # 独立 Next 工程（与本仓库主栈并行存在，非当前主前端）
├── scripts/                  # 如 start-backend.ps1、start-frontend.ps1
├── .devflow/、.explore/、.planning/  # 流程与计划文档（非运行时）
└── zzz-docs/                 # 仓库内设计/验证文档
```

## Directory Purposes

**`backend/src/main/java/com/grain/platform/controller/`:**

- Purpose: 对外 HTTP API，路径前缀多为 `/api/...`。
- Contains: `AuthController`、`DashboardController`、`GrainTempController`、`MetricController`、`PredictionController`、`SensorDataController`、`UserController`、`WarehouseController`。
- Key files: `GrainTempController.java`（粮温 CRUD、导入、分页、汇总）。

**`backend/src/main/java/com/grain/platform/service/`:**

- Purpose: 业务实现；导入、预测、仪表盘等复杂流程在此编排。
- Key files: `GrainTempService.java`、`GrainTempImportService.java`、`SensorDataImportService.java`、`PredictionService.java`。

**`backend/src/main/java/com/grain/platform/mapper/` + `backend/src/main/resources/mapper/`:**

- Purpose: 数据访问；Java 接口与 XML 一一对应（如 `GrainTempRecordMapper.java` / `GrainTempRecordMapper.xml`）。

**`backend/src/main/java/com/grain/platform/dto/`:**

- Purpose: API 输入输出类型，按业务分包便于检索。

**`frontend/src/views/`:**

- Purpose: 路由级页面：`LoginView.vue`、`DashboardView.vue`、`UsersView.vue`、`WarehouseView.vue`、`DataView.vue`（环境数据含粮温模式）、`PredictionView.vue`、`BigScreenView.vue`。

**`frontend/src/api/`:**

- Purpose: HTTP 客户端与后端契约封装。
- Key files: `http.js`（`API_BASE_URL`、`request`、`unwrapPayload`）、`grain.js`（仪表盘、仓库、用户、传感器、粮温、预测等 API 与数据规范化函数）。

**`frontend/src/router/index.js`:**

- Purpose: 路由表、`meta.public` / `meta.requiresAuth`、`beforeEach` 登录校验；`/environment` 挂载 `DataView`，`/data` 重定向到 `/environment`。

**`frontend/src/layout/ConsoleLayout.vue`:**

- Purpose: 控制台侧栏菜单与 `router-view` 容器；菜单路径需与 `router` 子路由一致。

## Key File Locations

**Entry Points:**

- `backend/src/main/java/com/grain/platform/GrainPlatformApplication.java`：Spring Boot 启动类。
- `frontend/src/main.js`：Vue 应用启动。
- `frontend/index.html`：Vite HTML 入口。

**Configuration:**

- `backend/src/main/resources/application.yml`：端口、数据源、MyBatis（勿在文档中复制敏感配置值）。
- `frontend/vite.config.js`：开发服务器 `port` / `host`。
- `backend/src/main/java/com/grain/platform/config/CorsConfig.java`：CORS。

**Core Logic（粮温相关）:**

- `backend/src/main/java/com/grain/platform/service/GrainTempImportService.java`：解析与写库。
- `backend/src/main/java/com/grain/platform/service/GrainTempService.java`：查询、分页、单条 CRUD、汇总、模板字节。
- `frontend/src/api/grain.js`：`importGrainTempFile`、`downloadGrainTempTemplate`、`fetchGrainTempRecordsPage` 等。

**Testing:**

- 后端测试依赖见 `backend/pom.xml`（`spring-boot-starter-test`）；本结构说明不展开用例路径（若后续增加，放在 `backend/src/test/java/`）。

## Naming Conventions

**Files:**

- Java：`PascalCase` 类名，Controller/Service/Mapper 后缀固定。
- Vue：`PascalCase` 组件文件（如 `DataView.vue`）。
- 路由：`frontend/src/router/index.js` 中小写 `path`（如 `/environment`）。

**Directories:**

- Java 包按职责与域划分：`dto/grain`、`controller` 等。
- 前端按类型分：`views`、`layout`、`api`、`stores`。

## Where to Add New Code

**New REST API（推荐步骤）:**

1. 在 `backend/src/main/java/com/grain/platform/controller/` 新增或扩展 `*Controller`，`@RequestMapping("/api/...")` 与动词方法。
2. 在 `service/` 实现业务；需要持久化则在 `mapper/` 增加接口并在 `backend/src/main/resources/mapper/` 增加 XML。
3. 在 `dto/`（必要时 `entity/`、`vo/`）定义请求/响应类型；列表分页优先返回 `PageResult<T>`。
4. 前端在 `frontend/src/api/grain.js`（或按域新建 `frontend/src/api/<domain>.js` 并在视图中引用）增加调用函数，使用 `request()` 以保持 `ApiResponse` 解包一致。

**New Vue 页面（控制台内）:**

1. 在 `frontend/src/views/` 新建 `XxxView.vue`。
2. 在 `frontend/src/router/index.js` 的 `ConsoleLayout` 的 `children` 中注册 `path`、`component`、`meta.title` / `meta.description`。
3. 在 `frontend/src/layout/ConsoleLayout.vue` 的 `navItems` 中增加菜单项，`path` 与路由一致。

**Utilities / 可复用前端逻辑:**

- 共享组合式函数：`frontend/src/composables/`。
- 会话与鉴权：`frontend/src/utils/session.js`、`frontend/src/stores/auth.js`。

## Special Directories

**`.planning/`:**

- Purpose: 项目级需求与本次 codebase 分析文档等。
- Generated: 否。
- Committed: 按团队约定（通常与仓库一同版本管理）。

**`frontend-next/`:**

- Purpose: 与 `frontend/` 分离的 Next.js 工程；添加功能时先确认产品主栈是否为 Vite SPA，避免改错工程。

**`backend/src/main/resources/mapper/`:**

- Purpose: MyBatis SQL 映射。
- Generated: 否。
- Committed: 是。

---

*Structure analysis: 2026-04-10*
