# Handoff

## 基础信息

- 创建时间：2026-04-08
- mission：grain-platform-bootstrap
- 当前阶段：Align completed for DB-first MVP pivot
- handoff 编号：012
- 是否 superseded：否

## 当前目标

- 固化“主线再次变更”后的新真相源：本期以数据库、数据保存管理归档和双线图展示为核心。
- 明确本期不再把“预测->修正->再预测”作为必做链路，仅作为扩展能力预留。
- 让后续会话可直接从“导库验证 -> 后端改造 -> 前端双线图改造”继续。

## 当前进度

- 已完成 `context-budget-explore` 路由一（需求梳理/方向对齐）的落盘。
- 已确认并记录：
  - 本期主线：数据库优先 MVP
  - 本期不拆独立归档表
  - 修正链降级为扩展（字段保留，入口不做）
  - 湿度/二氧化碳保留简单单值线（文件导入 + 手工录入）
  - 预测页支持可选预测天数，图表展示“实际值 + 预测值”双线
- 已同步更新 `.explore/grain-platform-bootstrap/` 下 `workflow.md`、`state.md`、`decision-log.md`、`checkpoints.md`。

## 本轮完成内容

- [x] 记录“主线二次收口”决策到 `decision-log.md`
- [x] 更新 mission 当前阶段与目标到 `state.md`
- [x] 更新范围边界与退出条件到 `workflow.md`
- [x] 新增 checkpoint：`2026-04-08-007`
- [x] 新增 handoff：`2026-04-08-012-db-first-mvp-mainline-pivot-resume-ready.md`
- [x] 更新 `handoffs/index.md` 指向本 handoff

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 本期主线改为数据库优先 MVP | 继续以修正链闭环作为本期主线 | 用户明确老师重点是数据库、数据管理和归档；先确保主链稳定可交付 |
| 修正链降级为扩展 | 本期继续做修正接口和页面 | 可控范围内先完成主链，避免实现复杂度压垮进度 |
| 不拆独立归档表 | 新增 `archive` 表 | `prediction_task + prediction_result` 已足够表达归档，避免冗余与一致性成本 |
| 湿度/CO2 保留简单辅线 | 与粮温完全同构建模 | 题目要求覆盖温湿度数据管理，但无需引入测点复杂度 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `.explore/grain-platform-bootstrap/workflow.md` | 任务目标、范围边界与阶段规划（已更新到新主线） | 最高 |
| `.explore/grain-platform-bootstrap/state.md` | 当前事实、问题和下一步（已更新到新主线） | 最高 |
| `.explore/grain-platform-bootstrap/decision-log.md` | 本轮关键决策（#11/#12/#13） | 最高 |
| `.explore/grain-platform-bootstrap/checkpoints.md` | 本轮 checkpoint 落盘 | 高 |
| `NEXT-SESSION-PROMPT.md` | 会话恢复入口提示（需与本 handoff 保持一致） | 高 |

## 关键验证结果

- 文本级验证通过：
  - 已确认 `.explore` 相关状态文件已更新到“数据库优先 MVP”口径。
  - 已确认 `handoffs/index.md` 最新入口已切到本 handoff。
- 运行级验证未进行：
  - 尚未执行 MySQL 导库验证与后端/前端改造验证。

## 风险 / 阻塞项 / 开放问题

- [ ] MySQL 真实凭据未确认，`schema.sql` 仍无法完成真实导入验证。
- [ ] 现有后端代码仍是旧预测口径，尚未按新主线裁剪。
- [ ] 现有前端预测页与导入页尚未按“双线图 + 可选预测天数 + 无修正入口”改造。
- [ ] 部分旧文档仍含“修正链本期必做”表述，后续需统一收口。

## 立即下一步

1. 先确认 MySQL 可用账号密码并执行 `schema.sql` 实际导入。
2. 基于“本期不做修正入口”改后端：
   - 预测创建与历史查询保留
   - 修正接口先不实现
   - 保留扩展字段映射
3. 改前端：
   - 预测页参数增加可选预测天数
   - 图表固定双线（实际值/预测值）
   - 移除本期修正入口
4. 收口真相源文档与 `NEXT-SESSION-PROMPT.md` 到当前主线。

## 恢复指引

1. 先读取 `NEXT-SESSION-PROMPT.md`
2. 再读取 `.explore/grain-platform-bootstrap/state.md`
3. 再读取本 handoff
4. 若出现冲突，以本 handoff 和 `state.md` 的最新口径为准
5. 从“立即下一步”的第 1 条继续

## 可从活跃上下文移除的内容

- 关于“修正链是否本期必做”的多轮对话细节
- 关于是否新增独立归档表的重复比较过程
- 旧路线下“多指标独立短期预测”的实现讨论
