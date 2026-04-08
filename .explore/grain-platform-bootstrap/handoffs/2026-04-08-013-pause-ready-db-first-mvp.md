# Handoff

## 基础信息

- 创建时间：2026-04-08
- mission：grain-platform-bootstrap
- 当前阶段：Pause-ready after DB-first MVP alignment
- handoff 编号：013
- 是否 superseded：否

## 当前目标

- 为本次休息创建一个可直接恢复的暂停交接点。
- 固化当前唯一有效主线：数据库优先 MVP。
- 让下次会话无需重新讨论方向，直接从数据库验证和代码改造开始。

## 当前进度

- 已完成主线二次收口并落盘：
  - 本期不再把“预测 -> 修正 -> 再预测”作为必做链路
  - 修正链降级为扩展能力预留
  - 本期不拆独立归档表
  - 湿度 / 二氧化碳保留简单单值辅线
  - 预测页支持可选预测天数，图表展示“实际值 + 预测值”双线
- 已完成以下文档更新：
  - `.explore/grain-platform-bootstrap/workflow.md`
  - `.explore/grain-platform-bootstrap/state.md`
  - `.explore/grain-platform-bootstrap/decision-log.md`
  - `.explore/grain-platform-bootstrap/checkpoints.md`
  - `.explore/grain-platform-bootstrap/session-tasks.md`
  - `.explore/grain-platform-bootstrap/handoffs/index.md`
  - `NEXT-SESSION-PROMPT.md`

## 本轮完成内容

- [x] 检查当前已完成内容是否需要额外记录
- [x] 追加暂停前 checkpoint：`2026-04-08-008`
- [x] 创建本 handoff：`2026-04-08-013-pause-ready-db-first-mvp.md`
- [x] 更新 `handoffs/index.md` 指向本 handoff
- [x] 更新 `state.md` 的最新 handoff 指向
- [x] 更新 `NEXT-SESSION-PROMPT.md` 为可直接复制的恢复提示

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 休息前再新增一份 handoff | 继续沿用 `012` | `012` 更偏“方向收口”，本 handoff 更偏“暂停恢复”，恢复入口更清晰 |
| `NEXT-SESSION-PROMPT.md` 继续保留在仓库根目录 | 只依赖 `.explore` 内 handoff | 便于用户直接复制，降低恢复门槛 |
| 本轮先提交文档，不推进实现 | 继续做数据库验证或代码改造 | 用户明确要先休息，此时最重要的是交接质量和恢复效率 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `.explore/grain-platform-bootstrap/handoffs/2026-04-08-013-pause-ready-db-first-mvp.md` | 本次暂停的主恢复入口 | 最高 |
| `.explore/grain-platform-bootstrap/state.md` | 当前事实、下一步与最新 handoff | 最高 |
| `NEXT-SESSION-PROMPT.md` | 可直接复制的下次恢复提示词 | 最高 |
| `.explore/grain-platform-bootstrap/checkpoints.md` | 本轮暂停前 checkpoint | 高 |

## 关键验证结果

- 文本级验证通过：
  - `.explore` 记录已与“数据库优先 MVP”主线一致。
  - `NEXT-SESSION-PROMPT.md` 已可作为恢复提示直接复制。
- 运行级验证未开始：
  - 仍未确认 MySQL 凭据
  - 仍未真实导入 schema
  - 仍未开始本轮代码改造

## 风险 / 阻塞项 / 开放问题

- [ ] MySQL 真实账号密码未知，`schema.sql` 导入仍被阻塞。
- [ ] 后端仍是旧预测口径，尚未按本期新主线改造。
- [ ] 前端 `PredictionView.vue`、`DataView.vue`、`frontend/src/api/grain.js` 仍未切换到新交互与新接口。
- [ ] 部分滚动预测文档仍保留“修正链本期必做”的描述，后续应统一收口。

## 立即下一步

1. 读取 `NEXT-SESSION-PROMPT.md`
2. 确认真实可用的 MySQL 账号密码
3. 实际执行 `backend/src/main/resources/db/schema.sql`
4. 验证以下表是否创建成功：
   - `grain_temp_point`
   - `grain_temp_record`
   - `grain_temp_summary`
   - `prediction_task`
   - `prediction_result`
   - `sensor_data`
5. 再按“本期不做修正入口”的口径改后端与前端

## 恢复指引

1. 先读取根目录 `NEXT-SESSION-PROMPT.md`
2. 再读取 `.explore/grain-platform-bootstrap/state.md`
3. 再读取本 handoff
4. 若旧文档与本 handoff 冲突，以本 handoff 和 `state.md` 为准
5. 从“立即下一步”的第 2 条继续

## 可从活跃上下文移除的内容

- 本轮对 handoff、state、prompt 一致性的核对过程
- “是否还要记录文档”的确认性对话
