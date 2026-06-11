package com.awsome.shop.point.domain.impl.service.points;

import com.awsome.shop.point.domain.model.points.BatchStatus;
import com.awsome.shop.point.domain.model.points.GrantType;
import com.awsome.shop.point.domain.model.points.PointsAccountEntity;
import com.awsome.shop.point.domain.model.points.PointsBatchEntity;
import com.awsome.shop.point.domain.model.points.PointsRuleEntity;
import com.awsome.shop.point.domain.model.points.PointsTransactionEntity;
import com.awsome.shop.point.domain.model.points.TransactionType;
import com.awsome.shop.point.domain.service.points.PointsGrantDomainService;
import com.awsome.shop.point.repository.points.PointsAccountRepository;
import com.awsome.shop.point.repository.points.PointsBatchRepository;
import com.awsome.shop.point.repository.points.PointsRuleRepository;
import com.awsome.shop.point.repository.points.PointsTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PointsExpiryDomainServiceImpl 单元测试（过期处理 + 周期性发放）
 */
@ExtendWith(MockitoExtension.class)
class PointsExpiryDomainServiceImplTest {

    @Mock
    private PointsBatchRepository pointsBatchRepository;

    @Mock
    private PointsAccountRepository pointsAccountRepository;

    @Mock
    private PointsTransactionRepository pointsTransactionRepository;

    @Mock
    private PointsRuleRepository pointsRuleRepository;

    @Mock
    private com.awsome.shop.point.domain.service.points.PointsAccountDomainService pointsAccountDomainService;

    @Mock
    private PointsGrantDomainService pointsGrantDomainService;

    @InjectMocks
    private PointsExpiryDomainServiceImpl pointsExpiryDomainService;

    private PointsBatchEntity expiredBatch(Long userId, long remaining) {
        PointsBatchEntity batch = new PointsBatchEntity();
        batch.setId(10L);
        batch.setUserId(userId);
        batch.setAmount(remaining);
        batch.setRemaining(remaining);
        batch.setStatus(BatchStatus.ACTIVE);
        batch.setExpireAt(LocalDateTime.now().minusDays(1));
        return batch;
    }

    // ==================== expirePoints ====================

    @Test
    @DisplayName("expirePoints 应标记批次过期、扣减余额并写 EXPIRE 流水")
    void expirePointsShouldExpireBatchAndDeductBalance() {
        PointsBatchEntity batch = expiredBatch(1L, 100L);
        when(pointsBatchRepository.findExpiredActiveBatches(any())).thenReturn(List.of(batch));
        PointsAccountEntity account = new PointsAccountEntity();
        account.setUserId(1L);
        account.setBalance(300L);
        when(pointsAccountRepository.getByUserId(1L)).thenReturn(account);

        int count = pointsExpiryDomainService.expirePoints();

        assertThat(count).isEqualTo(1);
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.EXPIRED);
        assertThat(batch.getRemaining()).isZero();
        assertThat(account.getBalance()).isEqualTo(200L);
        verify(pointsBatchRepository).update(batch);
        verify(pointsAccountRepository).update(account);

        ArgumentCaptor<PointsTransactionEntity> captor = ArgumentCaptor.forClass(PointsTransactionEntity.class);
        verify(pointsTransactionRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(TransactionType.EXPIRE);
        assertThat(captor.getValue().getAmount()).isEqualTo(-100L);
        assertThat(captor.getValue().getBalanceAfter()).isEqualTo(200L);
    }

