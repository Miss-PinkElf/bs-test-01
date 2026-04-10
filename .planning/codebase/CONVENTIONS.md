# 编码约定

**分析日期：** 2026-04-10

## 命名模式

**Java 文件：**
- 控制器：`XxxController.java`，位于 `backend/src/main/java/com/grain/platform/controller/`。
- 服务：`XxxService.java`，位于 `backend/src/main/java/com/grain/platform/service/`。
- MyBatis 接口：`XxxMapper.java`，与 XML 同名，位于 `backend/src/main/java/com/grain/platform/mapper/`。
- 请求/响应 DTO：`XxxRequest.java`、`XxxResponse.java`、`XxxDto.java` 等，按领域分子包，如 `backend/src/main/java/com/grain/platform/dto/user/`。
- 实体：`SysUser`、`Warehouse` 等 POJO，位于 `backend/src/main/java/com/grain/platform/entity/`。
- 简单值对象：`backend/src/main/java/com/grain/platform/vo/common/IdVO.java` 等。

**Java 标识符：**
- 类名：`PascalCase`。
- 方法与字段：`camelCase`；数据库列 `snake_case`，由 MyBatis `map-underscore-to-camel-case` 映射到实体属性（见 `backend/src/main/resources/application.yml`）。

**前端文件：**
- 视图：`frontend/src/views/*View.vue`（如 `UsersView.vue`）。
- 组合式函数：`frontend/src/composables/useXxx.js`（导出命名函数，如 `useClientPagination`、`useIncrementalList`）。
- API 封装：`frontend/src/api/*.js`（如 `grain.js`、`http.js`）。
- Store：`frontend/src/stores/auth.js`。
- 工具：`frontend/src/utils/*.js`。

**前端标识符：**
- 组合式函数与工具函数：`camelCase`。
- Vue 组件在 SFC 中默认与文件名一致；模板中使用 Element Plus 组件标签（如 `el-table`）。

## 分层与职责

**推荐依赖方向（后端）：**
- `controller` → 仅调用 `service`，返回 `ApiResponse<T>`（`backend/src/main/java/com/grain/platform/common/ApiResponse.java`）。
- `service` → 调用 `mapper`、编排事务（`@Transactional`）、抛出 `IllegalArgumentException` 表示业务/校验错误（见 `backend/src/main/java/com/grain/platform/service/UserService.java`）。
- `mapper` → MyBatis 接口；SQL 在 `backend/src/main/resources/mapper/*.xml`。
- `dto` / `entity` → 入参多用 `record` + Bean Validation；持久化用带 setter 的实体类。

**控制器约定：**
- 类上使用 `@RestController`，根路径为 `@RequestMapping("/api")`，具体资源再拼接（如 `UserController` 中 `/users`）。
- 写操作请求体使用 `@Valid @RequestBody`。

**前端分层：**
- 页面：`frontend/src/views/` 中 `<script setup>`，通过 `frontend/src/api/` 发请求。
- 共享列表逻辑：放入 `frontend/src/composables/`，供多个视图复用。

**与主前端并列的目录：**
- `frontend-next/` 为 Next.js + Ant Design 的独立静态站点，技术栈与 `frontend/` 不同；主业务 Vue 前端以 `frontend/` 为准。

## 代码风格

**格式化：**
- 仓库根目录未检出 `.editorconfig`、Prettier、ESLint 配置文件；`frontend/package.json` 未配置 `lint` 脚本。
- Java 遵循 Spring Boot 父 POM 默认编译与编码约定；`backend/pom.xml` 指定 `java.version` 为 17。

**字符串与引号（前端）：**
- 现有 `frontend` 源码普遍使用双引号（如 `frontend/src/main.js`、`frontend/src/api/http.js`）。

## import 组织

**Java：**
- 顺序：`jakarta` / `org.springframework` / 第三方 / 本项目 `com.grain.platform`，组间空行（以 `UserController.java` 为参照）。

