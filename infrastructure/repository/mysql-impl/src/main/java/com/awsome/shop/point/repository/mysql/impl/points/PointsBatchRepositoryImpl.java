package com.awsome.shop.point.repository.mysql.impl.points;

import com.awsome.shop.point.domain.model.points.BatchStatus;
import com.awsome.shop.point.domain.model.points.GrantType;
import com.awsome.shop.point.domain.model.points.PointsBatchEntity;
import com.awsome.shop.point.repository.mysql.mapper.points.PointsBatchMapper;
import com.awsome.shop.point.repository.mysql.po.points.PointsBatchPO;
import com.awsome.shop.point.repository.points.PointsBatchRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分批次仓储实现
 */
@Repository
@RequiredArgsConstructor
public class PointsBatchRepositoryImpl implements PointsBatchRepository {

    private final PointsBatchMapper pointsBatchMapper;

    @Override
    public List<PointsBatchEntity> findActiveBatchesByUserId(Long userId) {
        LambdaQueryWrapper<PointsBatchPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsBatchPO::getUserId, userId)
                .eq(PointsBatchPO::getStatus, BatchStatus.ACTIVE.name())
                .orderByAsc(PointsBatchPO::getExpireAt)
                .orderByAsc(PointsBatchPO::getCreatedAt);
        return pointsBatchMapper.selectList(wrapper).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PointsBatchEntity> findExpiredActiveBatches(LocalDateTime now) {
        LambdaQueryWrapper<PointsBatchPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsBatchPO::getStatus, BatchStatus.ACTIVE.name())
                .isNotNull(PointsBatchPO::getExpireAt)
                .le(PointsBatchPO::getExpireAt, now);
        return pointsBatchMapper.selectList(wrapper).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PointsBatchEntity> findExpiringBatches(Long userId, LocalDateTime deadline) {
        LambdaQueryWrapper<PointsBatchPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsBatchPO::getUserId, userId)
                .eq(PointsBatchPO::getStatus, BatchStatus.ACTIVE.name())
                .isNotNull(PointsBatchPO::getExpireAt)
                .le(PointsBatchPO::getExpireAt, deadline)
                .gt(PointsBatchPO::getRemaining, 0)
                .orderByAsc(PointsBatchPO::getExpireAt);
        return pointsBatchMapper.selectList(wrapper).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void save(PointsBatchEntity entity) {
        PointsBatchPO po = toPO(entity);
        pointsBatchMapper.insert(po);
        entity.setId(po.getId());
    }

    @Override
    public void update(PointsBatchEntity entity) {
        PointsBatchPO po = toPO(entity);
        pointsBatchMapper.updateById(po);
    }

    private PointsBatchEntity toEntity(PointsBatchPO po) {
        PointsBatchEntity entity = new PointsBatchEntity();
        entity.setId(po.getId());
        entity.setUserId(po.getUserId());
        entity.setAmount(po.getAmount());
        entity.setRemaining(po.getRemaining());
        entity.setGrantType(GrantType.valueOf(po.getGrantType()));
        entity.setReason(po.getReason());
        entity.setExpireAt(po.getExpireAt());
        entity.setStatus(BatchStatus.valueOf(po.getStatus()));
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }

    private PointsBatchPO toPO(PointsBatchEntity entity) {
        PointsBatchPO po = new PointsBatchPO();
        po.setId(entity.getId());
        po.setUserId(entity.getUserId());
        po.setAmount(entity.getAmount());
        po.setRemaining(entity.getRemaining());
        po.setGrantType(entity.getGrantType().name());
        po.setReason(entity.getReason());
        po.setExpireAt(entity.getExpireAt());
        po.setStatus(entity.getStatus().name());
        return po;
    }
}
