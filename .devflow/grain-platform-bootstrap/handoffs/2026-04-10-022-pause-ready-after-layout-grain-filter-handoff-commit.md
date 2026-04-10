# Handoff

## 基础信息

- 创建时间：2026-04-10
- mission：grain-platform-bootstrap
- 当前阶段：Pause-ready after 管理端布局 + 粮温多条件筛选 + devflow 补记 + Git 提交
- handoff 编号：022
- 是否 superseded：否

## 当前目标

- 保持「数据库优先 MVP」可演示；本轮完成管理端滚动体验与粮温原始记录表头向筛选，并落盘 devflow、提交代码，便于休息后无缝恢复。

## 当前进度

- 021 及之前主线仍有效；022 在 021 基础上追加本轮交付。
- 已完成：**管理端布局**
  - `ConsoleLayout.vue`：`el-scrollbar` 包裹侧栏菜单区与主内容；`console-body` 类名。
  - `styles.css`：`html/body/#app` 高度链、`.console-shell` 视口限高、`.console-main` 与侧栏滚动分工、小屏断点恢复文档流。
- 已完成：**粮温原始测点记录多条件筛选**
  - 后端：`GET /api/grain-temp/records` 增加 `pointNo`、`tempMin`、`tempMax`；`GET /api/grain-temp/records/filter-options`；`GrainTempRecordFilterOptionsDto`；Mapper `WHERE` 与 distinct 查询。
  - 前端：`DataView.vue` 粮温工具栏（区域/层号/点位、温度区间、采集时间范围、应用筛选/重置、关键词防抖）；`grain.js`。
- 已完成：**devflow 记录**：`state.md`、`decision-log` [#22][#23]、`plans/2026-04-10-console-layout-scroll-and-scrollbar.md`、`plans/2026-04-10-grain-temp-records-filter-toolbar.md`、`active-plan-links.md`。
- 已完成：**Git 提交** `16cabbc`（`shuowang/dev2.0`）：上述前后端与 `.devflow` 变更；**未**纳入 `.codex/`、`.cursor/` 下未跟踪的 GSD 工具文件。

## 本轮完成内容

- [x] 管理端侧栏与主区独立滚动 + Element Plus `el-scrollbar`
- [x] 粮温记录服务端区间/点位筛选 + `filter-options` + 前端工具栏
- [x] devflow 双 plan + state / 决策日志更新
- [x] `NEXT-SESSION-PROMPT-DEVFLOW.md` 已与 handoff 022 同步更新（根目录可复制提示词）
- [x] 代码提交 `16cabbc`

## 关键决策与原因

| 决策 | 原因 |
| --- | --- |
| 温度等结构化条件走后端 SQL | 分页列表必须在数据库侧过滤，结果一致、可答辩解释 |
| 下拉选项用 `filter-options` 聚合 | 避免依赖当前页数据或手写枚举，与现有 `grain_temp_record`/`point` 一致 |
| 布局用视口限高 +内部滚动 | 与 Element Plus Container 文档一致，避免整页文档滚动带跑侧栏 |
| 提交排除 `.codex`/`.cursor`大段未跟踪 GSD 文件 | 与毕设课题代码解耦，防误提交工具链 |

## 关键文件

| 文件 | 作用 |
| --- | --- |
| `frontend/src/layout/ConsoleLayout.vue` | 布局与 scrollbar |
| `frontend/src/styles.css` | 控制台布局样式 |
| `frontend/src/views/DataView.vue` | 粮温筛选工具栏 |
| `frontend/src/api/grain.js` | `fetchGrainTempRecordFilterOptions`、扩展 `fetchGrainTempRecords` |
| `backend/.../GrainTempController.java` | `filter-options`、分页参数 |
| `backend/.../GrainTempRecordMapper.xml` | 条件与 distinct |
| `.devflow/grain-platform-bootstrap/state.md` | 真相源 |
| `NEXT-SESSION-PROMPT-DEVFLOW.md` | 下轮复制用恢复提示词 |

## 风险 / 阻塞 / 开放问题

- [ ] **验收脚本**：尚未断言带 `keyword` 的分页；亦未断言带 `pointNo`/`tempMin`/`tempMax` 的粮温记录分页（可选轻量 smoke）。
- [ ] **回归清单**：`zzz-docs/验证/数据库优先MVP-回归验证清单.md` 可补充「粮温多条件筛选」手工步骤。
- [ ] **GSD**：用户计划用 GSD 做后续优化；仓库内 **`.planning/` 尚未初始化**，大量 `.codex/`、`.cursor/` GSD 相关路径仍为未跟踪，**勿整包提交**；初始化见下轮 `NEXT-SESSION` 与对话结论（`/gsd-new-project`、可选 `/gsd-map-codebase`）。
- [ ] **历史可选项未变**：设计文档口径扫荡、用户/仓库后端 `keyword`、`/screen` 本地筛选、沙箱 `esbuild EPERM` 用 `-SkipStaticChecks` 等（见 `state.md` 待解决问题）。
- [ ] **push**：`16cabbc` 若需同步远端，由你本地 `git push`。

## 立即下一步

1. 下轮开始：读 `NEXT-SESSION-PROMPT-DEVFLOW.md` + 本 handoff + `state.md`。
2. 新需求：Mini Align → plan → 实施（`project-zh.mdc` §3）；小修可直接做。
3. 可选：跑 `./scripts/run-acceptance-smoke.ps1`（或 `-SkipStaticChecks`）确认提交后全链路；初始化 GSD 与 devflow 并行维护。

## 恢复指引

1. 根目录 `NEXT-SESSION-PROMPT-DEVFLOW.md`
2. `.devflow/grain-platform-bootstrap/state.md`
3. 本文件：`handoffs/2026-04-10-022-pause-ready-after-layout-grain-filter-handoff-commit.md`
4. 按需：`handoffs/index.md`、`plans/active-plan-links.md`