**Vue/JS：**
- 典型顺序：Element Plus 图标 → `vue` → 业务 API → composables → 工具（见 `frontend/src/views/UsersView.vue`）。

## 错误处理

**后端统一出口：**
- `backend/src/main/java/com/grain/platform/common/GlobalExceptionHandler.java`：
  - `IllegalArgumentException` → HTTP 语义由响应体表达：`ApiResponse.error(400, message)`。
  - `MethodArgumentNotValidException` → 取首个字段错误文案或默认「参数校验失败」。
  - 其他 `Exception` → `log.error` 后 `500`，消息为异常信息或「服务器异常」。

**业务层模式：**
- 可预期失败（如用户名重复）使用 `IllegalArgumentException`，避免在 controller 中散落校验逻辑。

**前端 HTTP：**
- `frontend/src/api/http.js` 中 axios 响应拦截器将失败统一转为 `Promise.reject(new Error(message))`。
- `unwrapPayload` 在 `code !== 200` 时 `throw new Error`，与后端 `ApiResponse` 约定一致。

## 日志

**框架：** SLF4J（`LoggerFactory.getLogger(Class)`）。

**模式：**
- 控制器在接口入口/成功路径打 `info`，包含关键业务键（如 `UserController` 中 `username`、`id`）。
- 未捕获异常在 `GlobalExceptionHandler` 中 `log.error("Unhandled exception", exception)`。

**配置：**
- 根日志级别在 `backend/src/main/resources/application.yml` 的 `logging.level.root: info`。

## 校验与配置

**请求校验：**
- DTO 使用 `jakarta.validation` 注解（如 `UserCreateRequest` 中 `@NotBlank`、`@NotEmpty`）。

**应用配置：**
- 主配置：`backend/src/main/resources/application.yml`（端口、数据源、MyBatis、logging）。生产环境勿将敏感信息提交仓库；本地凭证仅记存在性，不在文档中抄写具体值。

## 前端模式（Element Plus、组合式）

**全局注册：**
- `frontend/src/main.js`：`app.use(ElementPlus)`，并引入 `element-plus/dist/index.css`。
- 全局样式变量与布局：`frontend/src/styles.css`。

**按需使用组件 API：**
- 消息与确认框：`import { ElMessage, ElMessageBox } from "element-plus"`（见 `UsersView.vue`）。
- 图标：`import { Search } from "@element-plus/icons-vue"` 等在模板中使用。

**组合式函数约定：**
- 使用 `ref` / `computed` / `watch`，对返回对象使用 `proxyRefs` 包裹，便于在模板中自动解包（`useClientPagination.js`、`useIncrementalList.js`）。
- 接受数据源可为 `ref`、getter 函数或普通数组，内部通过 `normalizeSource` 统一（同上两个文件）。

**状态与路由：**
- 全局认证状态：`frontend/src/stores/auth.js`（Pinia）。
- 路由：`frontend/src/router/index.js`。

**构建：**
- `frontend/vite.config.js`：`@vitejs/plugin-vue`，开发服务器端口 `5173`、`host: "0.0.0.0"`。

## 注释

**Java：**
- 控制器方法上简短中文说明用途（`// 查询用户列表。` 等形式，见 `UserController.java`）。

**前端：**
- 以函数命名与结构表达意图为主；复杂 UI 逻辑可局部补充短注释（现有代码以自解释为主）。

## 函数与模块设计

**Java Service：**
- 类级私有常量表示魔法字符串（如 `UserService` 中 `ADMIN_ROLE_CODE`、`ACTIVE_STATUS`）。
- 写操作在方法上标注 `@Transactional`。

**前端 composable：**
- 导出单一工厂函数；选项对象 `options = {}` 带合理默认值（分页默认 `pageSizes`、`step` 等）。

## Barrel 文件

**未采用：** `frontend/src/api/` 等目录未发现统一 `index.js` 再导出；视图直接按路径引用模块。

---

*约定分析：2026-04-10*
