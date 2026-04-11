# Mock 数据设计（滚动预测闭环版）

> **说明（2026-04-11）：** 本文档主要用于解释 `schema.sql` 中演示数据如何讲故事，包含“修正明显仓”“父子任务链”等扩展样本。它可以作为答辩时说明系统扩展能力的素材，但**不等于当前必做业务入口**。当前论文主口径请优先以最新 PRD、数据库设计和接口设计文档为准。

## 1. 文档目的

本文档用于为后续 `schema.sql` 和前后端联调准备一套符合新路线的 mock 数据方案。

目标：

- 能演示粮温 XLS 导入后的真实数据
- 能演示汇总分析
- 能演示首次预测
- 能演示新真实数据回填后的验证
- 能演示修正预测
- 能演示高温预警

## 2. mock 数据总体思路

建议至少准备 3 类仓库场景：

1. `稳定仓`
- 温度波动小
- 预测误差小
- 预警少

2. `升温风险仓`
- 温度在 9 月和 10 月逐步抬升
- 容易触发高温预警
- 适合展示“预测命中风险”

3. `修正明显仓`
- 第 1 轮预测和真实值偏差较明显
- 新真实数据回填后，第 2 轮修正预测更合理
- 适合展示“验证 + 修正”价值

## 3. 推荐仓库样本

| 仓库编号 | 仓库名称 | 用途 |
| --- | --- | --- |
| `WH-A01` | 一号平房仓 | 稳定仓样本 |
| `WH-A02` | 二号平房仓 | 升温风险仓样本 |
| `WH-C02` | 六号立筒仓 | 修正明显仓样本 |

## 4. 粮温测点 mock 设计

每个演示仓建议至少准备：

- 4 层
- 每层 4 个点位
- 共 16 个测点

对应示例：

- `A-1-1`
- `A-1-2`
- `A-1-3`
- `A-1-4`
- `A-2-1`
- ...
- `A-4-4`

这样既能体现“层温结构”，又不会让初版 mock 数据量失控。

## 5. 粮温原始记录 mock 设计

### 5.1 时间范围

建议准备：

- `2025-01-01` 到 `2025-10-31`

原因：

- 可覆盖“前 8 个月训练，后 2 个月预测/验证”的核心场景

### 5.2 数据粒度

- 按天 1 次汇总导入即可
- 每天 16 个测点

### 5.3 数据走势建议

#### 一号平房仓

- 1 月到 8 月整体平稳
- 9 月、10 月略升但不过阈值

#### 二号平房仓

- 7 月开始升温
- 9 月、10 月明显偏高
- 10 月部分日期超过预警阈值

#### 六号立筒仓

- 1 月到 8 月波动较大
- 第 1 轮预测偏保守
- 9 月真实值明显高于预测值
- 修正后 10 月预测更接近真实走势

## 6. 汇总分析 mock 设计

`grain_temp_summary` 建议至少存这些值：

- `avg_temp`
- `max_temp`
- `min_temp`
- `layer_1_avg`
- `layer_2_avg`
- `layer_3_avg`
- `layer_4_avg`
- `warning_level`
- `warning_flag`
- `warning_message`

### 6.1 预警阈值建议

为了演示效果，mock 数据中可采用：

- `< 26.0°C`：`NORMAL`
- `26.0°C ~ 27.9°C`：`ATTENTION`
- `>= 28.0°C`：`WARNING`

### 6.2 样例说明

#### 一号平房仓某日

- `avg_temp = 23.8`
- `max_temp = 24.9`
- `warning_level = NORMAL`

#### 二号平房仓某日

- `avg_temp = 27.2`
- `max_temp = 28.6`
- `warning_level = WARNING`
- `warning_message = 二层温度偏高，存在高温风险`

## 7. 预测任务 mock 设计

至少准备 3 轮演示任务：

### 任务 1：首次预测

- `task_no = TASK-ROLLING-001`
- `warehouse_id = 2`
- `task_round = 1`
- `trigger_type = INITIAL`
- `train_start_time = 2025-01-01`
- `train_end_time = 2025-08-31`
- `forecast_start_time = 2025-09-01`
- `forecast_end_time = 2025-10-31`
- `target_type = AVG_TEMP`

作用：

- 演示首次预测 9 月和 10 月每天温度

### 任务 2：9 月真实值回填后的修正预测

- `task_no = TASK-ROLLING-002`
- `parent_task_id = 1`
- `task_round = 2`
- `trigger_type = CORRECTION`
- `based_on_actual_end_time = 2025-09-30`
- `forecast_start_time = 2025-10-01`
- `forecast_end_time = 2025-11-30`

作用：

- 演示 9 月验证后修正 10 月与继续预测 11 月

### 任务 3：稳定仓预测

- `task_no = TASK-ROLLING-003`
- `warehouse_id = 1`
- `task_round = 1`
- `trigger_type = INITIAL`

作用：

- 演示低风险、低误差样本

## 8. 预测结果 mock 设计

每个任务的结果应至少覆盖：

- 每天一条结果
- 包含 `predicted_value`
- 在已验证日期补 `actual_value`
- 计算 `error_value`
- 计算 `error_rate`
- 生成 `warning_level`

### 8.1 任务 1 示例

#### 2025-09-01

- `predicted_value = 26.3`
- `actual_value = 26.8`
- `error_value = 0.5`
- `warning_level = ATTENTION`

#### 2025-10-08

- `predicted_value = 28.2`
- `actual_value = null`
- `warning_level = WARNING`

### 8.2 任务 2 示例

#### 2025-10-08`

- `predicted_value = 28.7`
- `actual_value = null`
- `warning_level = WARNING`
- `is_corrected = 1`

作用：

- 体现修正后预测比第 1 轮更接近真实升温趋势

## 9. 普通环境数据 mock 设计

湿度和二氧化碳建议保留少量数据即可：

- 每个仓库每月 2 到 4 条
- 主要用于保留旧页面和环境数据查询能力
- 不必再把它们做成主展示线

## 10. 推荐的 `schema.sql` mock 组织方式

建议分为 4 组插入：

1. 基础档案
- 仓库
- 用户
- 角色
- 指标字典

2. 粮温结构
- `grain_temp_point`
- `grain_temp_record`
- `grain_temp_summary`

3. 预测闭环
- `prediction_task`
- `prediction_result`

4. 普通环境数据
- `sensor_data`

## 11. 最终结论

这套 mock 数据设计的重点不是“量特别大”，而是“能讲完整故事”。

只要做到下面这 5 点，就足够支撑演示：

1. 有真实粮温原始数据
2. 有系统汇总分析结果
3. 有首次预测
4. 有真实值回填后的误差验证
5. 有修正后的新一轮预测和高温预警
