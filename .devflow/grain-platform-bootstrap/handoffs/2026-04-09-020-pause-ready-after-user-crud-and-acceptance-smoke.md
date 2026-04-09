# Handoff

## 基础信息

- 创建时间：2026-04-09
- mission：grain-platform-bootstrap
- 当前阶段：Pause-ready after user CRUD and acceptance smoke script
- handoff 编号：020
- 是否 superseded：否

## 当前目标

- 保持“数据库优先 MVP”主线稳定，进入答辩前的稳定收口阶段。
- 当前实现主线已推进到：初始化策略收口 -> 仓库 CRUD -> 用户 CRUD -> PowerShell 验收脚本。
- 下一轮优先处理剩余的展示统一与辅助文档收口，而不是继续扩功能。

## 当前进度

- 已完成初始化策略收口：默认启动不再自动重建演示库，显式重置走 `scripts/reset-demo-db.ps1`。
- 已完成仓库管理 CRUD：后端 `PUT/DELETE` 与前端编辑/删除交互均已落地并验证。
- 已完成用户管理 CRUD：
  - 后端已补 `POST /api/users`、`PUT /api/users/{id}`、`PUT /api/users/{id}/password`、`DELETE /api/users/{id}`
  - 已补齐多角色维护、所属仓库维护、密码重置、最后一个启用管理员保护与引用删除拦截
  - 前端 `UsersView.vue` 已补齐新增 / 编辑 / 删除 / 重置密码弹窗交互
- 已完成 PowerShell 验收脚本：`scripts/run-acceptance-smoke.ps1`
  - 运行态脚本链路已通过：`./scripts/run-acceptance-smoke.ps1 -SkipStaticChecks`
  - 单独静态验证已通过：`backend/` 的 `mvn -q -DskipTests compile`、`frontend/` 的 `npm run build`
- 已修复 `scripts/start-backend.ps1`：
  - UTF-8 编码问题已处理
  - 当前 `powershell.exe` 与 `pwsh.exe` 都可启动后端
  - 优先复用用户目录 `.m2/repository`

## 本轮完成内容

- [x] 补用户 CRUD 设计文档：`.devflow/grain-platform-bootstrap/spec/2026-04-09-user-crud-design.md`
- [x] 补用户 CRUD 实施计划：`.devflow/grain-platform-bootstrap/plans/2026-04-09-user-crud.md`
- [x] 完成用户 CRUD 后端实现与前端交互
- [x] 获取用户 CRUD 的静态验证与运行态 smoke 证据
- [x] 补一键 PowerShell 验收脚本：`scripts/run-acceptance-smoke.ps1`
- [x] 更新回归验证清单：`zzz-docs/验证/数据库优先MVP-回归验证清单.md`
- [x] 回写 `devflow` 的 `state.md`、`checkpoints.md`、`session-tasks.md`、计划索引
- [x] 生成新的 pause-ready handoff 与新的 `NEXT-SESSION-PROMPT-DEVFLOW.md`

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 用户管理采用“资料 CRUD + 独立密码重置接口” | 在编辑接口中顺带改密码 | 资料修改与密码操作分离后语义更清晰，也更贴合后台操作习惯 |
| 用户角色直接按现有多角色结构实现 | 收缩成单角色 | 数据库已经是 `sys_user + sys_user_role` 结构，继续收缩会制造额外适配成本 |
| 验收脚本保留 `-SkipStaticChecks` | 强行要求每次都在脚本里跑完整静态检查 | 当前沙箱中脚本内前端构建偶发 `esbuild spawn EPERM`，保留该参数能保证运行态回归不被环境差异阻塞 |
| 旧行式 Excel 验证改为脚本内动态生成 `.xlsx` | 继续依赖仓库内 `zzz-docs/设计文档/1.6.xls` | 现有 `1.6.xls` 不是有效导入样例，动态生成可保证回归样本稳定可用 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `backend/src/main/java/com/grain/platform/controller/UserController.java` | 用户 CRUD 接口入口 | 最高 |
| `backend/src/main/java/com/grain/platform/service/UserService.java` | 用户 CRUD 业务规则、多角色与管理员保护 | 最高 |
| `backend/src/main/resources/mapper/UserMapper.xml` | 用户增删改查、引用检查与角色关联 SQL | 高 |
| `frontend/src/views/UsersView.vue` | 用户管理页新增/编辑/删除/重置密码交互 | 高 |
| `frontend/src/api/grain.js` | 用户 CRUD 前端 API 封装 | 高 |
| `scripts/run-acceptance-smoke.ps1` | 一键验收脚本主入口 | 最高 |
| `scripts/start-backend.ps1` | 后端启动脚本，已修编码与 Maven 仓库策略 | 高 |
| `.devflow/grain-platform-bootstrap/plans/2026-04-09-acceptance-smoke-script.md` | 验收脚本实施计划 | 中 |
| `.devflow/grain-platform-bootstrap/spec/2026-04-09-user-crud-design.md` | 用户 CRUD 设计记录 | 中 |
| `.devflow/grain-platform-bootstrap/state.md` | 当前主真相源 | 最高 |

## 风险 / 阻塞项 / 开放问题

- [ ] `UsersView.vue` 右侧角色说明卡片区仍未做展示统一。
- [ ] `/screen` 大屏仍未纳入本轮统一范围。
- [ ] 部分辅助文档仍带旧口径，需要继续收口。
- [ ] 在当前沙箱环境内，脚本里直接触发前端构建时仍可能命中 `esbuild spawn EPERM`；仓库内单独执行 `npm run build` 已通过，因此当前建议在沙箱里用 `-SkipStaticChecks` 跑脚本。
- [ ] 当前 worktree 里存在与本轮无关的其它变更：`.codex/`、`.claude/`、旧 `NEXT-SESSION-PROMPT*` 等路径有独立变更或删除，后续不要误回滚。

## 立即下一步

1. 若继续推进前端收口，优先评估是否把 `UsersView.vue` 的角色说明区改成表格，以及是否把 `/screen` 纳入同一展示规范。
2. 继续清理仍带旧口径的辅助文档，确保恢复入口、状态记录和说明文档完全一致。
3. 若准备暂停较久或切到新对话，可直接从新的 `NEXT-SESSION-PROMPT-DEVFLOW.md` 恢复，而不是再读旧的 `019` handoff。

## 恢复指引

1. 先读取根目录：`NEXT-SESSION-PROMPT-DEVFLOW.md`
2. 再读取：
   - `.codex/skills/devflow/SKILL.md`
   - `zzz-docs/任务书.md`
   - `zzz-docs/开题报告.md`
   - `.devflow/grain-platform-bootstrap/state.md`
   - `.devflow/grain-platform-bootstrap/handoffs/2026-04-09-020-pause-ready-after-user-crud-and-acceptance-smoke.md`
3. 按需读取：
   - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-04-09-user-crud.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-04-09-acceptance-smoke-script.md`
   - `zzz-docs/验证/数据库优先MVP-回归验证清单.md`
4. 从“立即下一步”的第 1 条继续，不要重复实现已完成的用户 CRUD 和验收脚本。

## 可从活跃上下文移除的内容

- 本轮为定位沙箱内 `npm run build` / `esbuild spawn EPERM` 差异所追加的中间排查输出。
- 本轮为定位旧 `1.6.xls` 样本无效而追加的单次导入报错日志。
