package com.awsome.shop.point.application.api.dto.points.request;

import lombok.Data;

/**
 * 查询积分余额请求
 */
@Data
public class GetBalanceRequest {

    /** 操作人ID（网关注入） */
    private String operatorId;
}
