# Handoff

## 基础信息

- 创建时间：2026-04-10
- mission：grain-platform-bootstrap
- 当前阶段：Pause-ready after table search + Cursor rules hardening
- handoff 编号：021
- 是否 superseded：是（由 022 接替，布局与粮温筛选后请读 022）

## 当前目标

- 保持「数据库优先 MVP」与答辩前收口节奏；本轮完成列表模糊搜索与协作规则加固后继续可演示状态。

## 当前进度

- 已完成（参见 `state.md` 详单）：用户页角色区 + 答辩大屏表格化展示统一（前序 handoff 已记，021 起为补充轮次）。
- 已完成：**主流 CRUD 列表模糊搜索**
  - 前端：`frontend/src/utils/fuzzyText.js`，多页 `el-input` 搜索条
  - 服务端分页：`GET /api/grain-temp/records`、`GET /api/sensor-data` 可选 `keyword`（MyBatis LIKE）
- 已完成：**`.cursor/rules/project-zh.mdc` 强化**——「实现前对齐」硬约束（触发条件、必须先输出理解/方案/待确认、豁免语、反例）。

## 本轮完成内容

- [x] 列表搜索（前端过滤 + 两条分页接口 keyword）
- [x] `state.md` 已追加搜索结果条目
- [x] Cursor 项目规则加强（对齐门禁）
- [ ] 本轮未单独补「列表搜索」独立 plan 文件（可选后续补 `.devflow/.../plans/2026-04-10-list-keyword-search.md`）

## 关键决策与原因

| 决策 | 原因 |
| --- | --- |
| 用户/仓库等已全量拉取的列表走前端 `filterRows` | 接口未分页、数据量可控；减少后端改动面 |
| 粮温原始记录与环境记录走后端 `keyword` | 已分页列表需数据库侧过滤，避免前端全量拉取 |
| `.mdc` 写明豁免语（「直接做」） | 避免长期任务里用户明确要求快做时仍机械卡死 |

## 关键文件

| 文件 | 作用 |
| --- | --- |
| `frontend/src/utils/fuzzyText.js` | 通用过滤 |
| `frontend/src/views/UsersView.vue`、`WarehouseView.vue`、`DataView.vue`、`PredictionView.vue`、`DashboardView.vue` | 搜索 UI 与逻辑 |
| `frontend/src/api/grain.js` | `keyword` 透传 |
| `backend/.../GrainTempRecordMapper.xml`、`GrainTempController.java`、`GrainTempService.java` | 粮温记录 keyword |
| `backend/.../SensorDataMapper.xml`、`SensorDataController.java`、`SensorDataService.java` | 环境数据 keyword |
| `.cursor/rules/project-zh.mdc` | 协作硬约束 |

## 风险 / 阻塞 / 开放问题

- [ ] **流程**：曾出现未先对齐方案即批量改代码的情况；已用 `project-zh.mdc` 加固，恢复后助手应严格遵守 3.2 / 3.3。
- [ ] **未讨论完**：用户/仓库若未来数据量很大，是否要为列表接口增加后端 `keyword`。
- [ ] **未做**：`/screen` 大屏表格未加本地筛选（可选）。
- [ ] **验收脚本**：`run-acceptance-smoke.ps1` 尚未显式断言带 `keyword` 的分页接口（可用后续补一条轻量请求）。
- [ ] **沙箱**：脚本内前端构建仍可能 `esbuild spawn EPERM`，可继续 `-SkipStaticChecks`。
- [ ] **文档**：`zzz-docs/设计文档/` 非归档部分仍可继续扫旧口径。

## 立即下一步

1. 恢复对话后先读 `NEXT-SESSION-PROMPT-DEVFLOW.md` + 本 handoff + `state.md`。
2. 新需求默认**先 Mini Align**再编码；用户说「直接做」再豁免。
3. 可选：补列表搜索 plan、验收脚本 keyword 断言、大屏表筛选、设计文档口径扫荡。

## 恢复指引

1. 根目录 `NEXT-SESSION-PROMPT-DEVFLOW.md`
2. `.devflow/grain-platform-bootstrap/state.md`
3. 本文件：`handoffs/2026-04-10-021-pause-ready-after-table-search-and-cursor-rules.md`
4. 按需：`plans/active-plan-links.md`、`zzz-docs/验证/数据库优先MVP-回归验证清单.md`
