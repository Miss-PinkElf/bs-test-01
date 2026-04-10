# Phase 2: 温度预测页 - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.  
> Decisions are captured in `02-CONTEXT.md`.

**Date:** 2026-04-10  
**Phase:** 02 — 温度预测页体验优化  
**Areas discussed:** 摘要布局、训练区间、校验与错误、双表与图表位置

---

## 摘要与参数（A–D）

| 议题 | 选项摘要 | Selected |
|------|----------|----------|
| A 任务摘要 | `el-descriptions` 分组栅格 | 采纳助手推荐 |
| B 训练区间 | `datetimerange` + 清空 = 不传参 | 采纳助手推荐 |
| C 表单位置 | 同卡片 `el-collapse` 默认折叠 | 采纳助手推荐 |
| D 错误 | 简单区间校验 + `ElMessage.error` | 采纳助手推荐 |

**User's choice:** 用户表示「算了，按照你推荐的来」（先前若选过「下条逐条说」，本条改为整包采纳推荐方案）。

**Notes:** ISO 时间格式选用 `YYYY-MM-DDTHH:mm:ss` 写入 JSON，与 Spring Boot 默认 `LocalDateTime` 反序列化常见配置一致。

---

## 历史归档与结果表距离

| 议题 | 说明 | Selected |
|------|------|----------|
| 是否合并为一张表 | 保留双表，语义不同（当前任务明细 vs 归档列表） | 保留两个表 |
| 距离过大原因 | 全宽「双线图」插在参数区与双表之间，滚动路径长 | 将图表移到双表下方 |

**User's choice:** 认同「有点远」为布局顺序问题；仍用两个表，通过图表后置收紧两表的视觉关系。

---

## the agent's Discretion

- `el-descriptions` 列数、折叠内日期选择器宽度类等实现细节由实现者按现有 `styles.css` 约定处理。

## Deferred Ideas

- 多算法、外生变量 — 见 `02-CONTEXT.md` / `REQUIREMENTS.md` Out of Scope。
