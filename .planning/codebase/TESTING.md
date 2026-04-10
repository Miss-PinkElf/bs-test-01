# 测试模式

**分析日期：** 2026-04-10

## 测试框架（Maven / Java）

**依赖：**
- `backend/pom.xml` 声明 `spring-boot-starter-test`（`test` scope），随 Spring Boot `3.2.5` 父 POM 提供 JUnit 5、Mockito、Spring Test 等典型栈。

**源码布局：**
- `backend/src/test/java` **不存在**：当前仓库**没有**已提交的单元测试或集成测试类（未发现 `*Test.java`、`@Test` 引用）。

**运行命令（若将来补充测试）：**
```bash
cd backend
mvn test
```
- 验收脚本在编译阶段使用 `-DskipTests` 跳过测试（见下文 `scripts/run-acceptance-smoke.ps1`）。

## npm 脚本（前端）

**主 Vue 应用 `frontend/package.json`：**
| 脚本 | 作用 |
|------|------|
| `npm run dev` | `vite` 开发服务器 |
| `npm run build` | `vite build` 生产构建 |
| `npm run preview` | `vite preview` 预览构建产物 |

**未配置：** `test`、`lint`、`typecheck`；无 Vitest/Jest/Cypress 等依赖。

**并列项目 `frontend-next/package.json`：**
- 提供 `next lint` 等 Next.js 脚本；**验收冒烟未调用**该目录。

## 验收冒烟脚本

**位置：** `scripts/run-acceptance-smoke.ps1`

**默认行为概要：**
1. **静态检查（可用 `-SkipStaticChecks` 跳过）：**
   - 后端：`mvn -Dmaven.repo.local=... -q -DskipTests compile`，工作目录 `backend/`（见 `backend/pom.xml`）。
   - 前端：`npm run build`，工作目录 `frontend/`。
2. **数据（可用 `-SkipReset` 跳过）：** 调用 `scripts/reset-demo-db.ps1` 重置演示库。
3. **启动后端：** 通过 `scripts/start-backend.ps1` 在指定端口启动（默认 `ApiBase` 对应端口 `8081`），轮询 `GET /api/users` 直至 `code -eq 200`。
4. **HTTP 级断言：** 使用 PowerShell `Invoke-RestMethod` / 自定义 `Invoke-FileUpload`（multipart）调用 REST API，断言 JSON 包体中 `code == 200` 及业务字段。

**脚本参数（与可观测范围相关）：**
- `-ApiBase`：API 根地址，默认 `http://127.0.0.1:8081`。
- `-SkipReset` / `-SkipStaticChecks` / `-KeepBackendRunning`：控制是否重置库、是否编译/构建、结束后是否保留进程。

## 实际被覆盖的能力（验收脚本）

以下由 `run-acceptance-smoke.ps1` **直接或间接** 验证（**不**包含自动化浏览器/E2E、**不**包含 `frontend` 运行时交互测试）：

| 范围 | 验证方式 | 说明 |
|------|----------|------|
| 后端编译 | Maven `compile` | 保证主代码可通过编译 |
| 前端构建 | `npm run build` | 保证 `frontend` 生产构建成功 |
| 健康/用户 API | `GET /api/users` | 作为后端就绪探针 |
| 仪表盘 | `GET /api/dashboard/overview` | 校验 overview 字段集合、`latestAlerts` 中含 `REAL` 与 `PREDICTION` |
| 预测归档只读 | `GET /api/predictions/tasks`、`GET /api/predictions/tasks/{id}` | 列表非空、详情 `taskId` 一致 |
| 用户 CRUD | `POST/PUT/DELETE /api/users` 等 | 创建临时用户、更新、改密、列表校验、删除 |
| 粮温模板 | `GET /api/grain-temp/import/template` | 下载非空文件 |
| 粮温导入 | `POST` multipart `/api/grain-temp/import` | 固定模板、legacy CSV、手写最小 xlsx 包三种导入，`summaryGenerated` 与 summaries 查询 |
| 仪表盘副作用 | 再次 `GET /api/dashboard/overview` | `grainSummaryCount` 增长、`latestGrainSummaries` 与 `warehouseHealthList` 非空 |

**明确未覆盖：**
- `frontend` 页面路由、Element Plus 交互、Pinia 状态（无 Playwright/Cypress 等）。
- `frontend-next` 全站。
- Java 单元/集成测试目录尚为空时的任何 JUnit 用例。
- 认证登录流程（冒烟脚本未携带登录态头；若接口未来加鉴权需同步更新脚本）。

## 其他本地脚本（非 Maven/npm 测试）

- `scripts/start-backend.ps1`：释放端口、`mvn spring-boot:run` 启动（供人工或冒烟调用）。
- `scripts/reset-demo-db.ps1`：演示数据重置（冒烟可选步骤）。

## 覆盖率与 CI

**覆盖率：** 未检出 JaCoCo 等插件配置于 `backend/pom.xml`。

**CI：** 未在本次映射中强制读取 `.github/workflows`；若存在流水线，应以仓库内实际 YAML 为准补充。

## 实施新功能时的测试建议

1. **后端：** 在 `backend/src/test/java` 下按包镜像主代码，使用 `@SpringBootTest` 或切片测试 + `@MybatisTest`（按需要引入），为 `service` 层关键分支补充测试。
2. **前端：** 若引入 Vitest，建议与 Vite 同仓配置 `vitest.config.*`，对 `composables` 与 `utils` 做纯函数测试；页面可后续再引入 E2E。
3. **回归：** 扩展 API 后优先更新 `scripts/run-acceptance-smoke.ps1` 中的断言，保持与 `ApiResponse` 约定一致。

---

*测试分析：2026-04-10*
