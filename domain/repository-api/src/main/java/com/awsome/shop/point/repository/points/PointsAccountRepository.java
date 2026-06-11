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
     * 悲观锁查询积分账户（SELECT ... FOR UPDATE）。
     *
     * <p>必须在事务内调用，用于串行化同一用户的并发余额变动，杜绝丢失更新/超扣。</p>
     */
    PointsAccountEntity getByUserIdForUpdate(Long userId);

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
