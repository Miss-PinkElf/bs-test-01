# Handoff

## 基础信息

- 创建时间：2026-05-21
- mission：grain-platform-bootstrap
- 当前阶段：Pause-ready after 系统管理员角色分配收口
- handoff 编号：027
- 是否 superseded：否

## 当前目标

- 修复“系统管理员可以新增另一个系统管理员，进而在多管理员场景下互相删除”的权限口径问题，并在休息前把 devflow 过程记录、恢复提示词和提交状态收口。

## 当前进度

- 已完成本轮 Mini Align、轻量计划、实现、定向单元测试、后端编译、前端构建和 devflow 记录。
- 当前真相源继续为 `.devflow/grain-platform-bootstrap/`。
- `.explore/grain-platform-bootstrap/` 仍仅作为历史快照，不作为当前真相源。

## 本轮完成内容

- [x] 按根目录 `NEXT-SESSION-PROMPT-DEVFLOW.md` 恢复上下文。
- [x] 使用 `devflow` + 头脑风暴对齐本轮方案。
- [x] 新增轻量计划：`.devflow/grain-platform-bootstrap/plans/2026-05-21-admin-role-assignment-guard.md`。
- [x] 修改 `frontend/src/views/UsersView.vue`：
  - 新增 / 编辑普通用户时，角色下拉只展示 `WAREHOUSE_MANAGER` 与 `VIEWER`。
  - 编辑已有管理员时，保留展示 `ADMIN` 但禁用，避免误删管理员角色。
- [x] 修改 `backend/src/main/java/com/grain/platform/service/UserService.java`：
  - 创建用户时禁止提交 `ADMIN`。
  - 编辑非管理员时禁止提权为 `ADMIN`。
  - 编辑已有管理员时禁止移除 `ADMIN`。
  - 删除用户时禁止删除带 `ADMIN` 角色的账号。
- [x] 新增 `backend/src/test/java/com/grain/platform/service/UserServiceTest.java`：
  - 覆盖创建管理员、提权管理员、移除管理员角色、删除管理员四类风险。
- [x] 更新 devflow 记录：
  - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
  - `.devflow/grain-platform-bootstrap/bug-log.md`
  - `.devflow/grain-platform-bootstrap/state.md`
  - `.devflow/grain-platform-bootstrap/checkpoints.md`

## 验证结果

- `backend/`：`mvn -q test -Dtest=UserServiceTest` 通过。
- `backend/`：`mvn -q -DskipTests compile` 通过。
- `frontend/`：`npm run build` 通过。

## 关键决策与原因

| 决策 | 备选方案 | 原因 |
| --- | --- | --- |
| 前后端一起收口 `ADMIN` 分配 | 只删除前端下拉里的管理员选项 | 只改前端可以被接口绕过，权限边界必须由后端兜底 |
| 已有管理员可保留但不能移除 `ADMIN` | 允许编辑管理员角色 | 避免误操作把总管理员降权，同时保持现有 `admin` 账号稳定 |
| 禁止删除管理员账号 | 只保留“最后一个启用管理员不能删除” | 多管理员历史数据下，仍可能出现 B 删除 A；禁止删除管理员更符合老师的“总管理员”口径 |
| 本轮不改角色表和种子数据 | 删除数据库中的 `ADMIN` 角色选项 | `ADMIN` 仍是系统内置权限角色，应该保留给初始总管理员和权限判断使用 |

## 关键文件 / 产物

