package com.awsome.shop.point.repository.mysql.impl.points;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.points.PointsTransactionEntity;
import com.awsome.shop.point.domain.model.points.TransactionType;
import com.awsome.shop.point.repository.mysql.mapper.points.PointsTransactionMapper;
import com.awsome.shop.point.repository.mysql.po.points.PointsTransactionPO;
import com.awsome.shop.point.repository.points.PointsTransactionRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.stream.Collectors;

/**
 * 积分变动流水仓储实现
 */
@Repository
@RequiredArgsConstructor
public class PointsTransactionRepositoryImpl implements PointsTransactionRepository {

    private final PointsTransactionMapper pointsTransactionMapper;

    @Override
    public PageResult<PointsTransactionEntity> pageByUserId(Long userId, int page, int size) {
        LambdaQueryWrapper<PointsTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsTransactionPO::getUserId, userId)
                .orderByDesc(PointsTransactionPO::getCreatedAt);

        IPage<PointsTransactionPO> result = pointsTransactionMapper.selectPage(
                new Page<>(page, size), wrapper);

        PageResult<PointsTransactionEntity> pageResult = new PageResult<>();
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setTotal(result.getTotal());
        pageResult.setPages(result.getPages());
        pageResult.setRecords(result.getRecords().stream()
                .map(this::toEntity)
                .collect(Collectors.toList()));
        return pageResult;
    }

    @Override
    public void save(PointsTransactionEntity entity) {
        PointsTransactionPO po = toPO(entity);
        pointsTransactionMapper.insert(po);
        entity.setId(po.getId());
    }

    private PointsTransactionEntity toEntity(PointsTransactionPO po) {
        PointsTransactionEntity entity = new PointsTransactionEntity();
        entity.setId(po.getId());
        entity.setUserId(po.getUserId());
        entity.setType(TransactionType.valueOf(po.getType()));
        entity.setAmount(po.getAmount());
        entity.setBalanceAfter(po.getBalanceAfter());
        entity.setOrderRef(po.getOrderRef());
        entity.setBatchId(po.getBatchId());
        entity.setReason(po.getReason());
        entity.setOperatorId(po.getOperatorId());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }

    private PointsTransactionPO toPO(PointsTransactionEntity entity) {
        PointsTransactionPO po = new PointsTransactionPO();
        po.setId(entity.getId());
        po.setUserId(entity.getUserId());
        po.setType(entity.getType().name());
        po.setAmount(entity.getAmount());
        po.setBalanceAfter(entity.getBalanceAfter());
        po.setOrderRef(entity.getOrderRef());
        po.setBatchId(entity.getBatchId());
        po.setReason(entity.getReason());
        po.setOperatorId(entity.getOperatorId());
        return po;
    }
}
