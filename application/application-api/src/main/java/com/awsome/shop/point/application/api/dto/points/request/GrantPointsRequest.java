package com.awsome.shop.point.application.api.dto.points.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 内部积分发放请求（auth-service 注册后调用）
 */
@Data
public class GrantPointsRequest {

    /** 目标用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 发放金额 */
    @NotNull(message = "发放金额不能为空")
    @Min(value = 1, message = "发放金额必须大于0")
    private Long amount;

    /** 发放类型: ONBOARDING / PERIODIC / MANUAL / OTHER */
    @NotNull(message = "发放类型不能为空")
    private String grantType;

    /** 发放原因 */
    private String reason;
}
