# 粮仓环境数据预测管理平台

这是基于开题报告整理并初始化的毕业设计 MVP 项目骨架。

## 当前范围

- 后端：Spring Boot
- 前端：Vue 3 + Vite + ECharts
- 数据库：MySQL
- 预测：简单线性回归 / 移动平均思路
- 角色：管理员、仓库管理员、查看者

## 目录结构

```text
backend/   Spring Boot 后端
frontend/  Vue 前端
scripts/   Windows 启动脚本
.devflow/  任务记录、状态、计划与 handoff
```

## 推荐开发方式

### 方案 A：VS Code

适合同时开发前后端。建议安装这些扩展：

- Extension Pack for Java
- Spring Boot Extension Pack
- Vue - Official
- ESLint

### 方案 B：IntelliJ IDEA

- IDEA Ultimate：可以一个 IDE 同时开发前后端，体验最好。
- IDEA Community：更适合后端开发，前端也能写，但通常不如 VS Code 顺手。

### 实际建议

- 如果你想少切工具：直接用 VS Code 做全栈开发。
- 如果你更重视 Java 调试：用 IDEA 开后端，VS Code 开前端。

## 角色建议

- `ADMIN`：管理用户、仓库、全部数据
- `WAREHOUSE_MANAGER`：管理指定仓库和环境数据
- `VIEWER`：只读查看图表和预测结果

## 页面清单

- 登录页
- 仪表盘
- 仓库管理
- 环境数据查询
- 多指标独立预测

## 核心接口

- `POST /api/auth/login`
- `GET /api/dashboard/overview`
- `GET /api/warehouses`
- `POST /api/warehouses`
- `GET /api/sensor-data`
- `POST /api/sensor-data`
- `GET /api/metrics/options`
- `POST /api/predictions`

## 数据库表建议

- `sys_user`
- `sys_role`
- `sys_user_role`
- `warehouse`
- `sensor_metric`
- `sensor_data`
- `prediction_task`
- `prediction_result`

数据库定稿见：

- `backend/src/main/resources/db/schema.sql`
- `zzz-docs/设计文档/数据库设计定稿.md`

## 启动方式

Windows 下优先使用：

```powershell
npm run dev
```

这会在当前 VS Code 终端里同时启动前后端，并用日志前缀区分输出。
也可以单独启动：

```powershell
npm run frontend
npm run backend
```

如果你需要把本地数据库恢复到仓库内置的演示数据，请先执行：

```powershell
.\scripts\reset-demo-db.ps1
```

注意：

- 当前本机检测到了 `java`、`node`、`npm`
- 当前未检测到 `mvn`
- 前端默认地址改为 `http://localhost:5174`
- 如果你没有安装 Maven，可以用 IDEA 自带 Maven 运行后端，或者后续补 Maven Wrapper
- 后端默认启动现在不会自动重建演示库；只有手动执行 `reset-demo-db.ps1` 才会清库并回填演示数据

## 真相源

当前主开发 mission 真相源在：

- `.devflow/grain-platform-bootstrap/state.md`
- `.devflow/grain-platform-bootstrap/handoffs/2026-04-09-018-devflow-migration-ready.md`
- `.devflow/grain-platform-bootstrap/plans/active-plan-links.md`

当前文档 mission 真相源在：

- `.devflow/grain-docs-alignment/state.md`
- `.devflow/grain-docs-alignment/handoffs/2026-04-09-002-devflow-migration-ready.md`
- `.devflow/grain-docs-alignment/plans/active-plan-links.md`

