package com.awsome.shop.point.domain.service.points;

import com.awsome.shop.point.domain.model.points.GrantType;

/**
 * 积分发放/扣减/退回领域服务接口
 */
public interface PointsGrantDomainService {

    /**
     * 发放积分（创建批次 + 更新余额 + 写流水）
     *
     * @param userId    用户ID
     * @param amount    发放金额
     * @param grantType 发放类型
     * @param reason    发放原因
     */
    void grant(Long userId, long amount, GrantType grantType, String reason);

    /**
     * 扣减积分（FIFO 消耗批次 + 更新余额 + 写流水）
     *
     * @param userId   用户ID
     * @param amount   扣减金额
     * @param orderRef 关联订单号（幂等用）
     */
    void deduct(Long userId, long amount, String orderRef);

    /**
     * 退回积分（创建退回批次 + 更新余额 + 写流水）
     *
     * @param userId   用户ID
     * @param amount   退回金额
     * @param orderRef 关联订单号
     */
    void refund(Long userId, long amount, String orderRef);

    /**
     * 管理员手动调整积分
     *
     * @param userId     用户ID
     * @param delta      调整额度（正数增加，负数减少）
     * @param reason     调整原因
     * @param operatorId 操作人ID
     */
    void adjust(Long userId, long delta, String reason, Long operatorId);
}
