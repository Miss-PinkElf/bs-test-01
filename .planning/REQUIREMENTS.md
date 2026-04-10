# Requirements: 粮仓平台（GSD 增量）

**Defined:** 2026-04-10  
**Core Value:** 粮温与环境数据可导入、可查、可汇总，预测结果可追溯展示（见 `PROJECT.md`）

## v1 Requirements

本轮「粮温固定模板」可读性与导入一致性。

### Data Import / Template

- [x] **DATA-01**: 用户下载的粮温固定模板在 Excel 中层级清晰：中文说明、合并单元格、表头分区；关键列仍带英文键便于程序识别
- [x] **DATA-02**: `GrainTempImportService.getExcelTemplate` 与 `parseFixedTemplate` 规则一致；兼容旧版纯英文标签
- [x] **DATA-03**: `DataView.vue` 粮温导入说明与模板结构一致
- [x] **DATA-04**: `mvn compile`、`npm run build` 已通过（2026-04-10）；端到端导入建议在联调环境手工或 smoke 再验

## Phase 2 Requirements（温度预测页）

### Prediction / Console UX

- [x] **PRED-01**: 温度预测页将任务元数据（任务号、仓库、预测对象、算法、风险等级、预测天数、训练/预测区间、执行时间、摘要）以分组 + 栅格化布局展示，避免零散纵向堆叠，一屏内可读性提升（2026-04-10：`el-descriptions` 双块，待你验收）
- [x] **PRED-02**: 预测表单支持可选「训练数据开始时间」「训练数据结束时间」，调用预测接口时映射为 `trainStartTime` / `trainEndTime`；两者皆空时行为与当前「使用全部可用历史」一致（2026-04-10：折叠内 `datetimerange` + 清空）
- [x] **PRED-03**: 训练区间无效或历史数据不足时，用户可见明确错误提示（承接后端 `IllegalArgumentException` 等文案或前端校验）（2026-04-10：前端区间校验 + `ElMessage.error`）

## Phase 3 Requirements（温度预测页 · 交互二次）

### Prediction flow / copy

- [x] **UX2-01**: 页面区块顺序为「参数与摘要 → 双线图 → 预测结果表与历史归档表」；执行预测后用户无需滚到页底即可看到主曲线（2026-04-10：已调整顺序并 `scrollIntoView`）
- [x] **UX2-02**: 「预测结果列表」与「历史归档记录」卡片均展示一句副标题，说明各自数据含义及历史表「点击行切换当前任务」（2026-04-10：副标题 + 图表区说明）
- [x] **UX2-03**: 温度预测页首行（参数 \| 任务摘要）与次行（预测结果 \| 历史归档）在 **`md`（≥992px）起** 即按15:9 并排；`xs`/`sm` 整行叠放。避免仅配置 `xl` 导致 Element Plus 在常见笔记本宽度下将两列堆叠为上下布局（2026-04-10：`PredictionView.vue` `el-col` 断点）

## v2 Requirements

- [ ] **DATA-05**: 验收脚本 `run-acceptance-smoke.ps1` 增加针对新版模板版式或关键标签的断言（可选）

## Out of Scope

| Feature | Reason |
| --- | --- |
| 重写非粮温导入链路 | 未在本次诉求中 |
| 多算法切换与预测模型替换 | 本期 Phase 2 仅 UX + 训练窗口；算法仍线性回归 MVP |

## Traceability

| Requirement | Phase | Status |
| --- | --- | --- |
| DATA-01 | Phase 1 | Done |
| DATA-02 | Phase 1 | Done |
| DATA-03 | Phase 1 | Done |
| DATA-04 | Phase 1 | Done |
| PRED-01 | Phase 2 | Done（待 UAT） |
| PRED-02 | Phase 2 | Done（待 UAT） |
| PRED-03 | Phase 2 | Done（待 UAT） |
| UX2-01 | Phase 3 | Done（待 UAT） |
| UX2-02 | Phase 3 | Done（待 UAT） |
| UX2-03 | Phase 3 | Done（待 UAT） |

**Coverage:**

- Phase 1 / 模板 v1：4 条（DATA-*，已交付）
- Phase 2 / 预测页：3 条（PRED-*）
- Phase 3 / 预测交互：3 条（UX2-*）
- Mapped to phases: 10
- Unmapped: 0

---
*Last updated: 2026-04-10 Phase 3 补充 UX2-03（响应式栅格断点）*
