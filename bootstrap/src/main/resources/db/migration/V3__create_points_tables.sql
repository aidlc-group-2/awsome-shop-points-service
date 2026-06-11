-- ============================================================
-- 积分服务核心表结构
-- ============================================================

-- 1. 积分账户表（每个用户一条记录）
CREATE TABLE `points_account` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `balance`     BIGINT       NOT NULL DEFAULT 0 COMMENT '当前可用积分余额',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by`  BIGINT                DEFAULT NULL COMMENT '创建人',
    `updated_by`  BIGINT                DEFAULT NULL COMMENT '更新人',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    `version`     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分账户表';

-- 2. 积分批次表（每次发放为一个批次，FIFO 消耗）
CREATE TABLE `points_batch` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `amount`      BIGINT       NOT NULL COMMENT '批次发放总额',
    `remaining`   BIGINT       NOT NULL COMMENT '批次剩余可用额度',
    `grant_type`  VARCHAR(32)  NOT NULL COMMENT '发放类型: ONBOARDING/PERIODIC/MANUAL/OTHER',
    `reason`      VARCHAR(500)          DEFAULT NULL COMMENT '发放原因/说明',
    `expire_at`   DATETIME              DEFAULT NULL COMMENT '过期时间（NULL表示永不过期）',
    `status`      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/EXPIRED/EXHAUSTED',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by`  BIGINT                DEFAULT NULL COMMENT '创建人',
    `updated_by`  BIGINT                DEFAULT NULL COMMENT '更新人',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    `version`     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    INDEX `idx_user_status_expire` (`user_id`, `status`, `expire_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分批次表';

-- 3. 积分变动流水表
CREATE TABLE `points_transaction` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT       NOT NULL COMMENT '用户ID',
    `type`          VARCHAR(32)  NOT NULL COMMENT '变动类型: GRANT/DEDUCT/REFUND/EXPIRE/ADJUST',
    `amount`        BIGINT       NOT NULL COMMENT '变动金额（正数为增加，负数为减少）',
    `balance_after` BIGINT       NOT NULL COMMENT '变动后余额',
    `order_ref`     VARCHAR(64)           DEFAULT NULL COMMENT '关联订单号（兑换/退回时）',
    `batch_id`      BIGINT                DEFAULT NULL COMMENT '关联批次ID',
    `reason`        VARCHAR(500)          DEFAULT NULL COMMENT '变动原因/说明',
    `operator_id`   BIGINT                DEFAULT NULL COMMENT '操作人ID（手动调整时）',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by`    BIGINT                DEFAULT NULL COMMENT '创建人',
    `updated_by`    BIGINT                DEFAULT NULL COMMENT '更新人',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    `version`       INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    INDEX `idx_user_created` (`user_id`, `created_at` DESC),
    INDEX `idx_order_ref` (`order_ref`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分变动流水表';

-- 4. 积分规则配置表（全局配置，通常只有一条记录）
CREATE TABLE `points_rule` (
    `id`               BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `onboarding_bonus` BIGINT     NOT NULL DEFAULT 0 COMMENT '入职奖励积分额度',
    `periodic_amount`  BIGINT     NOT NULL DEFAULT 0 COMMENT '周期性发放额度',
    `periodic_cycle`   VARCHAR(32) NOT NULL DEFAULT 'MONTHLY' COMMENT '周期: DAILY/WEEKLY/MONTHLY',
    `validity_days`    INT        NOT NULL DEFAULT 365 COMMENT '积分有效期（天数），0表示永不过期',
    `created_at`       DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by`       BIGINT              DEFAULT NULL COMMENT '创建人',
    `updated_by`       BIGINT              DEFAULT NULL COMMENT '更新人',
    `deleted`          TINYINT    NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    `version`          INT        NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分规则配置表';

-- 插入默认规则
INSERT INTO `points_rule` (`onboarding_bonus`, `periodic_amount`, `periodic_cycle`, `validity_days`)
VALUES (100, 50, 'MONTHLY', 365);
