# Technology Stack

**Analysis Date:** 2026-04-10

## Languages

**Primary:**
- Java 17 — Spring Boot 后端（`backend/pom.xml` 中 `<java.version>17</java.version>`）
- JavaScript (ES modules) — 主前端 `frontend/`（Vite + Vue 3，无 TypeScript 配置为主路径）

**Secondary:**
- TypeScript — 实验性/备用前端 `frontend-next/`（Next.js 15 + React 19）
- PowerShell — 仓库根目录 `scripts/*.ps1` 开发与验收自动化

## Runtime

**Environment:**
- JVM 17（Spring Boot 应用）
- Node.js（前端 `npm` 脚本；`scripts/check-env.ps1` 检查 `node` / `npm`）

**Package Manager:**
- **后端：** Maven（`backend/pom.xml`）；本地仓库优先 `%USERPROFILE%\.m2\repository`，否则 `backend/.m2/repository`（见 `scripts/start-backend.ps1`）
- **主前端：** npm；锁文件：`frontend/package-lock.json`
- **Lockfile：** 主前端已提交 lockfile；后端为 Maven 依赖树，无 npm 式 lock

## Frameworks

**Core:**
- Spring Boot 3.2.5 — Web API、校验、测试起步依赖（`backend/pom.xml` 父 POM `spring-boot-starter-parent`）
- Vue 3.4.x — SPA（`frontend/package.json`）
- Vite 5.2.x — 开发与构建（`frontend/vite.config.js`）
- Pinia 2.x — 状态管理（`frontend/package.json`）
- Vue Router 4.x — 路由（`frontend/package.json`）
- Element Plus 2.8.x + `@element-plus/icons-vue` — UI 组件（`frontend/package.json`）
- ECharts 5.5.x — 图表（`frontend/package.json`）
- MyBatis Spring Boot Starter 3.0.3 — SQL 映射（`backend/pom.xml`）

**Testing:**
- spring-boot-starter-test（JUnit 等）— 已声明于 `backend/pom.xml`，`backend/src/test` 下当前无测试资源检出

**Build/Dev:**
- Maven：`mvn spring-boot:run` / `compile`（`scripts/start-backend.ps1`、`scripts/run-acceptance-smoke.ps1`）
- Vite：`npm run dev` / `npm run build` / `npm run preview`（`frontend/package.json`）

## Key Dependencies

**Critical:**
- `mysql-connector-j`（runtime）— MySQL JDBC（`backend/pom.xml`）
- `mybatis-spring-boot-starter` — 实体别名与 XML 映射（`backend/src/main/resources/application.yml`）
- `axios` — HTTP 客户端，统一封装于 `frontend/src/api/http.js`
- `poi-ooxml` 5.2.5 — 服务端 Excel（.xlsx）读写（`backend/pom.xml`）

**Infrastructure:**
- `spring-boot-starter-web` — REST
- `spring-boot-starter-validation` — Bean Validation（`jakarta.validation`）

## Configuration

**Environment:**
- 后端：`backend/src/main/resources/application.yml`（数据源、MyBatis、日志级别）；**敏感项仅说明存在**：库连接与账号口令在该文件中配置，生产环境应通过外部配置覆盖，**禁止**在文档或提交中复述具体口令。
- 前端 API 基址：`import.meta.env.VITE_API_BASE`，默认回落 `http://localhost:8081`（`frontend/src/api/http.js`）；`scripts/start-frontend.ps1` 启动前设置 `$env:VITE_API_BASE = "http://localhost:8081"`。

**Build:**
- Maven 项目级设置：`backend/.mvn/settings.xml`，`backend/.mvn/maven.config` 指定 `--settings=.mvn/settings.xml`
- Vite：`frontend/vite.config.js`（端口 5173、`host: "0.0.0.0"`）

## Platform Requirements

**Development:**
- JDK 17、Maven CLI 或 IDE 内嵌 Maven（仓库未检出 `mvnw.cmd`，`scripts/start-backend.ps1` 在缺少 wrapper 时依赖系统 `mvn`）
- Node.js + npm（`scripts/check-env.ps1`）
- MySQL 服务与可选 `mysql` 客户端 CLI（`scripts/reset-demo-db.ps1`）

**Production:**
- 可部署为 Spring Boot 可执行 JAR（`spring-boot-maven-plugin`）；前端为 Vite 静态构建产物（`npm run build` 输出目录由 Vite 默认约定，构建脚本见 `frontend/package.json`）

---

*Stack analysis: 2026-04-10*
