# 数据库优先 MVP 回归验证清单

## 1. 目标

用于验证当前“数据库优先 MVP”最新主线是否保持可演示、可联调、可回归。

本清单覆盖：
- 首页仪表盘预警优先口径
- 固定 XLSX 粮温模板下载与导入
- 粮温汇总与预测归档主链
- 旧 CSV / 行式 Excel 兼容能力

## 2. 环境前提

- 后端联调端口：`8081`
- 前端默认 API：`http://localhost:8081`
- 若本机 `8080` 被旧 Java 进程占用，不作为当前故障判断依据
- 当前真相源：
  - `zzz-docs/任务书.md`
  - `zzz-docs/开题报告.md`
  - `.explore/grain-platform-bootstrap/state.md`
  - 最新 handoff

## 3. 静态验证

### 3.1 后端编译

在 `backend/` 执行：

```powershell
mvn -q -DskipTests compile
```

通过标准：命令退出码为 `0`。

### 3.2 前端构建

在 `frontend/` 执行：

```powershell
npm run build
```

通过标准：`vite build` 成功完成。

## 4. 首页预警口径验证

### 4.1 首页概览接口

请求：

```powershell
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:8081/api/dashboard/overview | Select-Object -ExpandProperty Content
```

通过标准：返回 JSON 中至少包含以下字段：
- `warehouseCount`
- `grainSummaryCount`
- `realAlertCount`
- `predictionAlertCount`
- `archivedPredictionCount`
- `latestAlerts`
- `latestGrainSummaries`
- `warehouseHealthList`

### 4.2 首页预警来源验证

通过标准：
- `latestAlerts` 中同时允许出现：
  - `sourceType = REAL`
  - `sourceType = PREDICTION`
- 后端首页统计已不再依赖旧的 `todayDataCount` / `alertCount` 口径

### 4.3 前端首页验证

页面：`/dashboard`

通过标准：
- 可见卡片：
  - 在线粮仓
  - 粮温汇总
  - 真实预警
  - 预测预警
  - 预测归档
- 可见面板：
  - 近期预警
  - 仓库运行健康度
  - 最新粮温汇总

## 5. 固定 XLSX 模板验证

### 5.1 模板下载

请求：

```powershell
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:8081/api/grain-temp/import/template -OutFile .\grain-temp-fixed-template.xlsx
```

通过标准：
- 文件名为 `grain-temp-fixed-template.xlsx`
- 可用 Excel/WPS 打开
- 模板结构包含：
  - `warehouseId`
  - `collectedAt`
  - 至少一个 `zoneCode + probeCode` 区块
  - “层号/点位”矩阵
  - “汇总分析（系统自动生成，可留空）”说明区

### 5.2 固定模板导入

请求示例：

```powershell
$Form = @{ file = Get-Item .\grain-temp-fixed-template.xlsx }
Invoke-RestMethod -Uri http://127.0.0.1:8081/api/grain-temp/import -Method Post -Form $Form
```

通过标准：
- 返回 `batchNo`
- 返回 `summaryGenerated = true`
- 返回 `warningLevel` / `warningMessage`
- 不要求手工填写汇总区也能成功导入

### 5.3 汇总结果验证

请求：

```powershell
Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:8081/api/grain-temp/summaries?warehouseId=1" | Select-Object -ExpandProperty Content
```

通过标准：
- 新导入时间点已生成 `grain_temp_summary`
- 返回值包含：
  - `avgTemp`
  - `maxTemp`
  - `minTemp`
  - `layer1Avg` ~ `layer4Avg`
  - `warningLevel`
  - `warningFlag`
  - `warningMessage`

## 6. 兼容性验证

### 6.1 旧 CSV 行式模板兼容

通过标准：
- 旧 CSV 导入仍可成功
- 不影响当前已跑通的数据库优先 MVP 主链

### 6.2 旧行式 Excel 兼容

通过标准：
- 若使用旧字段列式 Excel，系统仍可按原列顺序解析
- 不强制要求老师使用该格式，但保留兼容能力

## 7. 主链联动验证

### 7.1 预测页历史序列未回退

页面：`/prediction`

通过标准：
- 仍可读取 `grain_temp_summary` 历史序列
- 实际值 / 预测值双线图仍可渲染
- 预测归档接口未因首页和模板改造受损

### 7.2 首页导入后联动

通过标准：
- 完成固定模板导入后，首页 `/dashboard` 的：
  - `grainSummaryCount`
  - `latestGrainSummaries`
  - `latestAlerts`
  - `warehouseHealthList`
  能体现最新数据变化

## 8. 本轮已完成验证记录

- [x] `backend/`：`mvn -q -DskipTests compile`
- [x] `frontend/`：`npm run build`
- [ ] `/api/dashboard/overview` 运行态 smoke（当前被本地 MySQL 认证失败阻塞）
- [ ] `/api/grain-temp/import/template` 下载 smoke（当前被后端启动失败阻塞）
- [ ] 固定 XLSX 导入 smoke（当前被后端启动失败阻塞）
- [ ] 旧 CSV 兼容 smoke（当前被后端启动失败阻塞）

## 9. 下一步建议

1. 先按本清单补完运行态 smoke。
2. 若固定模板会长期作为老师演示样例，再补一份真实样例文件到仓库内。
3. 若后续频繁回归，可再补一个 PowerShell 脚本串起“启动后端 + 下载模板 + 导入 + 查询首页概览”。

## 10. 当前阻塞记录

- 时间：2026-04-08
- 现象：`mvn -q spring-boot:run -Dspring-boot.run.arguments=--server.port=8081` 启动失败，未进入接口 smoke 阶段。
- 已定位原因：Spring Boot 在执行数据库初始化脚本时获取 MySQL 连接失败。
- 关键报错：`Access denied for user ''root''@''localhost'' (using password: YES)`
- 结论：当前固定 XLSX 模板代码已通过静态编译与前端构建，但运行态 smoke 受本机 MySQL 认证问题阻塞；该阻塞不是本轮模板解析逻辑直接导致。
