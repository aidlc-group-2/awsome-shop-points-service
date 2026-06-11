package com.awsome.shop.point.domain.service.points;

import com.awsome.shop.point.domain.model.points.PointsAccountEntity;

/**
 * 积分账户领域服务接口
 */
public interface PointsAccountDomainService {

    /**
     * 获取用户积分余额（不存在则自动创建账户）
     */
    PointsAccountEntity getOrCreateAccount(Long userId);

    /**
     * 悲观锁获取账户（不存在则先创建再加锁）。
     *
     * <p>必须在事务内调用，用于串行化同一用户的并发余额变动。</p>
     */
    PointsAccountEntity getOrCreateAccountForUpdate(Long userId);

    /**
     * 获取用户积分余额
     */
    long getBalance(Long userId);
}
