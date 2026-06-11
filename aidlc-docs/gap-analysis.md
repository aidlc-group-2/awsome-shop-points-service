# Unit 4 积分服务 — Gap 分析文档

> 对比来源：`awsome-shop-plan/aidlc-docs/inception/` 设计文档 vs `awsome-shop-points-service` 现有代码
> 分析时间：2026-06-10
> 分析人：AI-DLC

---

## 1. 设计要求总览（来自 Plan）

### 1.1 覆盖的 User Stories

| 故事 | 标题 | 优先级 | 关联需求 |
|------|------|--------|----------|
| US-15 | 查看积分余额 | Must | FR-P1 |
| US-16 | 查看积分变动记录 | Must | FR-P2 |
| US-17 | 积分到期提示 | Should | FR-P7, BR-3 |
| US-22 | 配置积分规则 | Must | FR-P3, FR-P4, FR-P5, FR-P7 |
| US-23 | 手动调整用户积分 | Must | FR-P6 |
| US-24 | 查看用户积分变动记录 | Should | FR-P2 |

### 1.2 跨服务接口（被其他服务依赖）

| 调用方 | 接口 | 用途 | 关联 |
|--------|------|------|------|
| Auth Service | `POST /api/v1/private/point/grant` | 注册成功后发放入职奖励 | FR-P3, US-01 |
| Order Service | `POST /api/v1/private/point/deduct` | 兑换时扣减积分 | FR-P8, US-08 |
| Order Service | `POST /api/v1/private/point/refund` | 取消时退回积分 | FR-P8, US-14 |

### 1.3 设计要求的组件清单

| 组件 | 类型 | 职责 |
|------|------|------|
| PointsController | Controller | 员工余额/变动；管理员规则/调整/查用户记录 |
| InternalPointsController | Controller | 内部接口：grant / deduct / refund |
| PointsAccountService | Service | 余额管理（按批次 FIFO） |
| PointsGrantService | Service | 发放/扣减/退回/手动调整 |
| PointsTransactionService | Service | 变动记录写入与查询 |
| PointsRuleService | Service | 规则配置管理 |
| PointsExpiryScheduler | 定时任务 | 周期性发放 + 积分过期处理 |
| PointsAccountRepository | Repository | 账户余额持久化 |
| PointsBatchRepository | Repository | 积分批次（FIFO）持久化 |
| PointsTransactionRepository | Repository | 变动流水持久化 |
| PointsRuleRepository | Repository | 规则配置持久化 |

### 1.4 数据模型要求

| 实体 | 关键字段 | 说明 |
|------|----------|------|
| PointsAccount | userId, balance | 用户积分账户 |
| PointsBatch | userId, amount, remaining, grantType, expireAt, createdAt | 每次发放为一个批次，FIFO 消耗 |
| PointsTransaction | userId, type(GRANT/DEDUCT/REFUND/EXPIRE/ADJUST), amount, orderRef, reason, operatorId, createdAt | 变动流水 |
| PointsRule | onboardingBonus, periodicAmount, periodicCycle, validityDays | 规则配置（全局单条或多条） |

### 1.5 业务规则

| 编号 | 规则 |
|------|------|
| BR-1 | 入职奖励：注册后一次性发放，额度可配置 |
| BR-2 | 周期性发放：按配置周期/额度自动发放 |
| BR-3 | FIFO 消耗：扣减时优先消耗最早到期的批次 |
| BR-4 | 余额不足拒绝兑换 |
| NFR-4 | 积分变动需保证一致性（并发控制） |

---

## 2. 现有代码状态

### 2.1 已具备（脚手架）

| 项目 | 状态 | 说明 |
|------|------|------|
| DDD + 六边形架构 | ✅ 完整 | 26 个子模块，分层清晰 |
| Maven 多模块结构 | ✅ 完整 | common/domain/infrastructure/application/interface/bootstrap |
| Spring Boot 3.4.1 + Java 21 | ✅ 配置好 | pom.xml 已定义 |
| MyBatis-Plus 3.5.7 | ✅ 配置好 | application.yml 中已配置 |
| Flyway 数据库迁移 | ✅ 配置好 | baseline-on-migrate: true |
| Redis 适配器模块 | ✅ 模块存在 | infrastructure/cache/redis-impl |
| SQS 适配器模块 | ✅ 模块存在 | infrastructure/mq/sqs-impl |
| JWT 安全模块 | ✅ 模块存在 | infrastructure/security/jwt-impl |
| 全局异常处理 | ✅ 已实现 | BusinessException / GlobalExceptionHandler |
| 统一 Result 响应 | ✅ 已实现 | Result.success() / Result.failure() |
| PageResult 分页 | ✅ 已实现 | common 模块中 |
| UserContext (ThreadLocal) | ✅ 已实现 | 审计字段自动填充用 |
| Swagger/SpringDoc | ✅ 配置好 | 端口 8003 |
| Test CRUD 示例 | ✅ 贯穿全层 | 完整示例：Controller → Application → Domain → Repository |

