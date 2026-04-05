# 交接文档

## 当前目标
把桌宠项目按毕业设计节奏持续推进，并保留可写论文、可答辩、可续接的过程记录。当前下一阶段主线是：接入简单对话，完成前后端最小闭环。

## 当前进度
- 前端展示层已完成基础可视化：Live2D、边框、toolbar、拖拽、缩放、联动缩放均已具备。
- 前端专项调试记录保留在 `.claude/explore/desktop-pet-ui-debug`。
- 已建立本目录作为“毕设全过程记录”的全局工作区。
- OpenSpec 当前应将焦点转向 `Task 5` 与 `Task 6`。

## 关键文件/产物
- `zzz-doc/zzz-prompt-debug/origin/原始PRD.md`
- `zzz-doc/zzz-prompt-debug/origin/1.md`
- `zzz-doc/zzz-prompt-debug/plan/桌宠项目详细PRD.md`
- `openspec/changes/desktop-pet-companion-roadmap/*`
- `.claude/explore/desktop-pet-ui-debug/*`
- `.claude/explore/desktop-pet-graduation-roadmap/*`
- `zzz-doc/桌宠毕设过程记录指南.md`

## 已做的决策（摘要）
- 将 explore 体系拆为“全局毕设推进”与“局部技术调试”两层。
- 保留 `desktop-pet-ui-debug` 作为前端调试归档，不再让它承载整份毕设主线。
- 下一轮具体开发通过 superspec 推进“简单对话 + 后端最小闭环”。

## 立即要做的下一步
1. 更新 OpenSpec 的任务状态，回写前端阶段已完成内容。
2. 明确 `Task 5` 与 `Task 6` 的最小实现边界。
3. 用 superspec 为简单对话闭环生成或更新 proposal/design/tasks。
4. 每完成一轮实现后，同步本目录的 `state.md`、`decision-log.md` 与 `learnings.md`。

## 恢复指引
1. 新对话优先读取本目录的 `handoff.md` 和 `state.md`。
2. 如果要回顾前端交互调试细节，再读取 `.claude/explore/desktop-pet-ui-debug`。
3. 进入具体功能实现时，以 OpenSpec 的 `proposal.md / design.md / tasks.md` 为真相源。
