你现在在仓库 `D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01`，请继续这个毕业设计项目，并严格优先读取和遵守以下文档，然后再开始任何分析或开发。

必须先做的事情：
1. 读取 `zzz-docs/开发指导版PRD-粮仓环境数据预测管理平台.md`
2. 读取 `zzz-docs/设计文档/需求清单-任务书与开题报告对齐.md`
3. 读取 `.explore/grain-docs-alignment/handoffs/2026-04-06-001-resume-ready.md`
4. 读取 `.explore/grain-platform-bootstrap/handoffs/2026-04-06-002-static-prototype-resume-ready.md`
5. 读取 `.explore/grain-docs-alignment/state.md` 和 `.explore/grain-platform-bootstrap/state.md`
6. 读取 `frontend-next/README.md`

当前已经完成的工作：
- 已完成任务书与开题报告的 Markdown 转换与对齐
- 已完成需求清单、来源映射、技术栈对齐
- 已完成“开发指导版 PRD”
- 已完成后端骨架、Vue 早期骨架、PowerShell 启动脚本
- 已完成 `frontend-next/` 的高保真静态原型增强，补齐了用户管理、环境数据图表、预测页、展示大屏和原型说明文档
- 已修复 `frontend-next/` 的 Next SWC 依赖问题，并重新通过 `npm run build` 验证

当前正式路线：
- 前端正式实现使用 `Vue 3 + Vite + Vue Router + Pinia + Element Plus + ECharts + Axios`
- 后端使用 `Spring Boot + MyBatis + MySQL`
- `frontend-next/` 只作为静态原型参考和答辩成品预期展示，不作为正式前端路线

最重要的约束：
- 不要再把范围扩展到机器人、数字孪生、LoRa/5G/GSM、区块链、复杂深度学习
- 后续开发必须严格以开发指导版 PRD 为准
- 优先完成完整业务流程，而不是堆高级功能
- 不要把 `frontend-next/` 误当成正式实现目标，它只负责帮助理解最终成品会长什么样

默认下一步任务：
1. 输出数据库 SQL 定稿和表说明
2. 输出后端接口清单、DTO/VO、Mapper 设计
3. 创建正式版 Vue 前端骨架，并参考 `frontend-next/` 的页面结构与信息架构

如果用户没有改变方向，就从第 1 步开始推进。
