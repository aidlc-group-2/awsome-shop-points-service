package com.awsome.shop.point.application.impl.service.points;

import com.awsome.shop.point.application.api.dto.points.ExpiringPointsDTO;
import com.awsome.shop.point.application.api.dto.points.PointsBalanceDTO;
import com.awsome.shop.point.application.api.dto.points.PointsRuleDTO;
import com.awsome.shop.point.application.api.dto.points.PointsTransactionDTO;
import com.awsome.shop.point.application.api.dto.points.request.AdjustPointsRequest;
import com.awsome.shop.point.application.api.dto.points.request.DeductPointsRequest;
import com.awsome.shop.point.application.api.dto.points.request.GetBalanceRequest;
import com.awsome.shop.point.application.api.dto.points.request.GrantPointsRequest;
import com.awsome.shop.point.application.api.dto.points.request.ListTransactionRequest;
import com.awsome.shop.point.application.api.dto.points.request.RefundPointsRequest;
import com.awsome.shop.point.application.api.dto.points.request.UpdateRuleRequest;
import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.points.BatchStatus;
import com.awsome.shop.point.domain.model.points.GrantType;
import com.awsome.shop.point.domain.model.points.PointsBatchEntity;
import com.awsome.shop.point.domain.model.points.PointsRuleEntity;
import com.awsome.shop.point.domain.model.points.PointsTransactionEntity;
import com.awsome.shop.point.domain.model.points.TransactionType;
import com.awsome.shop.point.domain.service.points.PointsAccountDomainService;
import com.awsome.shop.point.domain.service.points.PointsExpiryDomainService;
import com.awsome.shop.point.domain.service.points.PointsGrantDomainService;
import com.awsome.shop.point.domain.service.points.PointsRuleDomainService;
import com.awsome.shop.point.domain.service.points.PointsTransactionDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PointsApplicationServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class PointsApplicationServiceImplTest {

    @Mock
    private PointsAccountDomainService pointsAccountDomainService;

    @Mock
    private PointsGrantDomainService pointsGrantDomainService;

    @Mock
    private PointsTransactionDomainService pointsTransactionDomainService;

    @Mock
    private PointsRuleDomainService pointsRuleDomainService;

    @Mock
    private PointsExpiryDomainService pointsExpiryDomainService;

    @InjectMocks
    private PointsApplicationServiceImpl pointsApplicationService;

    // ==================== 员工端 ====================

    @Test
    @DisplayName("getBalance 应将 operatorId 转 Long 并返回余额")
    void getBalanceShouldParseOperatorId() {
        when(pointsAccountDomainService.getBalance(7L)).thenReturn(880L);

        GetBalanceRequest request = new GetBalanceRequest();
        request.setOperatorId("7");

        PointsBalanceDTO dto = pointsApplicationService.getBalance(request);

        assertThat(dto.getUserId()).isEqualTo(7L);
        assertThat(dto.getBalance()).isEqualTo(880L);
    }

    @Test
    @DisplayName("listTransactions 未指定 userId 时查询操作人自己")
    void listTransactionsShouldDefaultToOperator() {
        PointsTransactionEntity entity = new PointsTransactionEntity();
        entity.setId(1L);
        entity.setUserId(7L);
        entity.setType(TransactionType.GRANT);
        entity.setAmount(100L);
        entity.setBalanceAfter(100L);
        entity.setReason("入职奖励");
        entity.setCreatedAt(LocalDateTime.of(2026, 6, 1, 0, 0));

        PageResult<PointsTransactionEntity> page = new PageResult<>();
        page.setCurrent(1L);
        page.setSize(20L);
        page.setTotal(1L);
        page.setPages(1L);
        page.setRecords(List.of(entity));
        when(pointsTransactionDomainService.pageByUserId(7L, 1, 20)).thenReturn(page);

        ListTransactionRequest request = new ListTransactionRequest();
        request.setOperatorId("7");
        request.setPage(1);
        request.setSize(20);

        PageResult<PointsTransactionDTO> result = pointsApplicationService.listTransactions(request);

        assertThat(result.getTotal()).isEqualTo(1L);
        PointsTransactionDTO dto = result.getRecords().get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getType()).isEqualTo("GRANT");
        assertThat(dto.getAmount()).isEqualTo(100L);
        assertThat(dto.getBalanceAfter()).isEqualTo(100L);
        assertThat(dto.getReason()).isEqualTo("入职奖励");
    }

    @Test
    @DisplayName("listTransactions 指定 userId 时查询目标用户")
    void listTransactionsShouldUseExplicitUserId() {
        PageResult<PointsTransactionEntity> page = new PageResult<>();
        page.setRecords(Collections.emptyList());
        when(pointsTransactionDomainService.pageByUserId(42L, 1, 10)).thenReturn(page);

        ListTransactionRequest request = new ListTransactionRequest();
        request.setOperatorId("7");
        request.setUserId(42L);
        request.setPage(1);
        request.setSize(10);

        pointsApplicationService.listTransactions(request);

        verify(pointsTransactionDomainService).pageByUserId(42L, 1, 10);
    }

    @Test
    @DisplayName("getExpiringPoints 应汇总 30 天内到期批次")
    void getExpiringPointsShouldAggregateBatches() {
        LocalDateTime earliest = LocalDateTime.now().plusDays(3);
        PointsBatchEntity first = new PointsBatchEntity();
        first.setRemaining(60L);
        first.setExpireAt(earliest);
        first.setStatus(BatchStatus.ACTIVE);
        PointsBatchEntity second = new PointsBatchEntity();
        second.setRemaining(40L);
        second.setExpireAt(LocalDateTime.now().plusDays(10));
        second.setStatus(BatchStatus.ACTIVE);
        when(pointsExpiryDomainService.getExpiringBatches(7L, 30)).thenReturn(List.of(first, second));

        GetBalanceRequest request = new GetBalanceRequest();
        request.setOperatorId("7");

        ExpiringPointsDTO dto = pointsApplicationService.getExpiringPoints(request);

        assertThat(dto.getExpiringAmount()).isEqualTo(100L);
        assertThat(dto.getBatchCount()).isEqualTo(2);
        assertThat(dto.getEarliestExpireAt()).isEqualTo(earliest);
    }

    @Test
    @DisplayName("getExpiringPoints 无到期批次时返回 0")
    void getExpiringPointsShouldHandleEmpty() {
        when(pointsExpiryDomainService.getExpiringBatches(7L, 30)).thenReturn(Collections.emptyList());

        GetBalanceRequest request = new GetBalanceRequest();
        request.setOperatorId("7");

        ExpiringPointsDTO dto = pointsApplicationService.getExpiringPoints(request);

        assertThat(dto.getExpiringAmount()).isZero();
        assertThat(dto.getBatchCount()).isZero();
        assertThat(dto.getEarliestExpireAt()).isNull();
    }

    // ==================== 管理员端 ====================

    @Test
    @DisplayName("adjustPoints 应解析操作人并委托领域服务")
    void adjustPointsShouldDelegate() {
        AdjustPointsRequest request = new AdjustPointsRequest();
        request.setOperatorId("99");
        request.setUserId(7L);
        request.setDelta(-50L);
        request.setReason("纠错回收");

        pointsApplicationService.adjustPoints(request);

        verify(pointsGrantDomainService).adjust(7L, -50L, "纠错回收", 99L);
    }

    @Test
    @DisplayName("getRule 应映射规则 DTO")
    void getRuleShouldMapDto() {
        PointsRuleEntity rule = new PointsRuleEntity();
        rule.setId(1L);
        rule.setOnboardingBonus(1000L);
        rule.setPeriodicAmount(200L);
        rule.setPeriodicCycle("MONTHLY");
        rule.setValidityDays(365);
        when(pointsRuleDomainService.getRule()).thenReturn(rule);

        PointsRuleDTO dto = pointsApplicationService.getRule();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getOnboardingBonus()).isEqualTo(1000L);
        assertThat(dto.getPeriodicAmount()).isEqualTo(200L);
        assertThat(dto.getPeriodicCycle()).isEqualTo("MONTHLY");
        assertThat(dto.getValidityDays()).isEqualTo(365);
    }

    @Test
    @DisplayName("updateRule 应透传全部字段")
    void updateRuleShouldDelegate() {
        UpdateRuleRequest request = new UpdateRuleRequest();
        request.setOperatorId("99");
        request.setOnboardingBonus(2000L);
        request.setPeriodicAmount(300L);
        request.setPeriodicCycle("WEEKLY");
        request.setValidityDays(180);

        pointsApplicationService.updateRule(request);

        verify(pointsRuleDomainService).updateRule(2000L, 300L, "WEEKLY", 180);
    }

    // ==================== 内部接口 ====================

    @Test
    @DisplayName("grant 应解析 GrantType 并委托领域服务")
    void grantShouldParseGrantType() {
        GrantPointsRequest request = new GrantPointsRequest();
        request.setUserId(7L);
        request.setAmount(1000L);
        request.setGrantType("ONBOARDING");
        request.setReason("入职奖励");

        pointsApplicationService.grant(request);

        verify(pointsGrantDomainService).grant(7L, 1000L, GrantType.ONBOARDING, "入职奖励");
    }

    @Test
    @DisplayName("deduct 应委托领域服务")
    void deductShouldDelegate() {
        DeductPointsRequest request = new DeductPointsRequest();
        request.setUserId(7L);
        request.setAmount(150L);
        request.setOrderRef("order-1");

        pointsApplicationService.deduct(request);

        verify(pointsGrantDomainService).deduct(7L, 150L, "order-1");
    }

    @Test
    @DisplayName("refund 应委托领域服务")
    void refundShouldDelegate() {
        RefundPointsRequest request = new RefundPointsRequest();
        request.setUserId(7L);
        request.setAmount(150L);
        request.setOrderRef("order-1");

        pointsApplicationService.refund(request);

        verify(pointsGrantDomainService).refund(7L, 150L, "order-1");
    }
}
