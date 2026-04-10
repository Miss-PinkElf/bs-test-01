```text
你现在在仓库根目录下的 `bs-test-01`。请继续毕业设计项目，并严格先走 devflow 恢复流程后再开始任何分析、验证或开发。

【协作硬约束（必读）】
- 实现类需求（多文件、前后端、新交互）默认须先输出：理解 + 方案 + 待你确认，禁止同一条回复里直接大范围改代码。
- 只有你明确说「直接做」「不用讨论」等豁免语时，才可跳过对齐。详见 `.cursor/rules/project-zh.mdc` §3。

【必须先读取】
1. `.codex/skills/devflow/SKILL.md`
2. `zzz-docs/任务书.md`
3. `zzz-docs/开题报告.md`
4. `.devflow/grain-platform-bootstrap/state.md`
5. `.devflow/grain-platform-bootstrap/handoffs/2026-04-10-022-pause-ready-after-layout-grain-filter-handoff-commit.md`
6. 按需读取：
   - `.devflow/grain-platform-bootstrap/handoffs/index.md`
   - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-04-10-console-layout-scroll-and-scrollbar.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-04-10-grain-temp-records-filter-toolbar.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-04-09-acceptance-smoke-script.md`
   - `zzz-docs/验证/数据库优先MVP-回归验证清单.md`

【当前主线与口径】
- 过程记录走 `devflow`，真相源在 `.devflow/grain-platform-bootstrap/`
- `.explore/grain-platform-bootstrap/` 仅历史快照
- 本期「数据库优先 MVP」；不做「预测 -> 修正 -> 再预测」必做入口（字段可保留）
- 预测归档：`prediction_task + prediction_result`
- 正式前端：`frontend/`；`frontend-next/` 仅静态原型参考

【020 以来已完成（恢复时不必重做）】
1. 用户页 + 答辩大屏：角色说明与 `/screen` 说明/预警区已表格化；`styles.css` 含 `.screen-data-table`
2. 列表模糊搜索：
   - 工具：`frontend/src/utils/fuzzyText.js`
   - 前端过滤：用户/仓库/粮温汇总表/预测两表/首页三块列表
   - 后端分页：`GET /api/grain-temp/records`、`GET /api/sensor-data` 支持可选 `keyword`（LIKE）
3. 协作规则：`.cursor/rules/project-zh.mdc` 已加强「实现前对齐」
4. 管理端布局（2026-04-10）：`ConsoleLayout` 视口限高 + 侧栏/主区 `el-scrollbar`，见 devflow plan `2026-04-10-console-layout-scroll-and-scrollbar.md`
5. 粮温原始测点记录多条件筛选（2026-04-10）：
   - 后端：`GET /api/grain-temp/records` 可选 `pointNo`、`tempMin`、`tempMax`；`GET /api/grain-temp/records/filter-options`
   - 前端：`DataView.vue` 粮温模式工具栏（区域/层号/点位、温度区间、采集时间、应用筛选/重置、关键词防抖）
   - 见 plan `2026-04-10-grain-temp-records-filter-toolbar.md`
6. Git：`shuowang/dev2.0` 已提交 `16cabbc`（上述体验优化 + devflow 文档）；**未**将 `.codex/`、`.cursor/` 下大量未跟踪 GSD 工具文件纳入该提交
7. 静态验证曾执行：`backend` mvn compile、`frontend` npm run build（恢复后重大改动请再跑）

【未完成 / 未讨论完 / 可选】
1. 答辩前再扫 `zzz-docs/设计文档/` 非归档文档，补「数据库优先 MVP」文首提示
2. `run-acceptance-smoke.ps1`：未专门断言带 `keyword` 的分页；亦未断言粮温记录带 `pointNo`/`tempMin`/`tempMax` 或 `filter-options`（可选轻量 smoke）
3. `zzz-docs/验证/数据库优先MVP-回归验证清单.md` 可补充「粮温多条件筛选」手工步骤（答辩演示用）
4. 用户/仓库若数据量很大，是否改为后端 `keyword`（尚未拍板）
5. `/screen` 大屏表格未加本地筛选（可选）
6. 沙箱内脚本嵌套前端构建仍可能 `esbuild spawn EPERM` → 使用 `./scripts/run-acceptance-smoke.ps1 -SkipStaticChecks`
7. worktree 内与课题无关路径（如 `.codex/`、`.claude/`）可能有独立变更，**勿误提交**；GSD 拟用于后续优化时，`.planning/` 尚未初始化，初始化前勿整包提交 GSD 工具目录
8. 若需同步远端：本地对 `16cabbc` 及后续提交执行 `git push`（handoff 022 后若有新 commit 一并推送）

【下次从这里继续】
1. 新需求：Mini Align → plan（若需要）→ 实施；默认遵守 `project-zh.mdc` §3
2. 验收：`./scripts/run-acceptance-smoke.ps1` 或 `-SkipStaticChecks`
3. 文档冲突：以 `state.md` + 最新 handoff **022** 为准
4. GSD：后续优化波次可用 `/gsd-new-project`（可选先 `/gsd-map-codebase`）；与 devflow 并行，PROJECT 中可引用 `state.md` 作毕设真相源

【本地环境】
- 联调端口：`8081`；前端默认 API `http://localhost:8081`
- 占端口时先清理旧 Java 进程；库重置：`scripts/reset-demo-db.ps1`
```
