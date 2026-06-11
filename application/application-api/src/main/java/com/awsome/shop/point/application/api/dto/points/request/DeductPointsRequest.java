package com.awsome.shop.point.application.api.dto.points.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 内部积分扣减请求（order-service 兑换时调用）
 */
@Data
public class DeductPointsRequest {

    /** 目标用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 扣减金额 */
    @NotNull(message = "扣减金额不能为空")
    @Min(value = 1, message = "扣减金额必须大于0")
    private Long amount;

    /** 关联订单号（幂等用） */
    @NotBlank(message = "订单号不能为空")
    private String orderRef;
}
