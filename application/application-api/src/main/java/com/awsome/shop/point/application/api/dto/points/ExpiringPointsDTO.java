package com.awsome.shop.point.application.api.dto.points;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 即将过期积分 DTO
 */
@Data
public class ExpiringPointsDTO {

    /** 即将过期的总积分数 */
    private Long expiringAmount;

    /** 最早过期时间 */
    private LocalDateTime earliestExpireAt;

    /** 即将过期的批次数量 */
    private Integer batchCount;
}
