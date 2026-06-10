package com.awsome.shop.point.application.api.dto.points;

import lombok.Data;

/**
 * 积分规则配置 DTO
 */
@Data
public class PointsRuleDTO {

    private Long id;

    /** 入职奖励积分额度 */
    private Long onboardingBonus;

    /** 周期性发放额度 */
    private Long periodicAmount;

    /** 周期: DAILY/WEEKLY/MONTHLY */
    private String periodicCycle;

    /** 积分有效期（天数） */
    private Integer validityDays;
}
