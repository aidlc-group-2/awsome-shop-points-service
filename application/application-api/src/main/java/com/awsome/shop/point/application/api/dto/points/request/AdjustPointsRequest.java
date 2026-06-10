package com.awsome.shop.point.application.api.dto.points.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员手动调整积分请求
 */
@Data
public class AdjustPointsRequest {

    /** 操作人ID（网关注入） */
    private String operatorId;

    /** 目标用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 调整额度（正数增加，负数减少） */
    @NotNull(message = "调整额度不能为空")
    private Long delta;

    /** 调整原因 */
    @NotNull(message = "调整原因不能为空")
    @Size(min = 1, max = 500, message = "原因长度为 1-500 字符")
    private String reason;
}
