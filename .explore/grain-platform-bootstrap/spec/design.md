# Design

## 总览

- 选定方案：前后端分离的 Web 平台，后端使用 Spring Boot 暴露 REST API，前端使用 Vue + ECharts 构建管理界面。
- 选择原因：
  - 与开题报告完全一致。
  - 开发与答辩成本低。
  - 易于后续扩展为真实数据库和设备接入。

## 结构

- 受影响页面或组件：
  - 登录页
  - 仪表盘页
  - 仓库管理页
  - 数据查询页
  - 数据预测页
- 新增模块或文件：
  - `backend/` Spring Boot 骨架
  - `frontend/` Vue 项目骨架
  - `scripts/` PowerShell 启动脚本
  - `README.md`
  - `backend/src/main/resources/db/schema.sql`
- 复用模块或文件：
  - 当前仅复用开题报告文档结论，不依赖现有业务代码

## 数据与状态流

- 输入：
  - 用户登录信息
  - 仓库基础信息
  - 温度、湿度等环境数据
  - 查询与预测参数
- 状态归属：
  - 前端保存当前菜单、筛选条件和图表数据
  - 后端管理业务计算与数据返回
- 副作用：
  - 登录后缓存用户信息
  - 新增环境数据后刷新图表与统计
  - 执行预测后生成预测点结果
- 输出渲染路径：
  - 后端 JSON -> 前端 API 层 -> 页面状态 -> ECharts 渲染

## 接口

- API 或服务：
  - `POST /api/auth/login`
  - `GET /api/dashboard/overview`
  - `GET /api/warehouses`
  - `POST /api/warehouses`
  - `GET /api/sensor-data`
  - `POST /api/sensor-data`
  - `POST /api/predictions/temperature`
- 类型或数据契约：
  - 用户：`id / username / role / displayName`
  - 仓库：`id / code / name / location / capacity / managerName / status`
  - 环境数据：`warehouseId / metricType / metricValue / collectedAt`
  - 预测点：`time / actualValue / predictedValue`

## 数据库设计

- 建议表：
  - `sys_user`
  - `sys_role`
  - `sys_user_role`
  - `warehouse`
  - `sensor_metric`
  - `sensor_data`
  - `prediction_record`
- 推荐角色：
  - `ADMIN`：系统管理员，管理用户和全部仓库
  - `WAREHOUSE_MANAGER`：仓库管理员，维护本仓库数据
  - `VIEWER`：查看者，只读访问

## 风险

- 实现风险：
  - Spring Boot 项目若无 Maven Wrapper，运行门槛会上升。
  - 前端若未安装依赖，脚本需先执行 `npm install`。
- 迁移或回归风险：
  - 当前骨架中若使用演示级假数据，后续切换数据库时需要替换 service 实现。
