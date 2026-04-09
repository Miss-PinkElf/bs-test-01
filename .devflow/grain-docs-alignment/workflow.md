# 任务工作流

## 任务目标
- 对齐 `zzz-docs` 中任务书与开题报告的需求。
- 将两份 `.doc` 转换为 `md`。
- 输出一份“系统需求清单 + 来源映射”文档，说明系统可以做到什么，以及这些要求分别来自任务书还是开题报告。

## 范围边界
- 范围内：
  - 文档转换
  - 文档阅读与对齐分析
  - 需求清单整理
  - 技术栈与前后端职责梳理
- 范围外：
  - 代码实现
  - 页面改造
  - 后端接口开发

## 成功标准
- `zzz-docs` 下存在 `开题报告.md` 和 `2.任务书.md`。
- 需求清单文档清晰列出系统功能、技术栈、前后端职责。
- 每项需求都能标明来自任务书、开题报告，或两者共同要求。

## 阶段规划
1. Classify / 选择路径（文档对齐 mission）
2. Align（核对任务书、开题报告与需求清单）
3. Plan（记录文档入口与后续拆分方向）
4. Apply（补充文档、图示或来源映射）
5. Verify（确认文档真相源可直接指导下一阶段）
6. Handoff / Resume（跨对话恢复）

## 当前阶段
- Resume-ready after devflow migration

## 本轮补充进展
- 已完成过程记录迁移：
  - `.explore/grain-docs-alignment/` 已完整复制到 `.devflow/grain-docs-alignment/`
  - 新增 `.devflow/grain-docs-alignment/plans/active-plan-links.md` 作为文档入口索引
  - 原 `.explore/` 工作区保持不变

## 退出条件
- 用户可以直接从 `.devflow/grain-docs-alignment/` 读取状态、handoff 与文档入口。
