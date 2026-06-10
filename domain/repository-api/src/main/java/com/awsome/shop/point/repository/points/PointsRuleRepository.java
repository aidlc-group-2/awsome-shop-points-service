package com.awsome.shop.point.repository.points;

import com.awsome.shop.point.domain.model.points.PointsRuleEntity;

/**
 * 积分规则配置仓储接口
 */
public interface PointsRuleRepository {

    /**
     * 获取当前规则配置（全局唯一一条）
     */
    PointsRuleEntity getRule();

    /**
     * 更新规则配置
     */
    void update(PointsRuleEntity entity);
}
