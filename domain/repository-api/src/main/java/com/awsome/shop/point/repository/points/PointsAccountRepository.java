package com.awsome.shop.point.repository.points;

import com.awsome.shop.point.domain.model.points.PointsAccountEntity;

import java.util.List;

/**
 * 积分账户仓储接口
 */
public interface PointsAccountRepository {

    /**
     * 根据用户ID查询积分账户
     */
    PointsAccountEntity getByUserId(Long userId);

    /**
     * 查询所有账户（供周期性发放使用）
     */
    List<PointsAccountEntity> findAll();

    /**
     * 保存新账户
     */
    void save(PointsAccountEntity entity);

    /**
     * 更新账户（余额变动）
     */
    void update(PointsAccountEntity entity);
}
