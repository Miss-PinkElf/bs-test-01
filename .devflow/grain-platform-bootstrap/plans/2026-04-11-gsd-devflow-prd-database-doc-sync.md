# GSD / devflow / PRD / 数据库文档同步计划

## 背景（Mini Align）

- **目标**：把当前仓库的论文类文档、设计文档和 devflow 过程记录，同步到 **GSD Phase 1-9 全量状态**，并与当前代码、数据库结构、接口契约保持一致。
- **范围**：
  - 读取 `.planning/PROJECT.md`、`REQUIREMENTS.md`、`ROADMAP.md`、`STATE.md`
  - 对照 `.devflow/grain-platform-bootstrap/` 当前真相源
  - 对照 `backend/src/main/resources/db/schema.sql` 与 Controller 实际接口
  - 更新 `zzz-docs/设计文档/` 下用于论文写作的 PRD、数据库设计、接口设计等文档
  - 在 `.devflow/grain-platform-bootstrap/` 内补 plan、decision、state、handoff
- **不做**：
  - 不改数据库结构
  - 不改后端或前端业务逻辑
  - 不把“修正预测”重新提升为本期必做主线

## 当前事实与差异

| 项 | 当前情况 |
| --- | --- |
| **阶段真相源** | `.planning` 已覆盖 Phase 1-9，其中 Phase 1-6 为 2026-04-10，Phase 7-9 为 2026-04-11 |
| **过程真相源** | `.devflow/grain-platform-bootstrap/state.md` 与 handoff 022 停留在 2026-04-10，尚未记录本轮 GSD 补齐与论文文档同步 |
| **数据库设计** | `schema.sql` 的 11 张主表结构与现有数据库设计文档总体一致，但论文文档缺少“已实现状态”和与页面/接口的映射说明 |
| **接口设计** | 旧接口文档仍保留 `/api/predictions/{taskId}/correct` 等迁移期表述，缺少分页、删除、大屏、`filter-options`、`/summaries/page` 等当前已实现接口 |
| **论文材料** | 现有 PRD / 数据库设计已能表达数据库优先 MVP，但还没完整覆盖 Phase 1-9 的交付结果和当前答辩展示口径 |

## 方案选择

| 方案 | 描述 | 结论 |
| --- | --- | --- |
| **A. 只补最新 7-9 文档** | 修改量小，但会让 1-6 的模板、预测页、删除、overflow 等优化继续散落在其他文档里 | 放弃 |
| **B. 以 Phase 1-9 全量口径重写核心文档** | 一次性把论文主文档与当前代码事实对齐；后续写论文更稳 | 采用 |
| **C. 归档旧文档后新建一套并行文档** | 可保留更多历史，但会增加真相源数量，不利于当前收口 | 放弃 |

## 最终选择

- 采用 **方案 B**：
  - 保留现有文件名，直接把核心文档更新到 **Phase 1-9 全量已实现口径**
  - 对明显偏迁移期的文档补“历史 / 扩展预留”说明，避免继续误导后续写作
  - 在 devflow 内落一份轻量计划、状态更新和 handoff，形成本轮记录闭环

## 轻量任务

- [ ] 补 `.devflow` 计划与决策
- [ ] 校对 `schema.sql` 与数据库设计文档
- [ ] 按 Phase 1-9 重写 PRD 关键章节
- [ ] 更新后端接口与 DTO/VO/Mapper 设计文档
- [ ] 补 handoff 与 state，说明本轮为“文档同步，无代码行为变更”

## 预期产出

- `zzz-docs/设计文档/粮仓环境数据预测管理平台-PRD-滚动预测闭环版.md`
- `zzz-docs/设计文档/数据库设计-滚动预测与高温预警版.md`
- `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计-滚动预测版.md`
- `.devflow/grain-platform-bootstrap/` 下的计划、状态、决策、handoff 同步记录
