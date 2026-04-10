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

## v2 Requirements

- [ ] **DATA-05**: 验收脚本 `run-acceptance-smoke.ps1` 增加针对新版模板版式或关键标签的断言（可选）

## Out of Scope

| Feature | Reason |
| --- | --- |
| 重写非粮温导入链路 | 未在本次诉求中 |
| 预测算法升级 | 课题范围外增量 |

## Traceability

| Requirement | Phase | Status |
| --- | --- | --- |
| DATA-01 | Phase 1 | Done |
| DATA-02 | Phase 1 | Done |
| DATA-03 | Phase 1 | Done |
| DATA-04 | Phase 1 | Done |

**Coverage:**

- v1 requirements: 4 total
- Mapped to phases: 4
- Unmapped: 0

---
*Last updated: 2026-04-10 after 模板 v2 交付*
