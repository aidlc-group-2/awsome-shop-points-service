---
inclusion: fileMatch
fileMatchPattern: "domain/**"
---

# Domain 层编码规则

领域层是业务核心，包含领域模型、领域服务接口/实现、以及各类 Port 接口。

## 子模块职责

### domain-model
- 纯 Java POJO，不依赖 Spring 或任何框架注解
- 实体命名：`{Name}Entity`，使用 `@Data`
- 业务行为方法直接定义在实体上（充血模型），如 `updateInfo()`
- 包路径：`domain.model.{aggregate}`

### domain-api
- 领域服务接口，定义业务操作契约
- 命名：`{Name}DomainService`
- 包路径：`domain.service.{aggregate}`
- 方法参数使用基本类型或领域实体，不使用 DTO

### domain-impl
- 领域服务实现，使用 `@Service` + `@RequiredArgsConstructor`
- 命名：`{Name}DomainServiceImpl`
- 包路径：`domain.impl.service.{aggregate}`
- 只依赖 Port 接口（repository-api、cache-api、mq-api、security-api），绝不直接依赖基础设施实现
- 业务校验失败时抛出 `BusinessException(ErrorCode)`

### Port 接口（repository-api / cache-api / mq-api / security-api）
- 定义基础设施访问契约，由 infrastructure 层实现
- Repository 接口返回领域实体（`{Name}Entity`），不暴露持久化细节
- 命名：`{Name}Repository`、`{Name}Cache`、`{Name}MessageProducer` 等
- 包路径：`repository.{aggregate}`、`cache.{aggregate}` 等

## 禁止事项
- domain-model 中不允许出现 Spring 注解
- domain-api/domain-impl 不允许直接依赖 infrastructure 实现类
- 不允许在领域层处理 HTTP 请求/响应相关逻辑
- 不允许在领域层引用 DTO 或 Request 对象