| 文件 | 作用 | 相关性 |
| --- | --- | --- |
| `frontend/src/views/UsersView.vue` | 用户管理弹窗角色选项展示与禁用逻辑 | 本轮前端核心修复 |
| `backend/src/main/java/com/grain/platform/service/UserService.java` | 用户创建 / 编辑 / 删除的角色边界守卫 | 本轮后端核心修复 |
| `backend/src/test/java/com/grain/platform/service/UserServiceTest.java` | 权限绕过风险单元测试 | 本轮新增验证 |
| `.devflow/grain-platform-bootstrap/plans/2026-05-21-admin-role-assignment-guard.md` | 本轮轻量计划与执行结果 | 恢复与追溯入口 |
| `.devflow/grain-platform-bootstrap/bug-log.md` | 问题现象、问题原因、解决方案记录 | 问题清单 |
| `.devflow/grain-platform-bootstrap/state.md` | 当前 mission 真相源状态 | 已同步本轮完成情况 |
| `NEXT-SESSION-PROMPT-DEVFLOW.md` | 下次可复制恢复提示词 | 本次收尾更新 |

## 风险 / 阻塞项 / 开放问题

- [ ] 本轮已完成单元测试和构建验证，但尚未启动页面做人工点击复测。
- [ ] 若数据库里已经存在历史误建的第二管理员，本轮代码会阻止继续删除管理员，但不会自动清理历史账号；如需要，可下次单独讨论是否停用或降权历史误建账号。
- [ ] 普通环境导入模板修复仍未做浏览器页面完整链路复测：“下载模板 -> Excel 打开/保存 -> 上传导入”。
- [ ] 本地 `8081` 后端可能仍是旧进程；页面复测前先重启后端。
- [ ] demo 预测结果仍偏稀疏：`schema.sql` 中 demo 任务的 `prediction_result` 仍是 `step 1 / 5 / 10`。
- [ ] demo 预测点时间仍不统一：`prediction_result.result_time` 仍是 `00:00:00`，真实粮温汇总通常是 `08:40:00`。
- [ ] 粮温导入 deadlock 第二轮止血代码已完成，但“同仓库多 Excel 并发导入”真实场景仍未最终复测确认。
- [ ] 验收脚本可选增强仍未做：`keyword` 分页断言、`pointNo` / `tempMin` / `tempMax` / `filter-options` 断言。
- [ ] 答辩文档可选增强仍未做：可补“当前演示历史数据统一截止到 `2026-04-25`”、截图、ER 图和预测页口径说明。

## 立即下一步

1. 若要确认这次管理员角色修复的页面效果：重启本地 `8081` 后端，进入用户管理页，点击“新增用户”，确认角色下拉只剩“仓库管理员”和“参观者”。
2. 若要做后端负向验证：直接调用新增 / 编辑用户接口提交 `ADMIN`，确认后端拒绝。
3. 若继续验证普通环境导入：先重启本地 `8081` 后端，再在“普通环境数据”模式下载模板并上传验证。
4. 若继续数据导入联调：复测粮温“同仓库多 Excel 并发导入” deadlock 场景。
5. 若继续优化预测页：先按 Mini Align 讨论是否把 demo 预测点改为每天一条、是否统一到 `08:40:00`。

## 恢复指引

1. 先读取 `.codex/skills/devflow/SKILL.md`。
2. 再读取 `zzz-docs/任务书.md` 与 `zzz-docs/开题报告.md`。
3. 读取 `.devflow/grain-platform-bootstrap/state.md`。
4. 读取 `.devflow/grain-platform-bootstrap/handoffs/index.md`。
5. 读取本 handoff：`.devflow/grain-platform-bootstrap/handoffs/2026-05-21-027-pause-ready-after-admin-role-assignment-guard.md`。
6. 按需读取：
   - `.devflow/grain-platform-bootstrap/plans/2026-05-21-admin-role-assignment-guard.md`
   - `.devflow/grain-platform-bootstrap/bug-log.md`
   - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
   - `.devflow/grain-platform-bootstrap/handoffs/2026-05-18-026-pause-ready-after-sensor-template-date-import-fix.md`

## 可从活跃上下文移除的内容

- 本轮对用户管理角色下拉的初步检索输出。
- 后端 `UserService` 守卫方案的中间推导。
- Maven 本地仓库权限短暂阻塞的中间输出；当前重跑已通过。
