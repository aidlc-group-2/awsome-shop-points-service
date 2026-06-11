package com.awsome.shop.point.application.api.dto.points.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 内部积分退回请求（order-service 取消时调用）
 */
@Data
public class RefundPointsRequest {

    /** 目标用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 退回金额 */
    @NotNull(message = "退回金额不能为空")
    @Min(value = 1, message = "退回金额必须大于0")
    private Long amount;

    /** 关联订单号 */
    @NotBlank(message = "订单号不能为空")
    private String orderRef;
}
