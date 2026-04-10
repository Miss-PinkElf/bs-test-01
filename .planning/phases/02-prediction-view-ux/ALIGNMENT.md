# Phase 2 需求对齐：温度预测页体验优化

**日期：** 2026-04-10  
**状态：** 已对齐（实施前唯一真相以本文 + `REQUIREMENTS.md` REQ 为准）

## 1. 背景与问题

- **现象：** `PredictionView.vue` 中任务摘要以多行 `<div>` 纵向堆叠，信息「散」，一屏难以形成整体印象。
- **现象：** 表单仅仓库、预测对象、预测天数；摘要中展示的算法、训练/预测区间等对用户而言「不可控」。
- **原因（工程）：** 后端 `PredictionRequest` 已支持可选 `trainStartTime` / `trainEndTime`，前端未传；算法在 `PredictionService` 中写死为线性回归（MVP）。

## 2. 目标与范围（本期做）

| 编号 | 内容 |
| --- | --- |
|信息架构 | 任务元数据改为分组 + 栅格化展示（Element Plus：`el-descriptions` 或等价卡片布局），保持与现有字段一致 |
| 参数扩展 | 表单增加可选「训练数据开始/结束时间」，序列化传入现有预测 API；留空 = 保持当前默认（全量可用历史） |
| 体验 | 区间非法或数据不足时，错误提示可读 |

## 3. 明确不做（本期）

- 多算法切换、更换 `ForecastService` 实现或新增模型训练管线。
- 预测修正闭环、气象等外生变量接入。
- 数据库表结构变更（除非后续规划单独 REQ）。

## 4. 技术线索（便于 plan-phase）

- 前端：`frontend/src/views/PredictionView.vue`、`frontend/src/api/grain.ts`（`predictMetric`请求体）。
- 后端：`PredictionRequest`、`PredictionService.loadActualSeries(PredictionRequest, …)` 已按区间过滤粮温序列。

## 5. 验收口径

- 与 `ROADMAP.md` Phase 2 **Success criteria** 三条一致。
- REQ 级检查见 `REQUIREMENTS.md` PRED-01 — PRED-03。
