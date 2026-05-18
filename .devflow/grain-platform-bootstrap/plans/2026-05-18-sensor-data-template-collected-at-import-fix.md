# 普通环境数据模板时间导入修复计划

## 背景

用户在“环境数据”页切换到“普通环境数据”后，直接下载并导入普通环境数据模板，页面提示：

> 第 2 行字段 collectedAt 时间格式错误，应为 yyyy-MM-dd HH:mm:ss

## 问题判断

- 普通环境模板下载入口当前返回 CSV。
- 模板示例中的 `collectedAt` 原始文本是 `yyyy-MM-dd HH:mm:ss`。
- 用户用 Excel 打开或保存 CSV 后，时间文本可能被自动改写成 `2026/4/7 8:00`、`2026-4-7 8:00` 等常见格式。
- 后端 `SensorDataImportService` 当前只接受严格的 `yyyy-MM-dd HH:mm:ss`，导致“直接使用模板”也可能导入失败。
- 普通环境模板还包含 `temperature` 示例行，容易和当前“粮温主线”产生口径混淆。

## 修复方案

1. 后端导入解析兼容常见 Excel/CSV 时间格式：
   - `yyyy-MM-dd HH:mm:ss`
   - `yyyy-M-d H:mm:ss`
   - `yyyy-M-d H:mm`
   - `yyyy/MM/dd HH:mm:ss`
   - `yyyy/M/d H:mm:ss`
   - `yyyy/M/d H:mm`
2. 普通环境 CSV 模板示例只保留 `humidity` 与 `co2`。
3. 补充 `SensorDataImportService` 的导入解析单元测试，覆盖模板原始格式、Excel 常见改写格式和非法格式。
4. 更新 `bug-log.md` 与 `state.md`，记录问题现象、原因和解决方案。

## 验证

- [x] `backend/` 执行 `mvn -q test -Dtest=SensorDataImportServiceTest`
- [x] `backend/` 执行 `mvn -q -DskipTests compile`

## 执行结果

- 已将普通环境模板示例收口为 `humidity` 与 `co2`，不再在普通环境模板中放入 `temperature` 示例。
- 已兼容 Excel 打开 CSV 后常见的时间文本格式，例如 `2026/4/7 8:00` 与 `2026-4-7 8:00:00`。
- 已补充 `SensorDataImportServiceTest`，覆盖模板原始格式、Excel 常见改写格式和非法格式。
