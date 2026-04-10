# Codebase Concerns

**Analysis Date:** 2026-04-10

## Tech Debt

**无全局 API 鉴权（毕业设计 MVP 边界）：**
- Issue: `pom.xml` 未引入 `spring-boot-starter-security`；代码库中无 `SecurityFilterChain`、`@PreAuthorize` 或请求级令牌校验。业务接口在网络可达时等价于开放写读。
- Files: `backend/pom.xml`、`frontend/src/api/http.js`、`frontend/src/api/grain.js`
- Impact: 任意客户端可直接调用 CRUD、导入与预测接口；前端路由守卫仅依赖 `localStorage`，与后端授权无关。
- Fix approach: 引入 Spring Security + JWT 或 Session；统一从 `Authorization` 解析主体；前端 `http` 拦截器附带凭证；为导入等大流量接口单独限流与审计。

**演示型登录与令牌：**
- Issue: `AuthService.login` 使用明文口令与库中字段直接相等比较；返回的 token 为可预测的 `demo-token-` 前缀字符串。`AuthController.me` 可通过可选请求头 `X-Demo-Username` 指定当前用户，缺省回落为 `admin`。
- Files: `backend/src/main/java/com/grain/platform/service/AuthService.java`、`backend/src/main/java/com/grain/platform/controller/AuthController.java`
- Impact: 无真实身份绑定；`/api/auth/me` 易被滥用为按用户名查询画像（配合开放 API 时风险更高）。
- Fix approach: 密码哈希（BCrypt 等）；签发不可伪造的 JWT 或服务端 Session；删除或严格校验演示头；所有需登录接口在过滤器中校验。

**配置中的敏感连接信息：**
- Issue: 数据源连接串与账号口令写在 `application.yml` 仓库内，未拆到环境变量或外部密钥管理。
- Files: `backend/src/main/resources/application.yml`
- Impact: 仓库泄露即暴露数据库访问面；与生产部署规范不符。
- Fix approach: 使用 `SPRING_DATASOURCE_*` 或 profile 专用配置；CI/本地用 `.env`（不入库）或密钥管理器。

**预测修正链路仅保留字段：**
- Issue: 与 `.devflow/grain-platform-bootstrap/state.md` 一致：本期主线为「数据库优先 MVP」，不做「预测 → 修正 → 再预测」闭环；`PredictionService` 中任务备注与实体字段保留扩展位，但无修正入口与页面。
- Files: `backend/src/main/java/com/grain/platform/service/PredictionService.java`、`state.md`（已确认事实 / 待解决问题）
- Impact: 若产品文档仍描述闭环，会产生期望落差；数据库字段与 UI 不一致。
- Fix approach: 在 PRD 与界面文案中固定「归档 + 双线图 + 预警」口径；若后续要做修正，需新增接口、状态机与回归用例。

**演示库初始化与运维脚本：**
- Issue: `spring.sql.init.mode` 为 `never`，启动不自动建表灌数；依赖人工执行 `scripts/reset-demo-db.ps1` 将 `backend/src/main/resources/db/schema.sql` 管道进 `mysql`。
- Files: `backend/src/main/resources/application.yml`、`scripts/reset-demo-db.ps1`
- Impact: 新环境若未跑脚本会出现表不存在或空库；脚本依赖本机 `mysql` CLI 与可连接实例。
- Fix approach: 文档化「首次/重置」步骤；可选 Flyway/Liquibase；脚本参数化主机与库名（口令勿硬编码入仓库，改用交互或环境变量）。

## Known Bugs

**（无单独跟踪号）验收脚本在部分沙箱环境的前端构建：**
- Symptoms: `state.md` 记载：一键验收脚本内直接跑前端构建时可能遇到 `esbuild spawn EPERM`，而仓库内单独 `npm run build` 可通过。
- Files: `scripts/run-acceptance-smoke.ps1`、`.devflow/grain-platform-bootstrap/state.md`
- Trigger: 在受限沙箱中执行带前端构建的验收脚本。
- Workaround: 使用 `-SkipStaticChecks`，静态构建在宿主机单独执行。

## Security Considerations