### 2.2 Test 示例提供的参考价值

Test 示例展示了项目约定的编码规范：
- URL 格式：`/api/v1/{scope}/{module}/{action}`
- 所有接口用 POST
- 请求 DTO 带 Jakarta Validation
- Repository 实现 PO ↔ Entity 转换
- Flyway 迁移脚本格式

---

## 3. Gap 清单（需要新增/实现的）

### 3.1 数据库层

| # | Gap | 说明 | 对应需求 | 优先级 |
|---|-----|------|----------|--------|
| DB-1 | points_account 表 | 用户积分账户（userId, balance） | FR-P1 | Must |
| DB-2 | points_batch 表 | 积分批次（userId, amount, remaining, grantType, expireAt, status） | BR-3, FR-P7 | Must |
| DB-3 | points_transaction 表 | 变动流水（userId, type, amount, orderRef, reason, operatorId） | FR-P2 | Must |
| DB-4 | points_rule 表 | 规则配置（onboardingBonus, periodicAmount, periodicCycle, validityDays） | FR-P5 | Must |

### 3.2 领域模型层（domain-model）

| # | Gap | 说明 | 对应需求 |
|---|-----|------|----------|
| DM-1 | PointsAccountEntity | 账户实体 | FR-P1 |
| DM-2 | PointsBatchEntity | 批次实体（含 FIFO 消耗逻辑方法） | BR-3 |
| DM-3 | PointsTransactionEntity | 流水实体 | FR-P2 |
| DM-4 | PointsRuleEntity | 规则实体 | FR-P5 |
| DM-5 | TransactionType 枚举 | GRANT / DEDUCT / REFUND / EXPIRE / ADJUST | FR-P2 |
| DM-6 | GrantType 枚举 | ONBOARDING / PERIODIC / MANUAL / OTHER | FR-P3~P6 |

### 3.3 领域服务层（domain-api / domain-impl）

| # | Gap | 说明 | 对应需求 |
|---|-----|------|----------|
| DS-1 | PointsAccountDomainService | 余额查询（汇总未过期批次） | FR-P1, US-15 |
| DS-2 | PointsGrantDomainService | 发放/扣减(FIFO)/退回/手动调整 | FR-P3~P8 |
| DS-3 | PointsTransactionDomainService | 流水写入与查询 | FR-P2, US-16 |
| DS-4 | PointsRuleDomainService | 规则 CRUD | FR-P5, US-22 |
| DS-5 | PointsExpiryDomainService | 过期批次识别与处理 | FR-P7, US-17 |

### 3.4 仓储端口与实现（repository-api / mysql-impl）

| # | Gap | 说明 |
|---|-----|------|
| RP-1 | PointsAccountRepository（端口 + 实现） | 按 userId 查询/更新余额 |
| RP-2 | PointsBatchRepository（端口 + 实现） | 按 userId+status 查批次列表，按 expireAt 排序(FIFO) |
| RP-3 | PointsTransactionRepository（端口 + 实现） | 分页查询用户流水 |
| RP-4 | PointsRuleRepository（端口 + 实现） | 全局规则查询/更新 |

### 3.5 应用服务层（application-api / application-impl）

| # | Gap | 说明 | 对应 US |
|---|-----|------|---------|
| AS-1 | PointsAccountApplicationService | 查询余额 | US-15 |
| AS-2 | PointsTransactionApplicationService | 查询变动记录（分页） | US-16, US-24 |
| AS-3 | PointsRuleApplicationService | 规则配置 CRUD | US-22 |
| AS-4 | PointsAdjustApplicationService | 管理员手动调整 | US-23 |
| AS-5 | PointsGrantApplicationService | 内部发放/扣减/退回 | FR-P3, FR-P8 |

### 3.6 接口层（interface-http）

