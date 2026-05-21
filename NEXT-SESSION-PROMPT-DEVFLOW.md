```text
你现在在仓库根目录下的 `bs-test-01`。请继续毕业设计项目，并严格先走 devflow 恢复流程后再开始任何分析、验证或开发。

【协作硬约束（必读）】
- 必须始终使用简体中文。
- 实现类需求（多文件、前后端、新交互）默认须先输出：理解 + 方案 + 待确认，禁止同一条回复里直接大范围改代码。
- 只有我明确说「直接做」「不用讨论」等豁免语时，才可跳过对齐。
- 过程记录默认走 `devflow`，当前 mission 真相源在 `.devflow/grain-platform-bootstrap/`。
- 进入实现前先头脑风暴；完成代码修改后先询问是否需要提交，除非我已经明确要求提交。
- 如果提交，提交信息必须使用中文。
- 不需要做全局 ESLint；不影响运行的 TypeScript 报错可以先不处理，若要顺手修 TS 错误必须先确认。
- 仓库内提到路径时使用相对路径。

【必须先读取】
1. `.codex/skills/devflow/SKILL.md`
2. `zzz-docs/任务书.md`
3. `zzz-docs/开题报告.md`
4. `.devflow/grain-platform-bootstrap/state.md`
5. `.devflow/grain-platform-bootstrap/handoffs/index.md`
6. `.devflow/grain-platform-bootstrap/handoffs/2026-05-21-027-pause-ready-after-admin-role-assignment-guard.md`
7. 按需读取：
   - `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`
   - `.devflow/grain-platform-bootstrap/plans/2026-05-21-admin-role-assignment-guard.md`
   - `.devflow/grain-platform-bootstrap/bug-log.md`
   - `.devflow/grain-platform-bootstrap/checkpoints.md`
   - `.devflow/grain-platform-bootstrap/handoffs/2026-05-18-026-pause-ready-after-sensor-template-date-import-fix.md`
   - `zzz-docs/验证/数据库优先MVP-回归验证清单.md`
   - `zzz-docs/设计文档/项目整体架构与前后端初学者说明.md`
   - `zzz-docs/设计文档/数据库设计与表字段初学者说明.md`

【当前主线与口径】
- 本期仍是「数据库优先 MVP」。
- 不做「预测 -> 修正 -> 再预测」必做入口，修正相关字段可以保留为扩展。
- 预测归档继续使用：`prediction_task + prediction_result`。
- 温度主线走粮温模块；普通环境数据主要维护湿度和二氧化碳。
- 正式前端：`frontend/`；`frontend-next/` 仅静态原型参考。
- `.explore/grain-platform-bootstrap/` 仅历史快照，不作为当前真相源。
- 权限口径：`ADMIN` 是系统内置总管理员角色，不允许在用户管理里新增分配给普通账号；新增用户只允许选择仓库管理员和参观者。

【最近一次完成：系统管理员角色分配收口】
1. 问题现象：
   - 用户管理页点击“新增用户”时，角色下拉中可以选择“管理员（ADMIN）”。
   - 系统管理员 A 可以新增系统管理员 B。
   - 一旦存在多个启用管理员，原逻辑只保护“最后一个启用管理员”，B 可能删除或降权 A。
2. 问题原因：
   - `frontend/src/views/UsersView.vue` 的角色下拉直接展示后端返回的全部角色，没有区分“可分配业务角色”和“系统内置总管理员角色”。
   - `backend/src/main/java/com/grain/platform/service/UserService.java` 创建 / 编辑用户时只校验角色编码存在，没有禁止新增分配 `ADMIN`。
   - 删除用户时只做“至少保留一个启用管理员”保护，无法阻止历史误建的第二管理员删除其他管理员。
3. 已完成修复：
   - `frontend/src/views/UsersView.vue`
     - 新增 / 编辑普通用户时，角色下拉只展示 `WAREHOUSE_MANAGER` 与 `VIEWER`。
     - 编辑已有管理员时，保留展示 `ADMIN` 但禁用，避免误删管理员角色。
   - `backend/src/main/java/com/grain/platform/service/UserService.java`
     - 创建用户时禁止提交 `ADMIN`。
     - 编辑非管理员时禁止提权为 `ADMIN`。
     - 编辑已有管理员时禁止移除 `ADMIN`。
     - 删除用户时禁止删除带 `ADMIN` 角色的账号。
   - `backend/src/test/java/com/grain/platform/service/UserServiceTest.java`
     - 覆盖创建管理员、提权管理员、移除管理员角色、删除管理员四类接口绕过风险。
   - 已新增 / 更新 devflow 记录：
     - `.devflow/grain-platform-bootstrap/plans/2026-05-21-admin-role-assignment-guard.md`
     - `.devflow/grain-platform-bootstrap/bug-log.md`
     - `.devflow/grain-platform-bootstrap/state.md`
     - `.devflow/grain-platform-bootstrap/checkpoints.md`
     - `.devflow/grain-platform-bootstrap/handoffs/2026-05-21-027-pause-ready-after-admin-role-assignment-guard.md`
4. 已通过验证：
   - `backend/`：`mvn -q test -Dtest=UserServiceTest`
   - `backend/`：`mvn -q -DskipTests compile`
   - `frontend/`：`npm run build`

【本轮未完成 / 未讨论完 / 开放问题】
1. **系统管理员角色页面人工复测尚未做**
   - 单元测试、后端编译和前端构建已通过。
   - 尚未启动页面点击验证：“用户管理 -> 新增用户 -> 角色下拉只剩仓库管理员和参观者”。
   - 复测前先确认本地 `8081` 后端已重启到最新代码。
2. **后端运行态负向 smoke 可选**
   - 可直接调用新增 / 编辑用户接口提交 `ADMIN`，确认后端返回业务错误。
3. **历史误建管理员账号未自动清理**
   - 本轮阻止继续新增、提权和删除管理员账号。
   - 如果数据库中已经存在历史误建的第二管理员，本轮不会自动停用或降权；如需要，下次单独讨论处理策略。
4. **普通环境模板页面实测尚未做**
   - 后端解析单测与编译已通过。
   - 还没有做浏览器页面完整链路：“下载普通环境模板 -> Excel 打开/保存 -> 上传导入”。
   - 复测前先确认本地 `8081` 后端已重启到最新代码。
5. **本地 `8081` 后端可能还是旧进程**
   - 如果页面还看不到最新修复效果，先重启后端。
6. **demo 预测结果仍偏稀疏**
   - `schema.sql` 中 demo 任务的 `prediction_result` 仍是 `step 1 / 5 / 10`。
   - 还没有改成按 `forecastDays` 每天一条。
7. **demo 预测点时间仍不统一**
   - demo `prediction_result.result_time` 仍是 `00:00:00`。
   - 真实粮温汇总通常是 `08:40:00`。
8. **粮温导入 deadlock 真实复测**
   - 第二轮止血代码已完成。
   - 但“同仓库多 Excel 并发导入”真实场景还没最终确认彻底收口。
9. **验收脚本可选增强**
   - `run-acceptance-smoke.ps1` 还没专门断言 `keyword` 分页。
   - 还没专门断言 `pointNo` / `tempMin` / `tempMax` / `filter-options`。
10. **答辩文档可选增强**
   - 可补充“当前演示历史数据统一截止到 `2026-04-25`”说明。
   - 可继续整理论文截图、ER 图和预测页口径说明。

【下次从这里继续】
1. 若继续验证本次管理员角色修复：
   - 先重启本地 `8081` 后端。
   - 进入用户管理页。
   - 点击“新增用户”。
   - 确认角色下拉只剩“仓库管理员”和“参观者”，不再可选“管理员”。
   - 可选：用接口提交 `ADMIN` 做后端负向 smoke。
2. 若继续验证普通环境导入修复：
   - 先重启本地 `8081` 后端。
   - 进入数据页，切换到“普通环境数据”。
   - 下载普通环境模板。
   - 可用 Excel 打开/保存后上传，确认不再出现第 2 行 `collectedAt` 时间格式错误。
3. 若继续数据导入联调：
   - 先复测普通环境模板导入。
   - 再复测“同仓库多 Excel 并发导入”粮温 deadlock 场景。
4. 若继续优化预测页：
   - 先按 Mini Align 讨论是否把 demo 任务未来预测点改成每天一条。
   - 再讨论是否把 demo 预测点时间统一到 `08:40:00`。
5. 若继续写答辩或交接材料：
   - 优先复用两份初学者文档和里面的 PlantUML 图，再按目标场景裁剪。

【本地环境】
- 联调端口：`8081`
- 前端默认 API：`http://localhost:8081`
- 占端口时先清理旧 Java 进程
- 库重置：`scripts/reset-demo-db.ps1`
```
