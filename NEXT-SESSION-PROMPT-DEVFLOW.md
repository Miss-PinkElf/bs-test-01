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
5. `.devflow/grain-platform-bootstrap/handoffs/2026-04-10-021-pause-ready-after-table-search-and-cursor-rules.md`
6. 按需读取：
   - `.devflow/grain-platform-bootstrap/handoffs/index.md`
   - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
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
4. 静态验证曾执行：`backend` mvn compile、`frontend` npm run build（恢复后重大改动请再跑）

【未完成 / 未讨论完 / 可选】
1. 答辩前再扫 `zzz-docs/设计文档/` 非归档文档，补「数据库优先 MVP」文首提示
2. `run-acceptance-smoke.ps1` 未专门断言带 `keyword` 的分页请求（可选加一条）
3. 用户/仓库若数据量很大，是否改为后端 `keyword`（尚未拍板）
4. `/screen` 大屏表格未加本地筛选（可选）
5. 沙箱内脚本嵌套前端构建仍可能 `esbuild spawn EPERM` → 使用 `./scripts/run-acceptance-smoke.ps1 -SkipStaticChecks`
6. worktree 内与课题无关路径（如 `.codex/`、`.claude/`）可能有独立变更，勿误提交

【下次从这里继续】
1. 新需求：Mini Align → plan（若需要）→ 实施；默认遵守 `project-zh.mdc` §3
2. 验收：`./scripts/run-acceptance-smoke.ps1` 或 `-SkipStaticChecks`
3. 文档冲突：以 `state.md` + 最新 handoff `021` 为准

【本地环境】
- 联调端口：`8081`；前端默认 API `http://localhost:8081`
- 占端口时先清理旧 Java 进程；库重置：`scripts/reset-demo-db.ps1`
```
