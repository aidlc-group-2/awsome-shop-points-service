package com.awsome.shop.point.repository.points;

import com.awsome.shop.point.domain.model.points.PointsBatchEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分批次仓储接口
 */
public interface PointsBatchRepository {

    /**
     * 查询用户活跃批次，按过期时间升序（FIFO）
     */
    List<PointsBatchEntity> findActiveBatchesByUserId(Long userId);

    /**
     * 查询已过期但状态仍为 ACTIVE 的批次（供过期处理定时任务使用）
     */
    List<PointsBatchEntity> findExpiredActiveBatches(LocalDateTime now);

    /**
     * 查询用户即将过期的批次（N天内）
     */
    List<PointsBatchEntity> findExpiringBatches(Long userId, LocalDateTime deadline);

    /**
     * 保存新批次
     */
    void save(PointsBatchEntity entity);

    /**
     * 更新批次（消耗/过期状态变更）
     */
    void update(PointsBatchEntity entity);
}
