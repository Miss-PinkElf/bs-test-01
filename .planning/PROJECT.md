# 粮仓环境数据预测管理平台（毕设 / 数据库优先 MVP）

## What This Is

基于 Spring Boot 与 Vue 的粮仓温湿度与预测管理平台：支持粮温固定模板导入、汇总与预测归档、仪表盘与数据管理。当前面向毕业设计演示与答辩，以 MySQL 持久化与 REST API 为主轴。

## Core Value

**粮温与环境数据可导入、可查、可汇总，预测结果可追溯并在界面展示**；本期不以算法最优为目标，以工程完整与可演示为准。

## Requirements

### Validated

**过程与范围（devflow）**

- 数据库优先 MVP 主链（粮温导入与汇总、`prediction_task` / `prediction_result`、首页与数据页）— 详见 `.devflow/grain-platform-bootstrap/state.md`
- 管理端 `ConsoleLayout` 视口限高与侧栏/主区独立滚动（2026-04-10）
- 粮温原始测点记录服务端分页与多条件筛选（`pointNo` / `tempMin` / `tempMax`、`filter-options`）及 `DataView` 工具栏（2026-04-10）

**工程现状（摘自 `.planning/codebase/`，便于 GSD 规划）**

- 后端：Java 17、Spring Boot 3.2.x、MyBatis、默认 MySQL；REST + `ApiResponse` 统一响应；入口 `GrainPlatformApplication.java`
- 前端：Vue 3、Vite 5、Element Plus、Pinia、Vue Router；主路径 `frontend/`
- 粮温导入：`GrainTempImportService`（CSV / 行式 Excel / 固定 XLSX）；模板下载 `GrainTempController` → `GET /api/grain-temp/import/template`
- 演示与验收：`scripts/run-acceptance-smoke.ps1`、联调端口默认 `8081`（见 `application.yml`）

**体验（2026-04-10，模板 v2）**

- **GSD-UX-01～03**：粮温固定 XLSX 采用中文分区标题、合并单元格与填写说明；标签兼容「中文（英文键）」与旧版纯英文键；`DataView.vue` 说明与表结构一致；解析与生成均在 `GrainTempImportService` 中同源维护


### Active

- [ ] 按需：验收脚本对新版模板布局的轻量断言（见 `REQUIREMENTS.md` DATA-05）

### Out of Scope

- 「预测 → 修正 → 再预测」必做入口 — 本期收口，与 devflow 决策一致
- 全量 GSD 领域调研 — 本仓库以 devflow + codebase 文档为组合真相源

## Context

- **代码库鸟瞰（GSD）**：`.planning/codebase/`（STACK / ARCHITECTURE / STRUCTURE 等）
- **实施与过程真相源**：`.devflow/grain-platform-bootstrap/state.md`、最新 handoff、`NEXT-SESSION-PROMPT-DEVFLOW.md`
- **代码**：`backend/` + `frontend/`，联调默认端口 `8081`
- **模板优化诉求来源**：`zzz-prompt-debug/origin/优化excel.md`

## Constraints

- **毕设交付**：变更需可答辩解释，避免大范围无关重构
- **协作规则**：多文件产品行为变更前 Mini Align（`.cursor/rules/project-zh.mdc` §3）
- **Git**：`.planning/` 与 `AGENTS.md` 是否纳入版本库由你决定

## Key Decisions

| Decision | Rationale | Outcome |
| --- | --- | --- |
| 棕地仓库曾跳过初始 `/gsd-map-codebase` | 先 unblock quick路径 | 已补全 `.planning/codebase/` |
| 初始化跳过四向 research | 课题已进入收尾与体验优化 | 良好 |
| 过程记录仍以 devflow 为主 | 用户既定工作流 | 良好 |
| 固定模板标签双语 | 答辩可读性与程序识别兼顾；解析兼容旧模板 | 良好 |

## Evolution

本文件随 GSD 阶段与 devflow 里程碑更新；与 `.devflow/` 冲突时以 **devflow 中已确认事实** 为准，此处侧重 GSD 可执行需求与阶段边界。

**After each phase transition:**

1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

---
*Last updated: 2026-04-10 after 粮温固定模板 v2 + PROJECT 棕地 Validated 同步*
