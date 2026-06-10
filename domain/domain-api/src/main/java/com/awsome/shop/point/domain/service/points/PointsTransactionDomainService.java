package com.awsome.shop.point.domain.service.points;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.points.PointsTransactionEntity;

/**
 * 积分变动记录领域服务接口
 */
public interface PointsTransactionDomainService {

    /**
     * 分页查询用户积分变动记录
     */
    PageResult<PointsTransactionEntity> pageByUserId(Long userId, int page, int size);
}
