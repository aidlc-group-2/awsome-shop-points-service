package com.awsome.shop.point.domain.model.points;

/**
 * 积分变动类型枚举
 */
public enum TransactionType {

    /** 发放（入职/周期/手动） */
    GRANT,

    /** 兑换扣减 */
    DEDUCT,

    /** 取消退回 */
    REFUND,

    /** 过期失效 */
    EXPIRE,

    /** 管理员手动调整 */
    ADJUST
}
