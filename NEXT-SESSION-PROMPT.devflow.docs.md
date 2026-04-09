你现在在仓库 `E:\Learn\Vs\Code\bs-test-01`，请继续这个毕业设计项目的**文档对齐 / 论文支撑 / 需求真相源整理**工作，并严格优先读取和遵守以下文档，然后再开始任何分析、补文档或继续拆分。当前这个入口只服务于文档型 mission，请把 `.devflow/grain-docs-alignment/` 视为该 mission 的唯一过程真相源，原 `.explore/` 仅作为历史快照保留。

必须先读取：
1. `.codex/skills/devflow/SKILL.md`
2. `.devflow/grain-docs-alignment/state.md`
3. `.devflow/grain-docs-alignment/handoffs/2026-04-09-002-devflow-migration-ready.md`
4. `.devflow/grain-docs-alignment/plans/active-plan-links.md`
5. 再按需读取以下真相源文档：
   - `zzz-docs/2.任务书.md`
   - `zzz-docs/开题报告.md`
   - `zzz-docs/需求清单-任务书与开题报告对齐.md`
   - `zzz-docs/开发指导版PRD-粮仓环境数据预测管理平台.md`

当前唯一有效主线：
- 当前长期文档记录统一走 `devflow`
- `.devflow/grain-docs-alignment/` 是当前文档 mission 的主真相源
- 原 `.explore/grain-docs-alignment/` 只保留历史快照，不再作为当前主真相源
- 该 mission 的职责是：任务书 / 开题报告 / 需求清单 / PRD / 论文支撑文档的对齐、补充与恢复
- 若要进入正式开发，请切换到主开发 mission：`.devflow/grain-platform-bootstrap/`

本 mission 已完成：
- 任务书与开题报告已转换为 Markdown
- 已完成需求清单与来源映射
- 已完成开发指导版 PRD
- 已完成从 `.explore/grain-docs-alignment/` 到 `.devflow/grain-docs-alignment/` 的复制迁移

当前更适合继续做的事情：
1. 补论文图示、模块说明、系统结构说明
2. 把现有 PRD / 需求清单继续拆成数据库、接口、页面三级文档
3. 校对 `zzz-docs/` 下是否还有旧口径或重复文档需要归档/收口

当前注意事项：
- 这个 prompt 仅用于文档型 mission，不要直接在这里推进正式开发实现
- 如果当前任务已经进入代码、数据库或前后端实现，请改用 `NEXT-SESSION-PROMPT.devflow.md`
- 若文档与主开发 mission 的最新 `state.md` / handoff 冲突，以对应 mission 的最新真相源为准，不要混用旧结论

如果用户没有再次改变方向，就先从“当前更适合继续做的事情”的第 1 条开始判断，并在推进过程中持续更新 `.devflow/grain-docs-alignment/` 下的状态、决策、checkpoint 与 handoff。
