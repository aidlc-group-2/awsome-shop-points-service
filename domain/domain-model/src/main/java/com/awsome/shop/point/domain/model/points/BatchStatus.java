package com.awsome.shop.point.domain.model.points;

/**
 * 积分批次状态枚举
 */
public enum BatchStatus {

    /** 活跃（有剩余可用额度） */
    ACTIVE,

    /** 已过期 */
    EXPIRED,

    /** 已耗尽（remaining = 0） */
    EXHAUSTED
}
