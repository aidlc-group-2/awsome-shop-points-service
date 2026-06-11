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
     * 获取用户积分余额
     */
    long getBalance(Long userId);
}
