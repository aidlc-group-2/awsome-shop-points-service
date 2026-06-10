package com.awsome.shop.point.application.api.dto.points;

import lombok.Data;

/**
 * 积分余额 DTO
 */
@Data
public class PointsBalanceDTO {

    /** 用户ID */
    private Long userId;

    /** 当前可用积分余额 */
    private Long balance;
}
