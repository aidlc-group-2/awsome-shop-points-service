package com.awsome.shop.point.application.api.dto.points.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新积分规则请求
 */
@Data
public class UpdateRuleRequest {

    /** 操作人ID（网关注入） */
    private String operatorId;

    /** 入职奖励积分额度 */
    @NotNull(message = "入职奖励额度不能为空")
    @Min(value = 0, message = "入职奖励额度不能为负数")
    private Long onboardingBonus;

    /** 周期性发放额度 */
    @NotNull(message = "周期发放额度不能为空")
    @Min(value = 0, message = "周期发放额度不能为负数")
    private Long periodicAmount;

    /** 周期: DAILY/WEEKLY/MONTHLY */
    @NotNull(message = "发放周期不能为空")
    private String periodicCycle;

    /** 积分有效期（天数），0表示永不过期 */
    @NotNull(message = "有效期不能为空")
    @Min(value = 0, message = "有效期不能为负数")
    private Integer validityDays;
}
