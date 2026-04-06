```
你现在在仓库 `D:\Users\Mobius\Desktop\mine\AAA-code\bs-test-01`，请继续这个毕业设计项目，并严格优先读取和遵守以下文档，然后再开始任何分析或开发。

必须先做的事情：
1. 读取 `zzz-docs/开发指导版PRD-粮仓环境数据预测管理平台.md`
2. 读取 `zzz-docs/设计文档/需求清单-任务书与开题报告对齐.md`
3. 读取 `.explore/grain-docs-alignment/handoffs/2026-04-06-001-resume-ready.md`
4. 读取 `.explore/grain-platform-bootstrap/handoffs/2026-04-06-004-backend-real-persistence-resume-ready.md`
5. 读取 `.explore/grain-docs-alignment/state.md` 和 `.explore/grain-platform-bootstrap/state.md`
6. 读取 `zzz-docs/设计文档/数据库设计定稿.md`
7. 读取 `zzz-docs/设计文档/后端接口与DTO-VO-Mapper设计.md`
8. 读取 `frontend-next/README.md`
9. 读取 `NEXT-SESSION-PROMPT.md` 自身，确认本轮恢复目标

当前已经完成的工作：
- 已完成任务书与开题报告的 Markdown 转换与对齐
- 已完成需求清单、来源映射、技术栈对齐
- 已完成“开发指导版 PRD”
- 已完成后端骨架、Vue 早期骨架、PowerShell 启动脚本
- 已完成 `frontend-next/` 的高保真静态原型增强，补齐了用户管理、环境数据图表、预测页、展示大屏和原型说明文档
- 已修复 `frontend-next/` 的 Next SWC 依赖问题，并重新通过 `npm run build` 验证
- 已完成数据库 SQL 定稿和表说明
- 已完成后端接口清单、DTO/VO、Mapper 设计
- 已将 `frontend/` 升级为正式版 Vue 前端骨架，接入 `Vue Router + Pinia + Element Plus + Axios + ECharts`
- 已补齐正式前端的登录、仪表盘、用户管理、仓库管理、环境数据、预测页和展示大屏入口
- 已在 `frontend/` 执行 `npm install` 和 `npm run build`，构建通过
- 已完成后端主链路从 `DemoDataService` 到真实 `MyBatis + MySQL` 的替换
- 已在真实数据库下打通登录、仓库、环境数据、预测归档四条后端主链路
- 已在 `8081` 端口完成最小联调验证

当前正式路线：
- 前端正式实现使用 `Vue 3 + Vite + Vue Router + Pinia + Element Plus + ECharts + Axios`
- 后端使用 `Spring Boot + MyBatis + MySQL`
- `frontend-next/` 只作为静态原型参考和答辩成品预期展示，不作为正式前端路线

当前本地环境注意事项：
- 本地数据库名：`grain_env_predict`
- 数据库用户名：`root`
- 数据库密码：空
- 当前后端验证端口：`8081`
- 如果继续联调，优先让前端请求指向 `http://localhost:8081`
- 当前 `8080` 在本机会话里可能被旧 Java 进程占用，不要误判为后端代码故障

最重要的约束：
- 不要再把范围扩展到机器人、数字孪生、LoRa/5G/GSM、区块链、复杂深度学习
- 后续开发必须严格以开发指导版 PRD 为准
- 优先完成完整业务流程，而不是堆高级功能
- 不要把 `frontend-next/` 误当成正式实现目标，它只负责帮助理解最终成品会长什么样

默认下一步任务：
1. 开始把 `frontend/` 当前兼容态页面逐步切到真实 API
2. 优先联调登录、仓库、环境数据、预测四个页面
3. 再补齐用户管理、角色选项、指标选项、预测历史等正式接口
4. 联调稳定后，再增强仪表盘聚合、预测历史展示和演示流程

如果用户没有改变方向，就从第 1 步开始推进。
```