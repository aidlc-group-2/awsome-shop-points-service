package com.awsome.shop.point.domain.model.points;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分变动流水领域实体
 */
@Data
public class PointsTransactionEntity {

    private Long id;

    /** 用户ID */
    private Long userId;

    /** 变动类型 */
    private TransactionType type;

    /** 变动金额（正数为增加，负数为减少） */
    private Long amount;

    /** 变动后余额 */
    private Long balanceAfter;

    /** 关联订单号（兑换/退回时） */
    private String orderRef;

    /** 关联批次ID */
    private Long batchId;

    /** 变动原因/说明 */
    private String reason;

    /** 操作人ID（手动调整时） */
    private Long operatorId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
