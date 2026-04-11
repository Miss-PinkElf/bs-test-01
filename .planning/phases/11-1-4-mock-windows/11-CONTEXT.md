# Phase 11: 河南环境数据 1-4 月 mock、脏数据清理与 Windows 一键运行 - Context

**Gathered:** 2026-04-11
**Status:** Ready for planning

<domain>
## Phase Boundary

本阶段只处理两件事：

1. 为现有粮仓平台补齐适合演示与答辩的 **1 月到 4 月河南场景环境 mock 数据**，覆盖温度、湿度、二氧化碳浓度，并落到当前真实数据主链中。
2. 把当前 demo 数据库收口为 **可重建、可解释、可复位** 的标准演示库，清理现有脏数据。

本阶段 **不** 处理“任意 Windows 环境免安装依赖一键运行 / 类 docker 打包”能力，该内容已明确延后为单独 phase。

</domain>

<decisions>
## Implementation Decisions

### 演示数据基线

- **D-01:** 1 到 4 月 mock 数据直接固化到 `backend/src/main/resources/db/schema.sql`，作为“重置后标准演示库”的一部分，不额外建立独立 mock 数据生成器或单独导入脚本。
- **D-02:** 当前 demo 库的正确使用方式定义为“**可重建**”，即需要回到干净状态时，以 `scripts/reset-demo-db.ps1` 重置到标准演示库，而不是在已有脏数据上做复杂修补。

### mock 数据覆盖范围

- **D-03:** mock 数据覆盖 **温度、湿度、二氧化碳浓度** 三类指标；其中温度继续兼容当前平台的环境数据与预测链路口径，湿度与二氧化碳走现有 `sensor_data` 主线。
- **D-04:** mock 数据时间窗口固定为 **1 月 1 日到 4 月 30 日**，用于支撑 DataView 趋势图、普通环境数据分页列表、预测相关展示与答辩叙事。
- **D-05:** mock 数据只覆盖 **2 到 3 个重点仓库**，优先复用当前已有演示仓库而不是新增大量仓库，避免把 phase 扩成数据库结构重做。

### 数据密度与区域口径

- **D-06:** mock 数据密度目标是“图表连续、答辩够看、库体量不过大”，按 **天级到半天级** 采样设计；planner / executor 可在该范围内选取具体频率，但不能退化成稀疏到看不出趋势的几条样本。
- **D-07:** mock 数据应体现 **河南地区 1-4 月季节变化** 的合理趋势，保证温度、湿度、二氧化碳浓度整体走势能自圆其说、适合答辩讲解；不追求真实气象级精度，但不能像随机数。

### 脏数据清理策略

- **D-08:** 脏数据清理以“**一次性硬收口**”为主：把标准演示库写入 `schema.sql` 后，重置即得到干净数据；本阶段不做复杂清洗规则引擎，也不保留历史脏数据作为可回放样本。
- **D-09:** 如当前已有数据与标准演示口径冲突，应优先以“重置后标准数据”为准，而不是为了兼容旧脏数据去放宽数据口径或保留额外脏标记流程。

### the agent's Discretion

- 具体选用哪 2 到 3 个仓库、各仓库在 1-4 月的风险故事线如何分配，可由 planner 基于现有仓库样本与现有页面展示链路决定，但必须服务于“稳定仓 / 风险仓 / 对比仓”这类可讲述演示叙事。
- 在“按天”还是“按半天”之间的最终频率选择，可由 planner 结合 `schema.sql` 体量、页面性能与图表可读性决定，但应保持 D-06 的连续性目标。
- 是否顺带清理少量与标准演示库冲突的旧备注、质量标记或种子文案，可由 executor 在不扩大战线的前提下处理。

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 原始诉求与阶段约束

- `zzz-prompt-debug/origin/mock最近数据+添加一键运行脚本/prompt.md` — Phase 11 原始诉求；其中“一键运行”已在本次 discuss 中明确拆出本 phase
- `.planning/ROADMAP.md` — Phase 11 条目与当前 milestone 依赖
- `.planning/PROJECT.md` — 毕设 / 数据库优先 MVP 的总边界
- `.planning/STATE.md` — 当前阶段状态与历史 phase 演进记录

### 已锁定的前置模式

