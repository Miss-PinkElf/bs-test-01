# External Integrations

**Analysis Date:** 2026-04-10

## APIs & External Services

**第三方 SaaS / 公网 HTTP API：**
- 未检出对第三方 REST/Feign/WebClient 的调用；后端控制器集中在 `backend/src/main/java/com/grain/platform/controller/`（如 `AuthController`、`GrainTempController` 等），提供自有 REST API。

**内部前后端契约：**
- 浏览器 → Spring Boot：`frontend/src/api/http.js` 使用 `axios`，`baseURL` 为 `VITE_API_BASE` 或默认 `http://localhost:8081`。
- 统一响应包装：前端 `unwrapPayload` 期望 `code` / `data` 结构（`frontend/src/api/http.js`）；验收脚本校验 `code -eq 200`（`scripts/run-acceptance-smoke.ps1`）。

## Data Storage

**Databases:**
- MySQL — 应用数据源配置在 `backend/src/main/resources/application.yml`（JDBC URL 指向库名 `grain_env_predict`，时区 `Asia/Shanghai`，字符集相关参数在 URL 中）。
- 连接与凭据：**已在配置文件中存在**；文档不摘录具体用户名或口令。部署时使用环境特定配置或 Spring 配置外部化覆盖。
- 客户端：Spring `DataSource` + MyBatis（`application.yml` 中 `mybatis.mapper-locations: classpath:mapper/*.xml`，`type-aliases-package: com.grain.platform.entity`）。

**Schema 与初始化：**
- DDL/种子数据脚本：`backend/src/main/resources/db/schema.sql`（由 `scripts/reset-demo-db.ps1` 通过 `mysql` CLI 管道导入）。
- `spring.sql.init.mode: never` — 应用启动不自动执行 `schema-locations`（`application.yml`）；初始化依赖手动脚本或运维流程。

**File Storage:**
- 本地文件系统 — 验收脚本在系统临时目录生成上传用 Excel/CSV（`scripts/run-acceptance-smoke.ps1`）；业务侧上传处理由 `GrainTempController` 等后端接口完成，未检出对象存储 SDK。

**Caching:**
- 未检出 Redis 等外部缓存依赖（`backend/pom.xml` 无相关 starter）。

## Authentication & Identity

**Auth Provider:**
- 自定义应用内认证 — `backend/src/main/java/com/grain/platform/controller/AuthController.java`：`POST /api/auth/login`、`GET /api/auth/me`。
- 演示/开发头：`/api/auth/me` 支持可选请求头 `X-Demo-Username`，缺省时回落为 `"admin"`（见 `AuthController` 实现）。**非** OAuth2/OIDC 第三方 IdP 集成。

## Monitoring & Observability

**Error Tracking:**
- 未检出 Sentry 等外部错误上报；统一异常处理：`backend/src/main/java/com/grain/platform/common/GlobalExceptionHandler.java`。

**Logs:**
- Logback/Spring 默认日志；级别在 `application.yml` 的 `logging.level` 下配置（如 `root: info`）。

## CI/CD & Deployment

**Hosting:**
- 未检出 `.github/workflows` 等 CI 配置；本地联调由 `scripts/start-all.ps1` 分别启动后端与前端。

**CI Pipeline:**
- 未配置远程流水线；`scripts/run-acceptance-smoke.ps1` 在本地顺序执行后端 `mvn compile`、前端 `npm run build`、可选 DB 重置、启动后端并调用 REST 做冒烟验证。

## Environment Configuration

**Required env vars:**
- 前端：`VITE_API_BASE`（可选；未设置时使用 `http://localhost:8081`，见 `frontend/src/api/http.js` 与 `scripts/start-frontend.ps1`）。

**Secrets location:**
- 后端数据源等：**`backend/src/main/resources/application.yml` 中存在默认值**；生产密钥应置于受控环境（如部署环境变量、外部化 `application-prod.yml`），**勿**将真实密钥写入仓库或本文档。

## Webhooks & Callbacks

**Incoming:**
- 无专用 Webhook 端点检出。

**Outgoing:**
- 无对外 Webhook 注册检出。

---

*Integration audit: 2026-04-10*
