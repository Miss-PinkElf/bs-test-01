# 用户页角色区与答辩大屏展示统一（轻量计划）

## 目标

- `UsersView.vue` 右侧角色说明由多卡片改为 **单卡片 + `el-table`**，与后台其它列表页展示习惯一致。
- `/screen`（`BigScreenView.vue`）将「展示说明」「重点预警」由堆叠区块改为 **`el-table`**，并与全局 `panel-header` 结构对齐；大屏表格使用 `styles.css` 中 `.screen-data-table` 深色样式。
- 底部提示文案去掉「留待后续收口」类临时表述。

## 验证

- `frontend/` 执行 `npm run build` 通过。

## 状态

- 已在同一批次完成实现（2026-04-10）。
