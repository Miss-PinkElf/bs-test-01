# Handoff

## 基础信息

- 创建时间：2026-04-09
- mission：grain-platform-bootstrap
- 当前阶段：Resume-ready after devflow migration
- handoff 编号：018
- 是否 superseded：否

## 当前目标

- 把长期过程记录入口从 `.explore/` 平滑迁移到 `.devflow/`。
- 让下次恢复时直接按 `devflow` 真相源继续当前业务主线，而不是再依赖旧的 `.explore/` 恢复入口。

## 当前进度

- 已完整复制 `.explore/grain-platform-bootstrap/` 到 `.devflow/grain-platform-bootstrap/`。
- 已把新副本中的关键路径统一改写为 `.devflow/grain-platform-bootstrap/`。
- 已新增计划索引：`.devflow/grain-platform-bootstrap/plans/active-plan-links.md`。
- 已新增新的恢复提示词副本：`NEXT-SESSION-PROMPT.devflow.md`。
- 原 `.explore/grain-platform-bootstrap/` 与原 `NEXT-SESSION-PROMPT.md` 保持不变，可作为历史快照保留。
- 业务实现状态保持不变：
  - 数据库优先 MVP 运行态 smoke 已通过
  - 数据主线 CRUD 已完成
  - 后台页列表统一第一轮已完成
  - 数据管理页后端分页适配已完成

## 本轮完成内容

- [x] 复制 mission 工作区到 `.devflow/`
- [x] 改写 `.devflow` 副本中的路径引用
- [x] 补齐 devflow 版计划入口索引
- [x] 新建 devflow 版最新 handoff
- [x] 新建 `NEXT-SESSION-PROMPT.devflow.md`
- [x] 保留原 `.explore/` 与原提示词不动

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 采用“复制迁移”而不是原地修改 `.explore/` | 直接改原 `.explore/` | 用户明确要求原 explore 不动；复制迁移更安全 |
| 新提示词另存为 `NEXT-SESSION-PROMPT.devflow.md` | 覆盖原 `NEXT-SESSION-PROMPT.md` | 用户明确要求原提示词不动，同时需要新对话能直接进入 devflow |
| 新工作区增加 `plans/active-plan-links.md` | 只继续引用外部 `docs/` 计划 | devflow 默认把 plan 视为主线记录的一部分，增加一个入口索引更利于恢复 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `.devflow/grain-platform-bootstrap/state.md` | 当前 devflow mission 状态真相源 | 最高 |
| `.devflow/grain-platform-bootstrap/workflow.md` | 当前 mission 目标、阶段与边界 | 最高 |
| `.devflow/grain-platform-bootstrap/decision-log.md` | 记录迁移决策与历史关键选择 | 高 |
| `.devflow/grain-platform-bootstrap/checkpoints.md` | 迁移 checkpoint 与近期里程碑 | 高 |
| `.devflow/grain-platform-bootstrap/plans/active-plan-links.md` | 当前有效计划入口索引 | 高 |
| `NEXT-SESSION-PROMPT.devflow.md` | 新对话可直接复制的 devflow 提示词 | 最高 |

## 风险 / 阻塞项 / 开放问题

- [ ] 当前只是过程记录迁移，业务实现本身未新增。
- [ ] `spring.sql.init.mode=always` 仍会在每次后端启动时重建演示库。
- [ ] 仓库 / 用户管理 CRUD 仍未补完整。
- [ ] PowerShell 验收脚本仍未补。

## 立即下一步

1. 从新的 devflow 恢复入口继续评估是否保留 `spring.sql.init.mode=always`。
2. 若继续补后台管理闭环，优先做仓库 CRUD，再做用户 CRUD。
3. 若先补验收能力，则补一个 PowerShell 脚本串起导入与 CRUD smoke。

## 恢复指引

1. 先读取根目录 `NEXT-SESSION-PROMPT.devflow.md`
2. 再读取 `.devflow/grain-platform-bootstrap/state.md`
3. 再读取本 handoff：`2026-04-09-018-devflow-migration-ready.md`
4. 按需读取：
   - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
   - `docs/superpowers/plans/2026-04-09-data-crud-and-temperature-recompute.md`
   - `docs/superpowers/plans/2026-04-09-frontend-list-unification.md`
5. 从“立即下一步”的第 1 条开始继续

## 可从活跃上下文移除的内容

- `.explore -> .devflow` 文本替换的逐条过程
- 本轮为了迁移而做的目录级复制细节
