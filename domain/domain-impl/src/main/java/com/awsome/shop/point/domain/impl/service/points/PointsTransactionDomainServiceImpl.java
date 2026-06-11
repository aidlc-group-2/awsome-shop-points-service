package com.awsome.shop.point.domain.impl.service.points;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.points.PointsTransactionEntity;
import com.awsome.shop.point.domain.service.points.PointsTransactionDomainService;
import com.awsome.shop.point.repository.points.PointsTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 积分变动记录领域服务实现
 */
@Service
@RequiredArgsConstructor
public class PointsTransactionDomainServiceImpl implements PointsTransactionDomainService {

    private final PointsTransactionRepository pointsTransactionRepository;

    @Override
    public PageResult<PointsTransactionEntity> pageByUserId(Long userId, int page, int size) {
        return pointsTransactionRepository.pageByUserId(userId, page, size);
    }
}