| # | Gap | URL | 说明 | 对应 US |
|---|-----|-----|------|---------|
| IF-1 | 查看积分余额 | `POST /api/v1/public/point/balance/get` | 员工查余额 | US-15 |
| IF-2 | 查看积分变动 | `POST /api/v1/public/point/transaction/list` | 员工查流水 | US-16 |
| IF-3 | 积分到期提示 | `POST /api/v1/public/point/expiring/get` | 即将过期积分 | US-17 |
| IF-4 | 配置积分规则 | `POST /api/v1/point/rule/get` + `/update` | 管理员 | US-22 |
| IF-5 | 手动调整积分 | `POST /api/v1/point/adjust` | 管理员 | US-23 |
| IF-6 | 查用户积分变动 | `POST /api/v1/point/transaction/list` | 管理员查任意用户 | US-24 |
| IF-7 | 发放入职奖励 | `POST /api/v1/private/point/grant` | auth-service 调用 | FR-P3 |
| IF-8 | 兑换扣减 | `POST /api/v1/private/point/deduct` | order-service 调用 | FR-P8 |
| IF-9 | 取消退回 | `POST /api/v1/private/point/refund` | order-service 调用 | FR-P8 |

### 3.7 定时任务

| # | Gap | 说明 | 对应需求 |
|---|-----|------|----------|
| SC-1 | 周期性发放调度 | 按规则定期给目标员工发放积分 | FR-P4, US-22 |
| SC-2 | 积分过期处理 | 扫描到期批次，FIFO 失效，写 EXPIRE 流水 | FR-P7, US-17 |

### 3.8 基础设施 / 横切

| # | Gap | 说明 |
|---|-----|------|
| INF-1 | OperatorId 上下文拦截器 | 从网关注入的 body/header 提取 operatorId，设置到 UserContext |
| INF-2 | InternalAuthFilter | 内部接口(/private/)仅接受内网调用（或简单 token 校验） |

---

## 4. Gap 优先级评分表

| 评分维度（权重） | Must User Story | 跨服务接口 | 定时任务 | Should User Story |
|---|---|---|---|---|
| 业务价值/用户可见（30%） | 5 | 3（间接） | 2（后台） | 4 |
| 阻塞其他团队（25%） | 2 | 5（auth/order 等你） | 1 | 1 |
| 技术依赖/基础性（25%） | 4（需要数据模型） | 4（需要数据模型） | 3（需要批次模型） | 2 |
| 实现复杂度-越低越先（20%） | 4（相对简单） | 3（FIFO 逻辑） | 2（定时+FIFO） | 4 |
| **加权总分** | **3.85** | **3.85** | **2.05** | **2.85** |

**结论**：Must User Story 和跨服务接口并列最高优先级，但它们共享同一套数据模型。因此：

---

## 5. 推荐实施顺序

```
┌─────────────────────────────────────────────────────────┐
│ Phase 0 · 数据基础（所有功能的前提）                      │
│   - 4 张表的 Flyway 迁移脚本                             │
│   - 4 个领域实体 + 枚举                                  │
│   - 4 个 Repository 端口 + MySQL 实现                    │
│   - OperatorId 拦截器                                    │
└─────────────────────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────────────────────┐
│ Phase 1 · Must Stories + 跨服务接口（并行推进）           │
│   - US-15 查看积分余额                                   │
│   - US-16 查看积分变动记录                               │
│   - US-23 手动调整用户积分                               │
│   - IF-7 发放入职奖励（/private/point/grant）            │
│   - IF-8 兑换扣减（/private/point/deduct, FIFO）         │
│   - IF-9 取消退回（/private/point/refund）               │
└─────────────────────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────────────────────┐
│ Phase 2 · 规则与自动化                                   │
│   - US-22 配置积分规则                                   │
│   - SC-1 周期性发放定时任务                              │
│   - SC-2 积分过期处理定时任务                            │
└─────────────────────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────────────────────┐
│ Phase 3 · Should Stories                                 │
│   - US-17 积分到期提示                                   │
│   - US-24 管理员查看用户积分变动                         │
│   - INF-2 InternalAuthFilter（内部接口保护）             │
└─────────────────────────────────────────────────────────┘
```

---

## 6. 总结

| 维度 | 数量 |
|------|------|
| 需要新建的数据库表 | 4 张 |
| 需要新建的领域实体 | 4 个 + 2 枚举 |
| 需要新建的 Repository | 4 对（端口+实现） |
| 需要新建的领域服务 | 5 个 |
| 需要新建的应用服务 | 5 个 |
| 需要新建的 Controller 接口 | 9 个端点 |
| 需要新建的定时任务 | 2 个 |
| 需要新建的拦截器/过滤器 | 2 个 |
| 现有可复用 | 脚手架架构、全局异常、Result、分页、UserContext、Flyway、Swagger |
| 需要删除/修改 | Test CRUD 示例（可保留作参考或删除） |

**脚手架完成度约 30%**（架构+配置+规范完成，但无任何积分业务逻辑），**业务功能完成度 0%**。