**CORS 宽放行 + 携带凭证：**
- Risk: `CorsConfig` 使用 `setAllowCredentials(true)` 且 `setAllowedOriginPatterns(List.of("*"))`，生产环境易被恶意站点在浏览器侧滥用（与具体浏览器实现策略组合时需警惕）。
- Files: `backend/src/main/java/com/grain/platform/config/CorsConfig.java`
- Current mitigation: 便于本地前后端分离联调。
- Recommendations: 生产改为显式 origin 列表；若必须 `*`，通常应关闭 credentials 或改用同源反向代理。

**会话存储于 localStorage：**
- Risk: `frontend/src/utils/session.js` 将登录结果 JSON 存入 `localStorage`，无 HttpOnly 保护，存在 XSS 时令牌与用户字段一并泄露风险。
- Files: `frontend/src/utils/session.js`、`frontend/src/views/LoginView.vue`
- Current mitigation: MVP 演示场景。
- Recommendations: 迁移到 HttpOnly Cookie + CSRF 防护，或短期至少对 token 使用内存态并缩短暴露面。

**日志中的账户信息：**
- Risk: `AuthController` 在登录成功路径记录 `userId`、`username`、`roles`，日志聚合系统若权限宽松可能扩大泄露面。
- Files: `backend/src/main/java/com/grain/platform/controller/AuthController.java`
- Recommendations: 生产降级为 debug 或脱敏。

**数据库重置脚本参数：**
- Risk: `reset-demo-db.ps1` 提供可覆盖的连接参数默认值；若被误用于生产或把真实口令写入仓库副本，会造成事故。
- Files: `scripts/reset-demo-db.ps1`
- Current mitigation: 脚本明确面向演示库重置。
- Recommendations: 文首警告「仅演示」；默认口令改为强制参数或读取环境变量（不在此文档写出任何具体值）。

## Performance Bottlenecks

**预测任务列表组装：**
- Problem: `PredictionService.listTasks` 对每条任务调用 `requireWarehouse`、`metricService.getMetric`、`loadActualSeries`、`predictionResultMapper.selectByTaskId`，呈 N 次数据库与多次序列加载特征。
- Files: `backend/src/main/java/com/grain/platform/service/PredictionService.java`
- Cause: Stream 映射内逐条拉取关联数据。
- Improvement path: 批量查询仓库与指标缓存；任务 ID 列表一次拉取结果集再分组；分页接口替代 `selectAll`。

**环境数据参与预测时的全量过滤：**
- Problem: `loadActualSeries` 在非粮温指标时调用 `sensorDataService.list(warehouseId, metricCode)` 后在内存中按训练窗过滤。
- Files: `backend/src/main/java/com/grain/platform/service/PredictionService.java`、`backend/src/main/java/com/grain/platform/service/SensorDataService.java`（需与 `list` 实现对照）
- Cause: 历史接口可能返回该仓库该指标的全量点。
- Improvement path: 在 Mapper 层增加时间范围条件与 limit；预测只读所需区间。

**Excel / CSV 导入内存占用：**
- Problem: `GrainTempImportService.parseExcel` 与 `SensorDataImportService` 使用 Apache POI `WorkbookFactory.create(InputStream)` 整簿载入；大文件或畸形 xlsx 易导致堆内存压力与 GC。
- Files: `backend/src/main/java/com/grain/platform/service/GrainTempImportService.java`、`backend/src/main/java/com/grain/platform/service/SensorDataImportService.java`
- Cause: 流式 API 未使用；未在应用层声明更小 `spring.servlet.multipart.max-file-size`（当前未见自定义 multipart 配置，依赖 Spring Boot 默认上限）。
- Improvement path: 限制单文件大小与行数；大表用 SAX 流式读取；异步队列处理。

**粮温汇总层均值字段与导入行：**
- Problem: `buildSummary` 仅对层号 1–4 写入 `layer1Avg`–`layer4Avg`；固定模板常量 `FIXED_TEMPLATE_LAYER_COUNT` 为 4，若用户扩展更多层，导入行仍写入记录，但汇总表层均值列可能不完整。
- Files: `backend/src/main/java/com/grain/platform/service/GrainTempImportService.java`
- Cause: 模型与解析假设演示维度。
- Improvement path: 动态层聚合或扩展表字段；模板校验层数。

