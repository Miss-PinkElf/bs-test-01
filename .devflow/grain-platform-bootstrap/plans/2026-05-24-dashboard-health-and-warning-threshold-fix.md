# 2026-05-24 首页健康度历史温度口径与粮温预警阈值修复

## 背景

用户反馈两个问题：

- 「仓库运行健康度」中的「均温 / 峰值」看起来与「最新粮温汇总」一致，不符合“展示该仓库所有时间点粮温汇总的历史均值和峰值”的理解。
- 「环境数据」页中 6 号仓最高温只有 23.x°C，却显示 `ATTENTION`，而正常规则应为 `>= 28°C` 高温预警，`25°C ~ 28°C` 关注，低于 `25°C` 不预警。

## 问题原因

- 首页健康度 SQL 当前取的是最新粮温汇总的 `avg_temp`，并把最新预测任务的最大预测值作为“峰值”展示，字段名也沿用 `latestForecastValue`，导致展示口径混在“最新真实值 + 预测峰值”之间。
- `backend/src/main/resources/db/schema.sql` 中演示库种子数据对 6 号仓写了特殊低阈值：`collected_at >= 2026-04-18 08:40:00` 且最高温 `>= 23.20` 就标记 `ATTENTION`。
- 导入、手动重算、预测服务使用 `maxThreshold * 0.9` 作为关注阈值；在温度最大阈值为 `28` 时等于 `25.2`，与当前确认的 `25°C ~ 28°C` 关注口径不完全一致。

## 方案

1. 首页健康度接口新增清晰字段：
   - `historyAvgTemp`：该仓库所有粮温汇总记录的历史均温。
   - `historyMaxTemp`：该仓库所有粮温汇总记录的历史最高温。
2. 前端「仓库运行健康度」继续展示「均温 / 峰值」，但消费 `historyAvgTemp / historyMaxTemp`。
3. 演示库种子汇总规则统一为：
   - `max_temp >= 28`：`WARNING`
   - `max_temp >= 25`：`ATTENTION`
   - `max_temp < 25`：`NORMAL`
4. 导入、手动重算、预测预警同步使用 `25°C` 作为关注阈值下限。

## 验证计划

- `backend/` 执行 `mvn -q -DskipTests compile`。
- `frontend/` 执行 `npm run build`。
- 检查 `DashboardMapper.xml` 中健康度分页 SQL 已改为历史均温 / 历史峰值。
- 检查 `schema.sql` 不再包含 6 号仓 `23.20` 低阈值预警规则。

## 验证结果

- `backend/` 执行 `mvn -q -DskipTests compile` 通过。
- `frontend/` 执行 `npm run build` 通过。
- 已确认 `DashboardMapper.xml` 的健康度字段改为 `historyAvgTemp / historyMaxTemp`。
- 已确认 `backend/src/main/resources/db/schema.sql` 和主要读取 SQL 不再依赖 6 号仓 `23.20`、2 号仓 `26.45` 的特殊预警规则。

## 补充收口

- 为兼容已经落库的旧 `grain_temp_summary` 数据，新增一次性回写工具：
  - `scripts/rebuild-grain-temp-summary-warning.ps1`
  - `scripts/rebuild-grain-temp-summary-warning.sql`
- 作用：
  - 按当前统一规则批量重算旧汇总行的 `warning_level`、`warning_flag`、`warning_message`、`analysis_result`、`analysis_remark`
  - 统一规则为：
    - `max_temp >= 28`：`WARNING`
    - `25 <= max_temp < 28`：`ATTENTION`
    - `max_temp < 25`：`NORMAL`
- 说明：
  - 这是旧库修正工具，不改变本轮已经完成的读取层“按 `max_temp` 实时兜底重算”的页面逻辑。
  - 同时已更新 `backend/src/main/resources/db/schema.sql`，保证后续重置演示库时，`grain_temp_summary` 与 `prediction_task / prediction_result` 的演示数据也遵守本轮统一口径。
