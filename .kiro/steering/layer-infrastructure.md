---
inclusion: fileMatch
fileMatchPattern: "infrastructure/**"
---

# Infrastructure 层编码规则

基础设施层实现领域层定义的 Port 接口，是技术细节的适配器。

## 子模块

### mysql-impl（仓储适配器）
- 实现 `domain/repository-api` 中的 Repository 接口
- 使用 `@Repository` + `@RequiredArgsConstructor`
- 命名：`{Name}RepositoryImpl`
- 包路径：`repository.mysql.impl.{aggregate}`

#### PO（持久化对象）
- 命名：`{Name}PO`，使用 `@Data` + `@TableName("{table}")`
- 包路径：`repository.mysql.po.{aggregate}`
- 主键：`@TableId(type = IdType.AUTO)`
- 必须包含标准审计字段：
  - `createdAt`（`@TableField(fill = FieldFill.INSERT)`）
  - `updatedAt`（`@TableField(fill = FieldFill.INSERT_UPDATE)`）
  - `createdBy`（`@TableField(fill = FieldFill.INSERT)`）
  - `updatedBy`（`@TableField(fill = FieldFill.INSERT_UPDATE)`）
- 软删除：`@TableLogic` 标注 `deleted` 字段
- 乐观锁：`@Version` 标注 `version` 字段

#### Mapper
- 继承 `BaseMapper<{Name}PO>`，使用 `@Mapper`
- 命名：`{Name}Mapper`
- 包路径：`repository.mysql.mapper.{aggregate}`
- XML 映射文件放在 `resources/mapper/{aggregate}/` 下

#### SQL 编写规范
- **允许直接使用 MyBatis-Plus 通用 API 的操作**：`insert`、`updateById`、`deleteById`、`selectById`
- **其他所有查询（分页、条件查询、关联查询等）必须在 Mapper XML 中编写 SQL**
- **禁止使用注解方式编写 SQL**（如 `@Select`、`@Update`、`@Insert`、`@Delete`）
- **禁止使用 Lambda / Wrapper 构建查询条件**（如 `QueryWrapper`、`LambdaQueryWrapper`、`Wrappers.lambdaQuery()` 等）

#### 数据转换
- 在 RepositoryImpl 中手动编写 `toEntity()` 和 `toPO()` 私有方法
- PO ↔ Entity 转换不使用 MapStruct 等框架

### redis-impl（缓存适配器）
- 实现 `domain/cache-api` 中的缓存接口

### sqs-impl（消息队列适配器）
- 实现 `domain/mq-api` 中的消息接口

### jwt-impl（安全适配器）
- 实现 `domain/security-api` 中的安全接口

## 禁止事项
- 不允许在基础设施层编写业务逻辑
- 不允许直接暴露 PO 到上层，必须转换为领域实体
- 不允许依赖 application 层或 interface 层