    @Test
    @DisplayName("expirePoints 剩余为 0 的批次只标记过期，不写流水")
    void expirePointsShouldOnlyMarkBatchWithZeroRemaining() {
        PointsBatchEntity batch = expiredBatch(1L, 0L);
        when(pointsBatchRepository.findExpiredActiveBatches(any())).thenReturn(List.of(batch));

        int count = pointsExpiryDomainService.expirePoints();

        assertThat(count).isEqualTo(1);
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.EXPIRED);
        verify(pointsAccountRepository, never()).getByUserId(anyLong());
        verify(pointsTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("expirePoints 账户余额不足以扣减时跳过余额处理")
    void expirePointsShouldSkipBalanceWhenInsufficient() {
        PointsBatchEntity batch = expiredBatch(1L, 500L);
        when(pointsBatchRepository.findExpiredActiveBatches(any())).thenReturn(List.of(batch));
        PointsAccountEntity account = new PointsAccountEntity();
        account.setUserId(1L);
        account.setBalance(100L); // 小于过期额度
        when(pointsAccountRepository.getByUserId(1L)).thenReturn(account);

        int count = pointsExpiryDomainService.expirePoints();

        assertThat(count).isEqualTo(1);
        assertThat(account.getBalance()).isEqualTo(100L);
        verify(pointsAccountRepository, never()).update(any());
        verify(pointsTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("expirePoints 无过期批次时返回 0")
    void expirePointsShouldReturnZeroWhenNothingExpired() {
        when(pointsBatchRepository.findExpiredActiveBatches(any())).thenReturn(Collections.emptyList());

        assertThat(pointsExpiryDomainService.expirePoints()).isZero();
    }

    // ==================== getExpiringBatches ====================

    @Test
    @DisplayName("getExpiringBatches 应按截止时间查询")
    void getExpiringBatchesShouldDelegate() {
        PointsBatchEntity batch = expiredBatch(1L, 50L);
        when(pointsBatchRepository.findExpiringBatches(anyLong(), any())).thenReturn(List.of(batch));

        List<PointsBatchEntity> result = pointsExpiryDomainService.getExpiringBatches(1L, 30);

        assertThat(result).containsExactly(batch);
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(pointsBatchRepository).findExpiringBatches(org.mockito.ArgumentMatchers.eq(1L), captor.capture());
        assertThat(captor.getValue()).isAfter(LocalDateTime.now().plusDays(29));
    }

    // ==================== runPeriodicGrant ====================

    private PointsRuleEntity rule(Long periodicAmount) {
        PointsRuleEntity rule = new PointsRuleEntity();
        rule.setPeriodicAmount(periodicAmount);
        rule.setPeriodicCycle("MONTHLY");
        return rule;
    }

    private PointsAccountEntity accountOf(Long userId) {
        PointsAccountEntity account = new PointsAccountEntity();
        account.setUserId(userId);
        account.setBalance(0L);
        return account;
    }

    @Test
    @DisplayName("runPeriodicGrant 应为所有账户发放周期积分")
    void runPeriodicGrantShouldGrantToAllAccounts() {
        when(pointsRuleRepository.getRule()).thenReturn(rule(200L));
        when(pointsAccountRepository.findAll()).thenReturn(List.of(accountOf(1L), accountOf(2L)));

        int count = pointsExpiryDomainService.runPeriodicGrant();

        assertThat(count).isEqualTo(2);
        verify(pointsGrantDomainService).grant(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(200L), org.mockito.ArgumentMatchers.eq(GrantType.PERIODIC), anyString());
        verify(pointsGrantDomainService).grant(org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.eq(200L), org.mockito.ArgumentMatchers.eq(GrantType.PERIODIC), anyString());
    }

    @Test
    @DisplayName("runPeriodicGrant 单个用户失败不影响其他用户")
    void runPeriodicGrantShouldContinueOnFailure() {
        when(pointsRuleRepository.getRule()).thenReturn(rule(200L));
        when(pointsAccountRepository.findAll()).thenReturn(List.of(accountOf(1L), accountOf(2L)));
        doThrow(new RuntimeException("db error"))
                .when(pointsGrantDomainService).grant(org.mockito.ArgumentMatchers.eq(1L), anyLong(), any(), anyString());

        int count = pointsExpiryDomainService.runPeriodicGrant();

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("runPeriodicGrant 规则未配置或额度为 0 时跳过")
    void runPeriodicGrantShouldSkipWithoutValidRule() {
        when(pointsRuleRepository.getRule()).thenReturn(null);
        assertThat(pointsExpiryDomainService.runPeriodicGrant()).isZero();

        when(pointsRuleRepository.getRule()).thenReturn(rule(0L));
        assertThat(pointsExpiryDomainService.runPeriodicGrant()).isZero();

        verify(pointsGrantDomainService, never()).grant(anyLong(), anyLong(), any(), anyString());
    }
}
