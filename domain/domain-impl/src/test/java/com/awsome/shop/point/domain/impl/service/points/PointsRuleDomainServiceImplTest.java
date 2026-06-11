package com.awsome.shop.point.domain.impl.service.points;

import com.awsome.shop.point.common.exception.BusinessException;
import com.awsome.shop.point.domain.model.points.PointsRuleEntity;
import com.awsome.shop.point.repository.points.PointsRuleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PointsRuleDomainServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class PointsRuleDomainServiceImplTest {

    @Mock
    private PointsRuleRepository pointsRuleRepository;

    @InjectMocks
    private PointsRuleDomainServiceImpl pointsRuleDomainService;

    @Test
    @DisplayName("getRule 存在时返回规则")
    void getRuleShouldReturnRule() {
        PointsRuleEntity rule = new PointsRuleEntity();
        rule.setValidityDays(365);
        when(pointsRuleRepository.getRule()).thenReturn(rule);

        assertThat(pointsRuleDomainService.getRule()).isSameAs(rule);
    }

    @Test
    @DisplayName("getRule 不存在时抛 NOT_FOUND_003")
    void getRuleShouldThrowWhenMissing() {
        when(pointsRuleRepository.getRule()).thenReturn(null);

        assertThatThrownBy(() -> pointsRuleDomainService.getRule())
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("NOT_FOUND_003");
    }

    @Test
    @DisplayName("updateRule 应更新全部字段并保存")
    void updateRuleShouldModifyAndPersist() {
        PointsRuleEntity rule = new PointsRuleEntity();
        when(pointsRuleRepository.getRule()).thenReturn(rule);

        pointsRuleDomainService.updateRule(1000L, 200L, "MONTHLY", 180);

        assertThat(rule.getOnboardingBonus()).isEqualTo(1000L);
        assertThat(rule.getPeriodicAmount()).isEqualTo(200L);
        assertThat(rule.getPeriodicCycle()).isEqualTo("MONTHLY");
        assertThat(rule.getValidityDays()).isEqualTo(180);
        verify(pointsRuleRepository).update(rule);
    }

    @Test
    @DisplayName("updateRule 规则不存在时抛异常且不更新")
    void updateRuleShouldThrowWhenRuleMissing() {
        when(pointsRuleRepository.getRule()).thenReturn(null);

        assertThatThrownBy(() ->
                pointsRuleDomainService.updateRule(1L, 1L, "WEEKLY", 30))
                .isInstanceOf(BusinessException.class);
        verify(pointsRuleRepository, never()).update(any());
    }
}
