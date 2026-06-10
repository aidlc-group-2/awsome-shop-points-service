package com.awsome.shop.point.domain.model.points;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分账户领域实体
 */
@Data
public class PointsAccountEntity {

    private Long id;

    /** 用户ID */
    private Long userId;

    /** 当前可用积分余额 */
    private Long balance;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 增加余额
     */
    public void addBalance(long amount) {
        this.balance += amount;
    }

    /**
     * 扣减余额（调用前需校验余额充足）
     */
    public void deductBalance(long amount) {
        if (this.balance < amount) {
            throw new IllegalStateException("积分余额不足");
        }
        this.balance -= amount;
    }
}
