package com.awsome.shop.point.domain.model.points;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分规则配置领域实体
 */
@Data
public class PointsRuleEntity {

    private Long id;

    /** 入职奖励积分额度 */
    private Long onboardingBonus;

    /** 周期性发放额度 */
    private Long periodicAmount;

    /** 周期: DAILY/WEEKLY/MONTHLY */
    private String periodicCycle;

    /** 积分有效期（天数），0表示永不过期 */
    private Integer validityDays;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
