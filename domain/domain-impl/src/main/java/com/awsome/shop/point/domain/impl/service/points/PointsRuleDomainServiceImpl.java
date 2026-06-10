package com.awsome.shop.point.domain.impl.service.points;

import com.awsome.shop.point.common.enums.PointsErrorCode;
import com.awsome.shop.point.common.exception.BusinessException;
import com.awsome.shop.point.domain.model.points.PointsRuleEntity;
import com.awsome.shop.point.domain.service.points.PointsRuleDomainService;
import com.awsome.shop.point.repository.points.PointsRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 积分规则配置领域服务实现
 */
@Service
@RequiredArgsConstructor
public class PointsRuleDomainServiceImpl implements PointsRuleDomainService {

    private final PointsRuleRepository pointsRuleRepository;

    @Override
    public PointsRuleEntity getRule() {
        PointsRuleEntity rule = pointsRuleRepository.getRule();
        if (rule == null) {
            throw new BusinessException(PointsErrorCode.RULE_NOT_FOUND);
        }
        return rule;
    }

    @Override
    public void updateRule(Long onboardingBonus, Long periodicAmount, String periodicCycle, Integer validityDays) {
        PointsRuleEntity rule = getRule();
        rule.setOnboardingBonus(onboardingBonus);
        rule.setPeriodicAmount(periodicAmount);
        rule.setPeriodicCycle(periodicCycle);
        rule.setValidityDays(validityDays);
        pointsRuleRepository.update(rule);
    }
}
