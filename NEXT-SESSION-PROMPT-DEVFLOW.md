```text
你现在在仓库 `D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01`。请继续毕业设计项目，并严格先走 devflow 恢复流程后再开始任何分析、验证或开发。

【必须先读取】
1. `.codex/skills/devflow/SKILL.md`
2. `zzz-docs/任务书.md`
3. `zzz-docs/开题报告.md`
4. `.devflow/grain-platform-bootstrap/state.md`
5. `.devflow/grain-platform-bootstrap/handoffs/2026-04-09-020-pause-ready-after-user-crud-and-acceptance-smoke.md`
6. 按需读取：
   - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-04-09-user-crud.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-04-09-acceptance-smoke-script.md`
   - `zzz-docs/验证/数据库优先MVP-回归验证清单.md`

【当前主线与口径】
- 当前长期过程记录统一走 `devflow`
- `.explore/grain-platform-bootstrap/` 仅作为历史快照，不再作为当前主真相源
- 本期是“数据库优先 MVP”
- 不做“预测 -> 修正 -> 再预测”本期必做链路（字段保留，入口不做）
- 预测归档继续使用 `prediction_task + prediction_result`
- `frontend-next/` 仅作静态原型参考，不是正式实现目标

【当前已完成（本次会话内关键增量）】
1. 用户管理 CRUD 已完成
   - 后端已补：`POST /api/users`、`PUT /api/users/{id}`、`PUT /api/users/{id}/password`、`DELETE /api/users/{id}`
   - 已补齐多角色维护、所属仓库维护、密码重置、最后一个启用管理员保护与引用删除拦截
   - 前端 `UsersView.vue` 已支持新增 / 编辑 / 删除 / 重置密码弹窗交互
2. PowerShell 验收脚本已完成
   - 新增：`scripts/run-acceptance-smoke.ps1`
   - 已串起：后端编译、前端构建、演示库重置、后端启动、用户 CRUD、固定模板下载/导入、旧 CSV / 旧行式 Excel 导入、首页概览与预测只读接口检查
   - 运行态脚本链路已通过：`./scripts/run-acceptance-smoke.ps1 -SkipStaticChecks`
3. 启动脚本已收口
   - `scripts/start-backend.ps1` 已处理 UTF-8 编码问题
   - 当前 `powershell.exe` 与 `pwsh.exe` 都可稳定拉起 `8081`
   - 启动时优先复用用户目录 `.m2/repository`
4. 验证证据已获取
   - `backend/`：`mvn -q -DskipTests compile` 通过
   - `frontend/`：`npm run build` 通过
   - 用户 CRUD smoke 通过，临时数据已清理
   - 验收脚本运行态主链通过

【本次会话未完成 / 未讨论完】
1. `UsersView.vue` 右侧角色说明区仍是卡片区，未做展示统一
2. `/screen` 大屏未纳入本轮展示统一
3. 部分辅助文档仍带旧口径，需要继续收口
4. 当前沙箱环境里，脚本内直接执行前端构建仍可能命中 `esbuild spawn EPERM`
   - 仓库里单独执行 `npm run build` 已通过
   - 在沙箱里重复跑脚本时可优先用 `-SkipStaticChecks`
5. worktree 内存在与本轮无关的其它变更
   - `.codex/`、`.claude/`、旧 `NEXT-SESSION-PROMPT*` 等路径有独立改动或删除
   - 后续不要误回滚或误提交这些无关变更

【下次必须从这里继续】
1. 优先判断是否继续做后台页展示统一
   - 首先评估 `UsersView.vue` 角色说明区是否改成表格
   - 再决定是否把 `/screen` 纳入同一展示规范
2. 继续收口仍带旧口径的辅助文档
3. 如需重复跑验收，可直接使用：
   - `./scripts/run-acceptance-smoke.ps1`
   - 若在沙箱环境中被前端构建阻塞，则用：`./scripts/run-acceptance-smoke.ps1 -SkipStaticChecks`

【本地环境注意】
- 联调端口统一使用 `8081`
- 前端默认 API：`http://localhost:8081`
- 若 `8081` 已被旧 Java 进程占用，先检查并清理再启动
- 若文档冲突，以 `.devflow/grain-platform-bootstrap/state.md` 与最新 handoff 为准
```
