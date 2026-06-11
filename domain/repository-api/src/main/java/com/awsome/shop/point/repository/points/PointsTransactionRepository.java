package com.awsome.shop.point.repository.points;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.points.PointsTransactionEntity;

/**
 * 积分变动流水仓储接口
 */
public interface PointsTransactionRepository {

    /**
     * 分页查询用户积分变动记录（按时间倒序）
     */
    PageResult<PointsTransactionEntity> pageByUserId(Long userId, int page, int size);

    /**
     * 保存变动记录
     */
    void save(PointsTransactionEntity entity);
}
