package com.awsome.shop.point.repository.mysql.impl.points;

import com.awsome.shop.point.domain.model.points.PointsRuleEntity;
import com.awsome.shop.point.repository.mysql.mapper.points.PointsRuleMapper;
import com.awsome.shop.point.repository.mysql.po.points.PointsRulePO;
import com.awsome.shop.point.repository.points.PointsRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 积分规则配置仓储实现
 */
@Repository
@RequiredArgsConstructor
public class PointsRuleRepositoryImpl implements PointsRuleRepository {

    private final PointsRuleMapper pointsRuleMapper;

    @Override
    public PointsRuleEntity getRule() {
        // 全局只有一条规则记录（id=1）
        PointsRulePO po = pointsRuleMapper.selectById(1L);
        return po == null ? null : toEntity(po);
    }

    @Override
    public void update(PointsRuleEntity entity) {
        PointsRulePO po = toPO(entity);
        pointsRuleMapper.updateById(po);
    }

    private PointsRuleEntity toEntity(PointsRulePO po) {
        PointsRuleEntity entity = new PointsRuleEntity();
        entity.setId(po.getId());
        entity.setOnboardingBonus(po.getOnboardingBonus());
        entity.setPeriodicAmount(po.getPeriodicAmount());
        entity.setPeriodicCycle(po.getPeriodicCycle());
        entity.setValidityDays(po.getValidityDays());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }

    private PointsRulePO toPO(PointsRuleEntity entity) {
        PointsRulePO po = new PointsRulePO();
        po.setId(entity.getId());
        po.setOnboardingBonus(entity.getOnboardingBonus());
        po.setPeriodicAmount(entity.getPeriodicAmount());
        po.setPeriodicCycle(entity.getPeriodicCycle());
        po.setValidityDays(entity.getValidityDays());
        return po;
    }
}
