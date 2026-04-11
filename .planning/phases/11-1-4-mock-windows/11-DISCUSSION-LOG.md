# Phase 11: 河南环境数据 1-4 月 mock、脏数据清理与 Windows 一键运行 - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-11
**Phase:** 11-河南环境数据 1-4 月 mock、脏数据清理与 Windows 一键运行
**Areas discussed:** 数据落地方式, mock 数据密度, 脏数据清理口径, 范围裁剪

---

## 数据落地方式

| Option | Description | Selected |
|--------|-------------|----------|
| 写入 `schema.sql` | 把 1-4 月标准演示数据固化到重置脚本主链，重置后即得到标准库 | ✓ |
| 单独 mock 脚本 | 额外做生成/导入脚本，重置库与 mock 数据分开维护 | |
| 其他 | 用户自定义 | |

**User's choice:** 直接把“干净且可答辩”的标准演示数据沉到 `backend/src/main/resources/db/schema.sql`。
**Notes:** 用户接受推荐方案，目标是“重置后标准演示库”。

---

## mock 数据密度

| Option | Description | Selected |
|--------|-------------|----------|
| 稀疏样例 | 每仓每指标少量点，最低成本但图表表现弱 | |
| 中等连续密度 | 2-3 个重点仓库，覆盖温度/湿度/co2，1-4 月按天或按半天采样 | ✓ |
| 高密度 | 更高采样频率，数据量更大 | |

**User's choice:** 采用“2-3 个重点仓库 × 温度/湿度/co2 × 1-4 月按天或按半天采样”的中等连续密度。
**Notes:** 用户接受推荐方案，优先保证图表连续性与答辩可讲述性，而不是追求大数据量。

---

## 脏数据清理口径

| Option | Description | Selected |
|--------|-------------|----------|
| 重置为准 | 把 demo 库定义成可重建，清理以重置为主 | ✓ |
| 规则化清洗 | 保留旧数据，再做标脏/清洗逻辑 | |
| 其他 | 用户自定义 | |

**User's choice:** 直接把当前 demo 库定义成“可重建”，清理动作以重置为主，不做复杂清洗引擎。
**Notes:** 用户接受推荐方案，目标是收口出干净稳定的标准演示库。

---

## 范围裁剪

| Option | Description | Selected |
|--------|-------------|----------|
| 保留一键运行 | 与数据 phase 一起做 | |
| 拆出单独 phase | 先从 Phase 11 排除，后续单独规划 | ✓ |
| 其他 | 用户自定义 | |

**User's choice:** Windows 一键运行 / 类 docker 方案不在本 phase 做，后续单独开 phase。
**Notes:** 本次 discuss 只保留 mock 数据与脏数据清理两块。

---

## the agent's Discretion

- 具体选用哪 2-3 个重点仓库
- 最终采用按天还是按半天频率
- 与标准演示库口径冲突的旧备注、质量标记或样本文案如何最小代价清理

## Deferred Ideas

- Windows 一键运行 / 类 docker 免安装依赖环境
- 更复杂的脏数据识别与清洗规则引擎
