package com.awsome.shop.point.domain.service.points;

import com.awsome.shop.point.domain.model.points.PointsBatchEntity;

import java.util.List;

/**
 * 积分过期处理领域服务接口
 */
public interface PointsExpiryDomainService {

    /**
     * 处理所有已过期的活跃批次：标记为 EXPIRED，扣减对应账户余额，写 EXPIRE 流水
     *
     * @return 处理的批次数量
     */
    int expirePoints();

    /**
     * 查询用户即将过期的积分（N天内到期的批次）
     *
     * @param userId 用户ID
     * @param days   天数
     * @return 即将过期的批次列表
     */
    List<PointsBatchEntity> getExpiringBatches(Long userId, int days);

    /**
     * 按规则执行周期性发放（给所有活跃用户发放积分）
     *
     * @return 发放的用户数量
     */
    int runPeriodicGrant();
}
