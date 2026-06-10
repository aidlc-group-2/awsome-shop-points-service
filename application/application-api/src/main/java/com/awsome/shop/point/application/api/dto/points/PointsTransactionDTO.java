package com.awsome.shop.point.application.api.dto.points;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分变动记录 DTO
 */
@Data
public class PointsTransactionDTO {

    private Long id;

    private Long userId;

    /** 变动类型: GRANT/DEDUCT/REFUND/EXPIRE/ADJUST */
    private String type;

    /** 变动金额 */
    private Long amount;

    /** 变动后余额 */
    private Long balanceAfter;

    /** 关联订单号 */
    private String orderRef;

    /** 变动原因 */
    private String reason;

    /** 操作人ID */
    private Long operatorId;

    private LocalDateTime createdAt;
}
