# 粮温原始测点记录：多条件筛选工具栏

## 背景（Mini Align）

- **目标**：环境数据页「粮温原始测点记录」表格上方由单一模糊搜索，升级为与表头对齐的筛选工具栏：区域 / 层号 / 点位为下拉，温度为区间，采集时间为时间范围；仓库仍以页面右上方「查询条件」卡片为准。
- **范围**：后端 `GET /api/grain-temp/records` 扩展查询参数；新增筛选项接口；前端 `DataView.vue` 粮温模式 + `grain.js` + 相关样式。
- **不做**：普通环境数据（`sensor-data`）表格暂不复制同一套多维筛选（可后续单独立项）；验收脚本暂不强制断言新参数（可选后续补 smoke）。

## 问题与方案摘要

| 项 | 内容 |
| --- | --- |
| **现象** | 仅一条关键词框，无法按表头维度精确筛选。 |
| **原因** | 后端分页接口已支持 `warehouseId`、`startTime`、`endTime`、`zoneCode`、`layerNo` 等，但前端只传了 `warehouseId` 与 `keyword`；缺 `pointNo`、温度区间及下拉选项来源。 |
| **解决** | 扩展 `listRecordPage` / Mapper `WHERE`：`pointNo`、`tempMin`、`tempMax`；新增 `filter-options` 聚合去重下拉；前端工具栏 +「应用筛选/重置」+ 关键词防抖。 |

## 接口与文件

| 位置 | 说明 |
| --- | --- |
| `GET /api/grain-temp/records` | 增加可选 `pointNo`、`tempMin`、`tempMax`（余同前）。 |
| `GET /api/grain-temp/records/filter-options` | 可选 `warehouseId`，返回 `GrainTempRecordFilterOptionsDto`。 |
| `GrainTempRecordMapper.xml` | `recordQueryFromWhere` 增加点位与温度条件；`selectDistinctZoneCodes` / `LayerNos` / `PointNos`。 |
| `GrainTempRecordMapper.java`、`GrainTempService.java`、`GrainTempController.java` | 签名与实现串联。 |
| `dto/grain/GrainTempRecordFilterOptionsDto.java` | 筛选项 DTO。 |
| `frontend/src/api/grain.js` | `fetchGrainTempRecordFilterOptions`、`fetchGrainTempRecords` 参数。 |
| `frontend/src/views/DataView.vue` | `grainRecordFilters`、`grainCollectedRange`、`loadGrainRecordFilterOptions` 等。 |
| `frontend/src/styles.css` | `.grain-record-filter-form`、`.grain-temp-range`、`.grain-filter-daterange` 等。 |

## 轻量任务（已完成）

- [x] 后端条件与筛选项接口
- [x] 前端工具栏与请求串联
- [x] `mvn compile` / `npm run build` 验证

## 后续可选

- [ ] `run-acceptance-smoke.ps1` 增加一条带结构化参数的列表请求断言
- [ ] 回归验证清单 `zzz-docs/验证/数据库优先MVP-回归验证清单.md` 增加「粮温记录多条件筛选」手工步骤（若答辩需要）
