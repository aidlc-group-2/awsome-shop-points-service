# 项目结构

DDD + 六边形架构，以 Maven 多模块项目组织。每一层拆分为 API（接口）和 impl（实现）子模块，以强制依赖倒置。

## 模块布局

```
├── common/                              # 公共工具（异常、错误码、DTO、注解）
├── domain/
│   ├── domain-model/                    # 领域实体（纯 POJO，无框架依赖）
│   ├── domain-api/                      # 领域服务接口
│   ├── domain-impl/                     # 领域服务实现（@Service）
│   ├── repository-api/                  # 仓储端口接口
│   ├── cache-api/                       # 缓存端口接口
│   ├── mq-api/                          # 消息队列端口接口
│   └── security-api/                    # 安全端口接口
├── infrastructure/
│   ├── repository/
│   │   └── mysql-impl/                  # MySQL 适配器（@Repository）— PO、Mapper、Impl
│   ├── cache/
│   │   └── redis-impl/                  # Redis 适配器
│   ├── mq/
│   │   └── sqs-impl/                    # SQS 适配器
│   └── security/
│       └── jwt-impl/                    # JWT 适配器
├── application/
│   ├── application-api/                 # 应用服务接口 + DTO + 请求对象
│   └── application-impl/               # 应用服务实现（@Service）
├── interface/
│   ├── interface-http/                  # REST 控制器（@RestController）、异常处理器、响应包装器（包名使用 facade）
│   └── interface-consumer/              # SQS 消息消费者（包名使用 facade）
└── bootstrap/                           # Spring Boot 启动入口、配置、application.yml、Flyway 迁移脚本
```

## 依赖规则（严格执行）
- `interface` → 仅依赖 `application-api`（禁止依赖 application-impl，禁止直接依赖 common）
- `application-impl` → 仅依赖 `domain-api`（禁止依赖 repository 或 infrastructure）
- `domain-impl` → 依赖端口接口（`repository-api`、`cache-api`、`mq-api`、`security-api`）
- `infrastructure/*-impl` → 实现端口接口
- `bootstrap` 聚合所有 impl 模块，用于 Spring DI 装配
- **禁止修改 pom.xml**：任何模块的 pom.xml 文件（依赖声明、模块结构、groupId、artifactId 等）未经确认不得修改。新增依赖或调整模块关系必须先说明理由并获得确认

## 包命名约定
基础包名：`com.awsome.shop.point`

| 层级 | 包命名规则 |
|---|---|
| 领域模型 | `domain.model.{aggregate}` |
| 领域服务接口 | `domain.service.{aggregate}` |
| 领域服务实现 | `domain.impl.service.{aggregate}` |
| 仓储端口 | `repository.{aggregate}` |
| 仓储 MySQL 实现 | `repository.mysql.impl.{aggregate}` |
| 持久化对象 | `repository.mysql.po.{aggregate}` |
| MyBatis Mapper | `repository.mysql.mapper.{aggregate}` |
| 应用服务接口 | `application.api.service.{aggregate}` |
| 应用层 DTO | `application.api.dto.{aggregate}` |
| 应用层请求 DTO | `application.api.dto.{aggregate}.request` |
| 应用服务实现 | `application.impl.service.{aggregate}` |
| HTTP 控制器 | `facade.http.controller` |
| HTTP 异常处理器 | `facade.http.exception` |
| HTTP 响应包装器 | `facade.http.response` |

## 各层关键模式

### 领域模型（`domain-model`）
- 纯 Java POJO，使用 `@Data`（Lombok），不使用 Spring 注解
- 实体类命名为 `{Name}Entity`
- 业务方法定义在实体上（如 `updateInfo()`）

### 领域服务（`domain-api` / `domain-impl`）
- 接口定义在 `domain-api`，实现在 `domain-impl` 中标注 `@Service`
- 通过 `@RequiredArgsConstructor` 进行构造器注入
- 仅依赖端口接口（repository-api、cache-api 等）
- 领域规则违反时抛出 `BusinessException`，使用 `ErrorCode` 枚举

### 仓储（`repository-api` / `mysql-impl`）
- 端口接口定义在 `domain/repository-api` — 返回领域实体
- MySQL 适配器在 `infrastructure/repository/mysql-impl` 中标注 `@Repository`
- 持久化对象（PO）命名为 `{Name}PO`，使用 `@TableName`、`@TableId(type = IdType.AUTO)`
- 每个 PO 的标准审计字段：`createdAt`、`updatedAt`、`createdBy`、`updatedBy`（自动填充）
- 通过 `@TableLogic` 的 `deleted` 字段实现软删除
- 通过 `@Version` 的 `version` 字段实现乐观锁
- Mapper 使用 MyBatis-Plus 的 `BaseMapper<PO>`，标注 `@Mapper`
- 在仓储实现中手动编写 `toEntity()` / `toPO()` 转换方法

### 应用服务（`application-api` / `application-impl`）
- 接口定义在 `application-api`，实现在 `application-impl` 中标注 `@Service`
- 请求 DTO 位于 `application.api.dto.{aggregate}.request` — 使用 Jakarta Validation（`@NotBlank`、`@Size` 等）
- 响应 DTO 命名为 `{Name}DTO`，使用 `@Data`
- 在服务实现中手动编写 `toDTO()` 转换方法
- 禁止直接依赖仓储 — 仅调用领域服务

### HTTP 控制器（`interface-http`）
- `@RestController` + `@RequestMapping("/api/v1")` + `@RequiredArgsConstructor`
- 所有端点均使用 POST（包括查询类的 get/list）
- URL 规则：`/api/v1/{scope}/{module}/{action}`，版本号之后固定三段
  - `{scope}`：`public`（经 API Gateway 对前端暴露）/ `private`（微服务间内部调用）
  - `{module}`：业务模块名（如 `point`、`test`）
  - `{action}`：具体操作（`get`、`list`、`create`、`update`、`delete`）
- 类上 `@RequestMapping("/api/v1")`，方法上 `@PostMapping("/{scope}/{module}/{action}")`
- 请求体使用 `@Valid` 校验
- 返回 `Result<T>` 包装器（来自 `common.result.Result`）
- Swagger 注解：类上使用 `@Tag`，方法上使用 `@Operation`

### 异常处理
- 异常层级：`BaseException` → `BusinessException` / `ParameterException` / `SystemException`
- 错误码实现 `ErrorCode` 接口，按领域分组到枚举中（如 `SampleErrorCode`）
- 错误码格式：`{CATEGORY}_{SEQ}`（如 `NOT_FOUND_001`、`AUTH_001`、`CONFLICT_001`）
- `GlobalExceptionHandler` 根据错误码前缀自动映射 HTTP 状态码

### 数据库迁移
- Flyway 脚本位于 `bootstrap/src/main/resources/db/migration/`
- 表使用 `utf8mb4` 字符集、`InnoDB` 引擎
- 每张表包含：`id`（BIGINT AUTO_INCREMENT）、`created_at`、`updated_at`、`created_by`、`updated_by`、`deleted`、`version`
