```text
你现在在仓库 `E:\Learn\Vs\Code\bs-test-01`。请继续毕业设计项目，并严格先走 devflow 恢复流程后再开始任何分析、验证或开发。

【必须先读取】
1. `.codex/skills/devflow/SKILL.md`
2. `zzz-docs/任务书.md`
3. `zzz-docs/开题报告.md`
4. `.devflow/grain-platform-bootstrap/state.md`
5. `.devflow/grain-platform-bootstrap/handoffs/2026-04-09-019-pause-ready-after-init-and-warehouse-crud.md`
6. 按需读取：
   - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
   - `docs/superpowers/plans/2026-04-09-init-strategy-closure.md`
   - `docs/superpowers/plans/2026-04-09-warehouse-crud.md`
   - `zzz-docs/验证/数据库优先MVP-回归验证清单.md`

【当前主线与口径】
- 当前长期过程记录统一走 `devflow`
- `.explore/grain-platform-bootstrap/` 仅作为历史快照，不再作为当前主真相源
- 本期是“数据库优先 MVP”
- 不做“预测 -> 修正 -> 再预测”本期必做链路（字段保留，入口不做）
- 预测归档继续使用 `prediction_task + prediction_result`
- `frontend-next/` 仅作静态原型参考，不是正式实现目标

【当前已完成（本次会话内关键增量）】
1. 初始化策略收口完成
   - `backend/src/main/resources/application.yml` 已改为 `spring.sql.init.mode=never`
   - 默认启动不再自动重建演示库
   - 新增显式重置脚本：`scripts/reset-demo-db.ps1`
2. 仓库管理 CRUD 完成
   - 后端已补：`PUT /api/warehouses/{id}`、`DELETE /api/warehouses/{id}`
   - 前端 `WarehouseView.vue` 已支持新增 / 编辑 / 删除 / 删除确认 / 行点击详情联动
3. 验证证据已获取
   - `backend/`：`mvn -q -DskipTests compile` 通过
   - `frontend/`：`npm run build` 通过
   - 运行态仓库 CRUD smoke（create -> update -> delete）通过，临时数据已清理

【本次会话未完成 / 未开始】
1. 用户管理 CRUD 还没开始写代码
   - 仅完成了后端和前端现状读取、边界确认
   - 尚未新增用户 create/update/delete DTO、接口、Mapper、前端交互
2. PowerShell 验收脚本还没补
3. `UsersView.vue` 右侧角色说明区仍是卡片区，未做展示统一
4. `/screen` 大屏未纳入本轮展示统一

【下次必须从这里继续】
1. 优先实现用户 CRUD（后端 + 前端）
2. 完成静态 + 运行态验证并回写 devflow 状态/checkpoint
3. 再补 PowerShell 验收脚本

【本地环境注意】
- 联调端口统一使用 `8081`
- 前端默认 API：`http://localhost:8081`
- 若 `8081` 已被旧 Java 进程占用，先检查并清理再启动
- 若文档冲突，以 `.devflow/grain-platform-bootstrap/state.md` 与最新 handoff 为准
```
