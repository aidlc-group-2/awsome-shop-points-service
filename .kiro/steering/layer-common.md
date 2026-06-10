---
inclusion: fileMatch
fileMatchPattern: "common/**"
---

# Common 层编码规则

本模块是全局共享基础设施，所有其他模块都可以依赖它。不要在此引入 Spring 框架依赖。

## 职责范围
- 异常体系定义
- 错误码接口与枚举
- 通用 DTO（如 `PageResult`）
- 通用注解

## 异常体系
- 基类 `BaseException` 包含 `errorCode`（String）和 `errorMessage`（String）
- 三个子类：`BusinessException`（业务异常）、`ParameterException`（参数异常）、`SystemException`（系统异常）
- 支持 `ErrorCode` 枚举 + `MessageFormat` 占位符（`{0}`, `{1}`）参数化消息

## 错误码规范
- 实现 `ErrorCode` 接口，提供 `getCode()` 和 `getMessage()`
- 按业务领域分组为独立枚举类（如 `SampleErrorCode`）
- 格式：`{CATEGORY}_{SEQ}`，类别前缀决定 HTTP 状态码映射：
  - `AUTH_` → 401, `AUTHZ_` → 403, `PARAM_` → 400
  - `NOT_FOUND_` → 404, `CONFLICT_` → 409, `LOCKED_` → 423
  - `BIZ_` → 200, `SYS_` → 500

## 编码约定
- 使用 Lombok `@Data`、`@Getter` 简化代码
- 类注释使用中文 Javadoc
- 不依赖 Spring 框架
- 不依赖任何业务模块
