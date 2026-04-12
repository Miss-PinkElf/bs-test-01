---
status: partial
phase: 13-zzz-prompt-debug-origin
source: [13-VERIFICATION.md]
started: 2026-04-12T00:00:00Z
updated: 2026-04-12T00:00:00Z
---

## Current Test

等待按账号执行人工验收。

## Tests

### 1. admin / 123456
expected: 默认落点 `/dashboard`；可见菜单包含 仪表盘 / 用户管理 / 仓库管理 / 环境数据 / 温度预测；用户管理、仓库管理、环境数据写操作、预测执行与删除均可正常使用；越权场景不存在。
result: pending

### 2. manager_a01 / 123456
expected: 默认落点 `/environment`；菜单不出现 用户管理 / 仓库管理；环境数据页与预测页仓库选择固定为所属仓库；允许对所属仓库新增、编辑、删除、导入、执行预测；不能查看或修改其它仓库数据；直接输入其它后台管理 URL 会被重定向到可访问页面。
result: pending

### 3. viewer_demo / 123456
expected: 默认落点 `/dashboard`；可见菜单仅包含 仪表盘 / 环境数据 / 温度预测；环境数据页和预测页保留查询、图表、任务摘要与历史记录浏览能力；看不到 新增 / 手工录入 / 导入 / 下载模板 / 编辑 / 删除 / 执行预测 / 批量删除 等入口；手动输入无权限 URL 不应停在空白或死循环页面。
result: pending

### 4. manager_a01 越仓读写
expected: 在环境数据页与预测页尝试切换到其它仓库或直接请求其它仓库资源时，应被前端锁仓或收到后端 `403`；不能越仓查看、删除或执行预测。
result: pending

### 5. viewer_demo 写操作越权
expected: viewer 账号即使手动构造写操作请求，也应收到明确的 `403` 语义，而不是 `400` 或静默成功。
result: pending

## Summary

total: 5
passed: 0
issues: 0
pending: 5
skipped: 0
blocked: 0

## Gaps

- 暂无；等待人工执行上述验收脚本后填写。
