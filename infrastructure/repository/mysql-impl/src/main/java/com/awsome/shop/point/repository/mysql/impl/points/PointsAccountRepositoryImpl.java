package com.awsome.shop.point.repository.mysql.impl.points;

import com.awsome.shop.point.domain.model.points.PointsAccountEntity;
import com.awsome.shop.point.repository.mysql.mapper.points.PointsAccountMapper;
import com.awsome.shop.point.repository.mysql.po.points.PointsAccountPO;
import com.awsome.shop.point.repository.points.PointsAccountRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分账户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class PointsAccountRepositoryImpl implements PointsAccountRepository {

    private final PointsAccountMapper pointsAccountMapper;

    @Override
    public PointsAccountEntity getByUserId(Long userId) {
        LambdaQueryWrapper<PointsAccountPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsAccountPO::getUserId, userId);
        PointsAccountPO po = pointsAccountMapper.selectOne(wrapper);
        return po == null ? null : toEntity(po);
    }

    @Override
    public PointsAccountEntity getByUserIdForUpdate(Long userId) {
        PointsAccountPO po = pointsAccountMapper.selectByUserIdForUpdate(userId);
        return po == null ? null : toEntity(po);
    }

    @Override
    public List<PointsAccountEntity> findAll() {
        return pointsAccountMapper.selectList(null).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void save(PointsAccountEntity entity) {
        PointsAccountPO po = toPO(entity);
        pointsAccountMapper.insert(po);
        entity.setId(po.getId());
    }

    @Override
    public void update(PointsAccountEntity entity) {
        PointsAccountPO po = toPO(entity);
        pointsAccountMapper.updateById(po);
    }

    private PointsAccountEntity toEntity(PointsAccountPO po) {
        PointsAccountEntity entity = new PointsAccountEntity();
        entity.setId(po.getId());
        entity.setUserId(po.getUserId());
        entity.setBalance(po.getBalance());
        entity.setVersion(po.getVersion());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }

    private PointsAccountPO toPO(PointsAccountEntity entity) {
        PointsAccountPO po = new PointsAccountPO();
        po.setId(entity.getId());
        po.setUserId(entity.getUserId());
        po.setBalance(entity.getBalance());
        po.setVersion(entity.getVersion());
        return po;
    }
}
