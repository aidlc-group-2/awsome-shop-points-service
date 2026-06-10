---
inclusion: fileMatch
fileMatchPattern: "interface/**"
---

# Interface 层编码规则

接口层是系统的入口，负责接收外部请求并委托给应用层处理。

## 子模块

### interface-http（HTTP 控制器）
- 使用 `@RestController` + `@RequestMapping` + `@RequiredArgsConstructor`
- 命名：`{Name}Controller`
- 包路径：`facade.http.controller`

#### URL 设计规范
- 格式：`/api/v1/{scope}/{module}/{action}`，版本号之后必须是三段
  - **第一段 `{scope}`**：访问范围
    - `public` — 经过 API Gateway 对前端提供的接口
    - `private` — 微服务之间内部调用的接口
  - **第二段 `{module}`**：业务模块名（如 `point`、`category`、`brand`）
  - **第三段 `{action}`**：具体操作
- 示例：
  - `POST /api/v1/public/point/get` — 前端查询单个商品
  - `POST /api/v1/public/point/list` — 前端分页查询商品
  - `POST /api/v1/public/point/create` — 前端创建商品
  - `POST /api/v1/private/point/get` — 其他微服务内部查询商品
- 常用 action：
  - `/get` — 查询单条
  - `/list` — 分页查询
  - `/create` — 创建
  - `/update` — 更新
  - `/delete` — 删除
- 所有端点均使用 `@PostMapping`（包括查询操作）
- Controller 类上 `@RequestMapping("/api/v1")`，方法上 `@PostMapping("/{scope}/{module}/{action}")`
- 请求体使用 `@RequestBody @Valid` 接收并校验
- 返回值统一使用 `Result<T>` 包装（`com.awsome.shop.point.common.result.Result`）
- Swagger 注解：类上 `@Tag(name = "...", description = "...")`，方法上 `@Operation(summary = "...")`

#### 统一响应格式
```json
{ "code": 0, "message": "操作成功", "data": {} }
```
- `code = 0` 表示成功，非 0 为错误码
- 使用 `Result.success()` / `Result.success(data)` / `Result.error(code, message)` 静态方法

#### 全局异常处理
- `GlobalExceptionHandler`（`@RestControllerAdvice`）统一捕获异常
- `BusinessException` → 根据错误码前缀自动映射 HTTP 状态码
- `ParameterException` → 400
- `MethodArgumentNotValidException` → 400（含字段级错误详情）
- `SystemException` / `Exception` → 500（不暴露内部细节）

### interface-consumer（消息消费者）
- 处理 SQS 消息，委托给应用层服务

## 禁止事项
- Controller 只调用 Application Service 接口，不直接调用 Domain Service 或 Repository
- interface 层不直接依赖 common，通过 application-api 传递获得
- 不在 Controller 中编写业务逻辑
- 不在 Controller 中进行数据转换（转换由 Application 层负责）
- 不直接返回领域实体，必须通过 DTO
