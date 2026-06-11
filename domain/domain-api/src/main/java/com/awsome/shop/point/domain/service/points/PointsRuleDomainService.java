package com.awsome.shop.point.domain.service.points;

import com.awsome.shop.point.domain.model.points.PointsRuleEntity;

/**
 * 积分规则配置领域服务接口
 */
public interface PointsRuleDomainService {

    /**
     * 获取当前规则配置
     */
    PointsRuleEntity getRule();

    /**
     * 更新规则配置
     */
    void updateRule(Long onboardingBonus, Long periodicAmount, String periodicCycle, Integer validityDays);
}