## Fragile Areas

**粮温导入多形态解析：**
- Files: `backend/src/main/java/com/grain/platform/service/GrainTempImportService.java`
- Why fragile: 固定模板依赖前若干行扫描 `warehouseId` / `zoneCode` 启发式（`isFixedTemplateSheet`）；与旧版 CSV、行式 Excel 共用入口，用户微调模板行列或合并单元格易导致解析失败或静默跳过空单元。
- Safe modification: 任一改版需同时跑 `scripts/run-acceptance-smoke.ps1`（或等价步骤）中固定模板、旧 CSV、旧行式 xls 用例；更新 `zzz-docs/验证/数据库优先MVP-回归验证清单.md`。

**环境数据 Excel 导入：**
- Files: `backend/src/main/java/com/grain/platform/service/SensorDataImportService.java`
- Why fragile: 与粮温类似的格式假设与 POI 全量解析。
- Safe modification: 与粮温相同，以导入样本文件为契约测试。

**前端列表「全量 + 客户端过滤」：**
- Files: `frontend/src/views/UsersView.vue`（及 `state.md` 所述用户/仓库列表模式）
- Why fragile: 用户与仓库接口全量返回时，数据量上升后首屏与内存变差；搜索在浏览器侧完成，与后端分页语义不一致。
- Safe modification: 增加后端 `keyword` 与分页后再改前端为服务端驱动；每改一处对照 `state.md` 中「待解决问题」条目。

**答辩大屏表格：**
- Why fragile: `state.md` 记载 `/screen` 大屏表格未做本地筛选（可选），与后台页行为不一致可能引起演示口径问题。
- Files: `frontend/src/views/BigScreenView.vue`（及相关样式 `frontend/src/styles.css`）

## Scaling Limits

**单实例 JVM + 本地 MySQL：**
- Current capacity: 典型毕业设计数据量（仓库、测点、导入批次有限）。
- Limit: 并发导入与大结果集列表无连接池调优与读写分离；预测列表无分页。
- Scaling path: 连接池参数、只读副本、接口分页与导出异步化。

**前端 axios 超时：**
- Files: `frontend/src/api/http.js`
- Limit: `timeout: 10000`（10s）；大文件导入或慢查询易超时。
- Scaling path: 按接口覆盖更长超时或上传走独立 client。

## Dependencies at Risk

**Apache POI 5.2.5：**
- Risk: 解析不可信上传的 Office 文档属于常见攻击面（XML 炸弹、超大行）。
- Impact: 服务拒绝或内存耗尽。
- Migration plan: 限制大小与解析深度；关注 POI 安全公告并及时小版本升级。

## Missing Critical Features

**生产级安全与审计：**
- Problem: 无统一认证授权、无操作审计、无速率限制。
- Blocks: 无法直接对公网或不可信网络暴露。

**自动化测试：**
- Problem: `backend` 下未见 `src/test/java` 业务测试；`frontend/package.json` 无 `test` 脚本。
- Blocks: 重构导入与预测逻辑时回归成本高，依赖手工 smoke 与验收脚本。

## Test Coverage Gaps

**核心业务路径无单元/集成测试：**
- What's not tested: 粮温固定模板解析边界、汇总重算、`PredictionService` 线性预测与归档、`Dashboard` 聚合 SQL。
- Files: `backend/src/main/java/com/grain/platform/service/GrainTempImportService.java`、`backend/src/main/java/com/grain/platform/service/PredictionService.java`、各 `*Mapper.xml`
- Risk: 模板微调或 MyBatis 条件变更易引入静默错误。
- Priority: High（相对毕业设计维护周期）

**验收脚本断言不完整：**
- What's not tested: `state.md` 记载带 `keyword` 的分页接口、粮温记录 `filter-options` 与 `pointNo`/`tempMin`/`tempMax` 等新增参数尚未纳入脚本断言。
- Files: `scripts/run-acceptance-smoke.ps1`、`.devflow/grain-platform-bootstrap/state.md`
- Risk: 回归漏测导致演示前才发现接口契约破坏。
- Priority: Medium

---

*Concerns audit: 2026-04-10*
