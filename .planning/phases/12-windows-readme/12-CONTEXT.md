# Phase 12: Windows 免依赖运行封装与根目录 README 双运行说明（手动运行 / 免安装运行） - Context

**Gathered:** 2026-04-11
**Status:** Ready for planning

<domain>
## Phase Boundary

本阶段处理两件事：

1. 为当前项目补齐一套面向 **Windows** 的“免安装依赖运行”方案，让别人不需要预装 `node`、`npm`、`java`、`mvn`、`mysql` 也能启动演示环境。
2. 在仓库根目录 README 中明确写出两种运行方式：**手动开发运行** 与 **免安装运行**，让使用者知道分别需要什么环境、怎么启动、怎么重置 demo 数据。

本阶段不扩展业务功能，不改已有产品范围；重点是运行封装、启动脚本、分发形态与文档说明。

</domain>

<decisions>
## Implementation Decisions

### 发布形态

- **D-01:** 免安装运行采用 **portable 发布目录** 方案，不把运行时和发布产物直接提交进仓库；仓库只保留源码、脚本、README 与打包逻辑。
- **D-02:** portable 产物必须通过 `.gitignore` 忽略，最终由用户自己把生成后的发布目录压缩后发给别人，而不是依赖 git 分发二进制大文件。
- **D-03:** portable 方案的目标体验是 **尽量接近“解压就能跑”**，优先走普通用户权限，不要求用户额外安装前后端依赖。

### 运行时封装

- **D-04:** 免安装模式下不再依赖 `node`、`npm`、`mvn` 参与运行；这些只属于开发态或打包态依赖。portable 运行时应改为消费预构建产物。
- **D-05:** 后端在免安装模式下应以 **可直接运行的 Spring Boot JAR** 形态交付；前端在免安装模式下应以 **预构建静态资源** 形态交付，而不是继续要求本机启动 Vite dev server。
- **D-06:** 免安装模式需要把 **JRE** 一并带上；planner / executor 不应把“让用户自己安装 Java”保留为 portable 运行前提。

### 数据库与数据策略

- **D-07:** 免安装模式继续坚持 **真实 MySQL**，不为省事切换成嵌入式数据库；要做的是把 MySQL 改成可携带的 Windows portable 形态。
- **D-08:** portable 数据库仍沿用 Phase 11 已经收口的 `schema.sql` / demo baseline / reset 口径，不另起一套演示数据源。
- **D-09:** portable 运行默认 **保留现有数据**，不在每次启动时自动重置；需要单独提供 reset 脚本用于恢复到标准演示库。

### 启动入口与 README

- **D-10:** 免安装模式对最终使用者尽量只暴露 **一个主启动脚本**，由它负责拉起数据库、后端和前端访问入口；不把多步手工编排留给普通演示使用者。
- **D-11:** 根目录 README 必须明确区分两种运行方式：
  - **手动运行**：面向开发者，说明需要安装 `node`、`npm`、`java`、`mvn`、`mysql` 等环境，并沿用现有开发脚本。
  - **免安装运行**：面向演示/答辩使用者，说明如何启动 portable 包、如何停止、如何重置 demo 数据。
- **D-12:** README 必须明确说明“仓库本身不附带 portable 大文件，portable 包由本地打包生成后自行压缩分发”。

### the agent's Discretion

- portable 发布目录的具体命名可以由 planner / executor 决定，例如 `release/windows-portable/` 或等价目录，只要满足 D-01 与 D-02。
- 前端静态资源最终是由后端统一托管，还是由额外轻量静态服务一起打包，只要满足 D-04、D-05 和 D-10，都可由 planner / executor 决定。
- portable 脚本的日志输出、PID 管理、启动等待、端口探测与停止策略，可由 planner / executor 结合现有 `scripts/` 风格决定。

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 原始诉求与阶段边界

- `zzz-prompt-debug/origin/mock最近数据+添加一键运行脚本/prompt.md` — 原始需求来源；Phase 12 的“类似 docker、Windows 免安装依赖运行”直接来自这里
- `.planning/ROADMAP.md` — Phase 12 条目、依赖关系与当前 milestone 上下文
- `.planning/PROJECT.md` — 项目总边界；当前仍以毕设演示与数据库优先 MVP 为主
- `.planning/STATE.md` — 当前 phase 演进状态；Phase 11 已明确把 Windows 一键运行拆出到后续 phase

### 前置决策

- `.planning/phases/11-1-4-mock-windows/11-CONTEXT.md` — 已锁定“Windows 一键运行不属于 Phase 11，而是单独 phase”，且 demo 数据基线由 `schema.sql + reset-demo-db.ps1` 主导
- `zzz-docs/归档/2026-04-11-Phase11河南Jan-Apr基线与脏数据清理归档.md` — Phase 11 已完成的 baseline/reset 事实，portable 方案必须建立在这条链路之上

### 当前运行与打包现状

