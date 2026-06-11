package com.awsome.shop.point.application.api.dto.points.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 查询积分变动记录请求
 */
@Data
public class ListTransactionRequest {

    /** 操作人ID（网关注入） */
    private String operatorId;

    /** 目标用户ID（管理员查其他用户时使用，为空则查自己） */
    private Long userId;

    @Min(value = 1, message = "页码最小为 1")
    private Integer page = 1;

    @Min(value = 1, message = "每页大小最小为 1")
    @Max(value = 100, message = "每页大小最大为 100")
    private Integer size = 20;
}
