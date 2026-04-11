# 2026-04-11 Phase 11 河南 Jan-Apr 基线与脏数据清理归档

## 交付主题

把 demo 库收口为可重建的标准演示库：围绕河南 1 到 4 月场景重写环境与粮温 baseline，清理旧 9 月冲突数据，并把这套 baseline 接进仓库现有的验收主链。

## 本次交付

- `backend/src/main/resources/db/schema.sql`
  已改为仓库 `1 / 2 / 6` 的 Jan-Apr 河南 baseline，不再保留旧 9 月主线。
- `scripts/reset-demo-db.ps1`
  已与 `schema.sql` 目标库语义对齐。
- `scripts/verify-demo-baseline.ps1`
  新增 reset 后 baseline 断言脚本，验证时间窗、行数、预警与旧样本清理情况。
- `scripts/run-acceptance-smoke.ps1`
  已接入 Phase 11 baseline gate，并补足 `/api/sensor-data`、`/api/sensor-data/trend`、`/api/grain-temp/summaries`、`/api/predictions/tasks` 与 dashboard 分页接口的 Phase 11 断言。
- `backend/src/main/java/com/grain/platform/service/GrainTempImportService.java`
  修复固定模板粮温导入在 smoke 中的误判问题，保证固定模板、legacy CSV、legacy 行式 Excel 三条导入链路都可通过。

## 验证结果

- `& '.\scripts\reset-demo-db.ps1'`：通过
- `& '.\scripts\verify-demo-baseline.ps1'`：通过
- `& '.\scripts\run-acceptance-smoke.ps1'`：通过
- 人工收口：用户同意本次结果可直接归档

## 归档文件

- `.planning/phases/11-1-4-mock-windows/11-01-SUMMARY.md`
- `.planning/phases/11-1-4-mock-windows/11-02-SUMMARY.md`
- `.planning/phases/11-1-4-mock-windows/11-VERIFICATION.md`
- `.planning/phases/11-1-4-mock-windows/11-HUMAN-UAT.md`

## 备注

- Phase 11 只交付 “Jan-Apr 河南 baseline + 脏数据清理 + baseline 验收链路收口”。
- Windows 一键运行 / 免安装依赖环境仍保持 deferred，不属于本次归档范围。
- 如后续继续做 Windows 一键运行、打包、或更强的 demo 数据管理能力，建议新开 phase 处理。