- `README.md` — 当前根 README；需要在本 phase 中升级为双运行方式说明
- `package.json` — 根脚本入口；当前 `dev` / `frontend` / `backend` / `reset-demo-db` 组织方式是 Phase 12 的现有基础
- `.gitignore` — 需要在本 phase 中承接 portable 发布目录与大文件忽略策略
- `.planning/codebase/STACK.md` — 当前明确依赖 `Java 17 / Node.js / Maven / MySQL` 的开发态现实
- `.planning/codebase/STRUCTURE.md` — 当前项目结构与 `scripts/`、`backend/`、`frontend/` 的主要落点
- `.planning/codebase/CONCERNS.md` — 已记录 demo 初始化与 `mysql` CLI 依赖等运行风险
- `.planning/codebase/INTEGRATIONS.md` — 已记录 `schema.sql`、本地脚本和现有联调入口之间的关系

### 现有脚本与配置入口

- `scripts/check-env.ps1` — 当前开发态环境检查逻辑；说明现状仍依赖本机工具链
- `scripts/start-backend.ps1` — 当前后端启动方式；仍依赖 `mvn` / `mvnw`
- `scripts/start-frontend.ps1` — 当前前端启动方式；仍依赖 `npm install` 与 Vite dev server
- `scripts/start-all.ps1` — 当前 Windows 联调总入口；portable 方案需要决定是复用还是旁路它
- `scripts/dev-inline.cjs` — 当前根级 `npm run dev` 的并行启动包装器
- `scripts/reset-demo-db.ps1` — 当前 demo 数据重置入口；portable 方案需要继续复用其语义
- `scripts/verify-demo-baseline.ps1` — 当前 baseline 校验入口；portable 方案需要与其保持一致
- `backend/src/main/resources/application.yml` — 当前后端 datasource 与端口配置；portable MySQL 落地会直接触及这里或其运行时覆盖方式
- `backend/pom.xml` — 当前后端构建与 Spring Boot jar 打包基础
- `frontend/package.json` — 当前前端构建脚本基础；portable 前端必须建立在 `vite build` 产物之上

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `scripts/start-all.ps1` 已经是 Windows 侧的统一入口雏形，portable 方案可以沿用其“一个入口拉起多个进程”的使用心智。
- `scripts/reset-demo-db.ps1` 与 `scripts/verify-demo-baseline.ps1` 已经把 demo 数据重置与校验语义收口完成，portable 方案不需要再发明第二套 demo 数据口径。
- 根 `package.json` 已经把开发态入口集中到少数脚本中，README 的“双模式运行”可以延续这种入口集中思路。
- `backend/pom.xml` 已具备 Spring Boot 可执行 jar 的基础，不需要改换后端栈。
- `frontend/package.json` 已具备 `vite build`，说明前端静态产物路径是现成可用的。

### Established Patterns

- 当前仓库的开发态脚本默认端口已经固定为前端 `5174`、后端 `8081`，portable 方案应优先复用现有端口习惯，避免 README 和验收心智分裂。
- 当前 demo 数据不是应用启动时自动建库，而是通过显式 reset 路径恢复；portable 方案也应延续“默认保留数据，单独 reset”的模式。
- 当前 README、脚本与 codebase 文档都把本机 `node` / `npm` / `mvn` / `mysql` 视为前提，这正是 Phase 12 要收口和替换的现状。

### Integration Points

- portable 方案会落在根目录 README、`.gitignore`、`scripts/`、`backend` 打包输出、`frontend` 构建输出及其发布目录编排上。
- 若 portable MySQL 被引入，运行时配置会和 `backend/src/main/resources/application.yml` 及 reset 脚本参数衔接。
- 若前端不再依赖 Vite dev server，则需要明确它在 portable 模式下是如何与后端和浏览器访问入口对接的。

</code_context>

<specifics>
## Specific Ideas

- 用户希望“像 Docker 一样”，但目标平台限定为 **任意 Windows 环境**，因此这里的本质不是容器编排，而是做出一套 **Windows 便携分发包**。
- 用户接受的分发方式是：仓库里不放这些运行时大文件，由本地生成 portable 目录后自行压缩发送给别人。
- 用户接受的决策默认值为：
  - portable 目录方案
  - 尽量解压即跑、普通用户权限
  - 真实 MySQL 的便携版，而不是嵌入式替代
  - 一个主启动脚本
  - 默认保留数据，单独 reset

</specifics>

<deferred>
## Deferred Ideas

- macOS / Linux 的免安装运行方案：当前 phase 仅面向 Windows。
- 更像正式安装器的 MSI / EXE 安装包：当前 phase 优先 portable 分发，不把安装器作为必做项。
- 基于 Docker Desktop / WSL2 的容器化分发：虽然用户用“类似 docker”描述目标体验，但本 phase 的实际收口方向是原生 Windows portable 包，而非引入 Docker 作为运行前提。

</deferred>

---

*Phase: 12-windows-readme*
*Context gathered: 2026-04-11*