- `.planning/phases/07-zzz-prompt-debug-origin-prompt-md/07-CONTEXT.md` — `DataView.vue` 粮温与环境数据查询/图表链路的既有约束
- `.planning/phases/09-mock/09-CONTEXT.md` — “尽量走真实数据主链、不回退运行时 mock”的已锁定原则
- `.devflow/grain-platform-bootstrap/state.md` — 当前仓库关于显式重置演示库、数据库优先 MVP 的事实源

### 当前代码与数据入口

- `backend/src/main/resources/db/schema.sql` — 当前演示库 DDL 与种子数据入口；本 phase 的核心落点
- `scripts/reset-demo-db.ps1` — 当前标准重置路径；本 phase 的脏数据清理主入口
- `backend/src/main/java/com/grain/platform/controller/SensorDataController.java` — 普通环境数据分页、趋势、导入接口入口
- `backend/src/main/java/com/grain/platform/service/SensorDataService.java` — 普通环境数据分页、趋势与批量导入编排
- `backend/src/main/java/com/grain/platform/service/SensorDataImportService.java` — 普通环境模板与导入格式基线
- `backend/src/main/resources/mapper/SensorDataMapper.xml` — `sensor_data` 列表与趋势查询 SQL
- `frontend/src/views/DataView.vue` — 环境数据页主视图；趋势图、分页列表与导入入口都在这里
- `frontend/src/api/grain.js` — `fetchSensorData`、`fetchSensorTrend`、`importSensorData` 等前端接口封装

### 数据模型与产品说明

- `zzz-docs/设计文档/数据库设计-滚动预测与高温预警版.md` — `sensor_metric` / `sensor_data` 数据模型与字段含义
- `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计-滚动预测版.md` — `/api/sensor-data`、`/api/sensor-data/trend`、导入接口口径
- `zzz-docs/设计文档/粮仓环境数据预测管理平台-PRD-滚动预测闭环版.md` — 普通环境数据、展示与“真实接口优先”的产品口径

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `scripts/reset-demo-db.ps1` 已经提供“显式重置演示库”的现成入口，本 phase 可以直接复用，不必再发明第二套清库机制。
- `SensorDataController` + `SensorDataService` + `SensorDataMapper.xml` 已经把普通环境数据列表、趋势图、导入链路打通，新增的 1-4 月 mock 数据只要落到 `sensor_data` 即可被现有页面消费。
- `DataView.vue` 已同时具备环境趋势图、环境数据分页列表、导入模板与导入动作，是验证 mock 数据可见性的现成承载页。

### Established Patterns

- 当前仓库的 demo 数据初始化并非应用启动自动灌库，而是通过 `schema.sql` + `reset-demo-db.ps1` 显式重建；Phase 11 应沿用这个模式。
- 前面 phase 已明确管理端与数据页应尽量走真实接口与真实数据库主链，而不是回到运行时前端 mock。
- `sensor_data` 当前是“一个仓库、一个时间点、一个指标值一行”的单值模型，适合通过规则化批量种子数据构造时间序列。

### Integration Points

- 本 phase 的主要数据落点是 `backend/src/main/resources/db/schema.sql` 中的 `sensor_metric`、`sensor_data`、必要时关联到已有 `warehouse` 样本。
- 本 phase 的验证重点会落在 `DataView.vue` 的普通环境模式、环境趋势图、环境记录分页列表，以及必要时预测读取普通环境历史数据的链路。
- 如需清理旧脏数据，应优先围绕 `reset-demo-db.ps1` 的重置结果做验证，而不是在运行中新增单独的“清洗入口”页面或接口。

</code_context>

<specifics>
## Specific Ideas

- 用户明确说明其场景位于 **中国河南省**，因此 1-4 月 mock 数据需要体现河南春季过渡期的合理走势，而不是无规律随机波动。
- 用户接受的方向是：把“干净且可答辩”的标准演示数据直接沉到 `schema.sql`，并把 demo 库定义为可重建。
- 用户接受的规模控制是：`2-3 个重点仓库 × 温度/湿度/co2 × 1-4 月连续时间序列`，追求演示可读性，不追求海量数据。

</specifics>

<deferred>
## Deferred Ideas

- **Windows 一键运行 / 类 docker 免安装依赖环境**：用户已明确要求从 Phase 11 排除，后续单独开 phase 处理。
- 如未来需要“脏数据自动识别 / 标脏 / 清洗规则引擎”，属于后续增强能力，不纳入本阶段。

</deferred>

---

*Phase: 11-1-4-mock-windows*
*Context gathered: 2026-04-11*
