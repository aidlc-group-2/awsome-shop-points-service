package com.awsome.shop.point.domain.model.points;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分批次领域实体
 *
 * <p>每次积分发放创建一个批次，记录发放总额和剩余可用额度。
 * 消耗时按 FIFO（先到期/先发放的优先消耗）。</p>
 */
@Data
public class PointsBatchEntity {

    private Long id;

    /** 用户ID */
    private Long userId;

    /** 批次发放总额 */
    private Long amount;

    /** 批次剩余可用额度 */
    private Long remaining;

    /** 发放类型 */
    private GrantType grantType;

    /** 发放原因/说明 */
    private String reason;

    /** 过期时间（NULL表示永不过期） */
    private LocalDateTime expireAt;

    /** 批次状态 */
    private BatchStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 从此批次消耗指定额度
     *
     * @param deductAmount 要消耗的额度
     * @return 实际消耗的额度（可能小于请求额度，如果剩余不足）
     */
    public long consume(long deductAmount) {
        long actual = Math.min(deductAmount, this.remaining);
        this.remaining -= actual;
        if (this.remaining == 0) {
            this.status = BatchStatus.EXHAUSTED;
        }
        return actual;
    }

    /**
     * 判断批次是否已过期
     */
    public boolean isExpired() {
        if (this.expireAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.expireAt);
    }

    /**
     * 标记为过期
     */
    public void markExpired() {
        this.status = BatchStatus.EXPIRED;
    }
}
