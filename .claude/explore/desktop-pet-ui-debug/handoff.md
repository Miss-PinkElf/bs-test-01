# 交接文档

## 当前目标
本目录目标已完成：保留桌宠前端第二轮视觉联动优化的专项调试记录，供后续复盘和论文撰写引用。

## 当前进度
- 已完成 toolbar、悬浮边框、顶部拖拽把手、右下角缩放手柄。
- 已修复拖拽/缩放后 PIXI / Live2D 生命周期报错。
- 已补充真实窗口 `resize` -> bounds 同步链路，并将模型可视区、toolbar 和外框收敛到统一缩放基准。
- 用户已确认联动缩放效果通过验证。
- 已生成正式 handoff：`.claude/handoffs/2026-03-24-223232-desktop-pet-边框联动缩放续接.md`

## 关键文件/产物
- `frontend/src/App.tsx`
- `frontend/src/App.module.scss`
- `frontend/src/components/Live2DWidget/Live2DCanvas.tsx`
- `frontend/src/components/Live2DWidget/useLive2DModel.ts`
- `frontend/src/main.ts`
- `zzz-doc/桌宠前端开发问题修复清单.md`
- `zzz-doc/zzz-prompt-debug/plan/桌宠项目详细PRD.md`
- `openspec/changes/desktop-pet-companion-roadmap/design.md`
- `openspec/changes/desktop-pet-companion-roadmap/tasks.md`
- `.claude/handoffs/2026-03-24-223232-desktop-pet-边框联动缩放续接.md`

## 已做的决策（摘要）
- `PIXI.Application` 在 resize 场景必须保持单例，只允许 `renderer.resize`。
- 模型适配必须基于原始尺寸计算，不能叠加已缩放尺寸。
- 下一轮重点从“修报错”切换到“边框、模型、toolbar 联动缩放 + 限制范围 + 文档完善”。

## 立即要做的下一步
1. 将毕设主线切换到 `.claude/explore/desktop-pet-graduation-roadmap/`
2. 更新 OpenSpec 当前任务状态
3. 用 superspec 推进“前端简单对话入口 + Python 后端最小闭环”

## 恢复指引
1. 如果需要回顾前端边框联动缩放问题，读取本目录文件即可。
2. 如果要继续推进毕业设计主线，优先读取 `.claude/explore/desktop-pet-graduation-roadmap/`。
3. 具体功能开发仍以 OpenSpec change workspace 为真相源。
