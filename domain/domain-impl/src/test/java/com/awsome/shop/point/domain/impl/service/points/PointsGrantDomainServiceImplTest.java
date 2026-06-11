package com.awsome.shop.point.domain.impl.service.points;

import com.awsome.shop.point.common.exception.BusinessException;
import com.awsome.shop.point.domain.model.points.BatchStatus;
import com.awsome.shop.point.domain.model.points.GrantType;
import com.awsome.shop.point.domain.model.points.PointsAccountEntity;
import com.awsome.shop.point.domain.model.points.PointsBatchEntity;
import com.awsome.shop.point.domain.model.points.PointsRuleEntity;
import com.awsome.shop.point.domain.model.points.PointsTransactionEntity;
import com.awsome.shop.point.domain.model.points.TransactionType;
import com.awsome.shop.point.domain.service.points.PointsAccountDomainService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PointsGrantDomainServiceImpl 单元测试（发放/扣减/退回/调整）
 */
@ExtendWith(MockitoExtension.class)
class PointsGrantDomainServiceImplTest {

    @Mock
    private PointsAccountDomainService pointsAccountDomainService;

    @Mock
    private PointsAccountRepository pointsAccountRepository;

    @Mock
    private PointsBatchRepository pointsBatchRepository;

    @Mock
    private PointsTransactionRepository pointsTransactionRepository;

    @Mock
    private PointsRuleRepository pointsRuleRepository;

    @InjectMocks
    private PointsGrantDomainServiceImpl pointsGrantDomainService;

    private PointsAccountEntity account(long balance) {
        PointsAccountEntity account = new PointsAccountEntity();
        account.setId(1L);
        account.setUserId(1L);
        account.setBalance(balance);
        return account;
    }

    private PointsBatchEntity activeBatch(long remaining) {
        PointsBatchEntity batch = new PointsBatchEntity();
        batch.setUserId(1L);
        batch.setAmount(remaining);
        batch.setRemaining(remaining);
        batch.setStatus(BatchStatus.ACTIVE);
        return batch;
    }

    private void stubRule(Integer validityDays) {
        PointsRuleEntity rule = new PointsRuleEntity();
        rule.setValidityDays(validityDays);
        when(pointsRuleRepository.getRule()).thenReturn(rule);
    }

    // ==================== grant ====================

    @Test
    @DisplayName("grant 成功：创建批次、增加余额、写 GRANT 流水")
    void grantShouldCreateBatchAndTransaction() {
        PointsAccountEntity account = account(100L);
        when(pointsAccountDomainService.getOrCreateAccountForUpdate(1L)).thenReturn(account);
        stubRule(365);

        pointsGrantDomainService.grant(1L, 500L, GrantType.ONBOARDING, "入职奖励");

        ArgumentCaptor<PointsBatchEntity> batchCaptor = ArgumentCaptor.forClass(PointsBatchEntity.class);
        verify(pointsBatchRepository).save(batchCaptor.capture());
        PointsBatchEntity batch = batchCaptor.getValue();
        assertThat(batch.getAmount()).isEqualTo(500L);
        assertThat(batch.getRemaining()).isEqualTo(500L);
        assertThat(batch.getGrantType()).isEqualTo(GrantType.ONBOARDING);
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.ACTIVE);
        assertThat(batch.getExpireAt()).isAfter(LocalDateTime.now().plusDays(364));

        assertThat(account.getBalance()).isEqualTo(600L);
        verify(pointsAccountRepository).update(account);

        ArgumentCaptor<PointsTransactionEntity> txnCaptor = ArgumentCaptor.forClass(PointsTransactionEntity.class);
        verify(pointsTransactionRepository).save(txnCaptor.capture());
        PointsTransactionEntity txn = txnCaptor.getValue();
        assertThat(txn.getType()).isEqualTo(TransactionType.GRANT);
        assertThat(txn.getAmount()).isEqualTo(500L);
        assertThat(txn.getBalanceAfter()).isEqualTo(600L);
        assertThat(txn.getReason()).isEqualTo("入职奖励");
    }

    @Test
    @DisplayName("grant 规则未配置有效期时批次永不过期")
    void grantShouldCreateNonExpiringBatchWithoutValidityDays() {
        when(pointsAccountDomainService.getOrCreateAccountForUpdate(1L)).thenReturn(account(0L));
        stubRule(null);

        pointsGrantDomainService.grant(1L, 100L, GrantType.MANUAL, "手动发放");

        ArgumentCaptor<PointsBatchEntity> captor = ArgumentCaptor.forClass(PointsBatchEntity.class);
        verify(pointsBatchRepository).save(captor.capture());
        assertThat(captor.getValue().getExpireAt()).isNull();
    }

    @Test
    @DisplayName("grant 金额非正时抛 PARAM_002")
    void grantShouldRejectNonPositiveAmount() {
        assertThatThrownBy(() -> pointsGrantDomainService.grant(1L, 0L, GrantType.MANUAL, "x"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("PARAM_002");

        verify(pointsBatchRepository, never()).save(any());
    }

    // ==================== deduct ====================

    @Test
    @DisplayName("deduct 成功：FIFO 消耗批次、扣减余额、写 DEDUCT 流水")
    void deductShouldConsumeBatchesFifo() {
        PointsAccountEntity account = account(300L);
        when(pointsAccountDomainService.getOrCreateAccountForUpdate(1L)).thenReturn(account);
        PointsBatchEntity first = activeBatch(100L);
        PointsBatchEntity second = activeBatch(200L);
        when(pointsBatchRepository.findActiveBatchesByUserId(1L)).thenReturn(List.of(first, second));

        pointsGrantDomainService.deduct(1L, 150L, "order-1");

        // FIFO：第一批 100 耗尽，第二批扣 50
        assertThat(first.getRemaining()).isZero();
        assertThat(first.getStatus()).isEqualTo(BatchStatus.EXHAUSTED);
        assertThat(second.getRemaining()).isEqualTo(150L);
        verify(pointsBatchRepository).update(first);
        verify(pointsBatchRepository).update(second);

        assertThat(account.getBalance()).isEqualTo(150L);

        ArgumentCaptor<PointsTransactionEntity> captor = ArgumentCaptor.forClass(PointsTransactionEntity.class);
        verify(pointsTransactionRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(TransactionType.DEDUCT);
        assertThat(captor.getValue().getAmount()).isEqualTo(-150L);
        assertThat(captor.getValue().getBalanceAfter()).isEqualTo(150L);
        assertThat(captor.getValue().getOrderRef()).isEqualTo("order-1");
    }

    @Test
    @DisplayName("deduct 刚好够扣时只消耗到所需额度")
    void deductShouldStopWhenSatisfied() {
        PointsAccountEntity account = account(300L);
        when(pointsAccountDomainService.getOrCreateAccountForUpdate(1L)).thenReturn(account);
        PointsBatchEntity first = activeBatch(100L);
        PointsBatchEntity second = activeBatch(200L);
        when(pointsBatchRepository.findActiveBatchesByUserId(1L)).thenReturn(List.of(first, second));

        pointsGrantDomainService.deduct(1L, 80L, "order-2");

        assertThat(first.getRemaining()).isEqualTo(20L);
        assertThat(second.getRemaining()).isEqualTo(200L);
        verify(pointsBatchRepository).update(first);
        verify(pointsBatchRepository, never()).update(second);
    }

    @Test
    @DisplayName("deduct 余额不足时抛 POINTS_001")
    void deductShouldRejectInsufficientBalance() {
        when(pointsAccountDomainService.getOrCreateAccountForUpdate(1L)).thenReturn(account(50L));

        assertThatThrownBy(() -> pointsGrantDomainService.deduct(1L, 100L, "order-3"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("POINTS_001");

        verify(pointsAccountRepository, never()).update(any());
        verify(pointsTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deduct 金额非正时抛 PARAM_001")
    void deductShouldRejectNonPositiveAmount() {
        assertThatThrownBy(() -> pointsGrantDomainService.deduct(1L, -5L, "order-4"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("PARAM_001");
    }

    // ==================== refund ====================

    @Test
    @DisplayName("refund 成功：创建 OTHER 类型批次并写 REFUND 流水")
    void refundShouldCreateBatchAndTransaction() {
        PointsAccountEntity account = account(100L);
        when(pointsAccountDomainService.getOrCreateAccountForUpdate(1L)).thenReturn(account);
        stubRule(365);

        pointsGrantDomainService.refund(1L, 200L, "order-5");

        ArgumentCaptor<PointsBatchEntity> batchCaptor = ArgumentCaptor.forClass(PointsBatchEntity.class);
        verify(pointsBatchRepository).save(batchCaptor.capture());
        assertThat(batchCaptor.getValue().getGrantType()).isEqualTo(GrantType.OTHER);
        assertThat(batchCaptor.getValue().getReason()).contains("order-5");

        assertThat(account.getBalance()).isEqualTo(300L);

        ArgumentCaptor<PointsTransactionEntity> txnCaptor = ArgumentCaptor.forClass(PointsTransactionEntity.class);
        verify(pointsTransactionRepository).save(txnCaptor.capture());
        assertThat(txnCaptor.getValue().getType()).isEqualTo(TransactionType.REFUND);
        assertThat(txnCaptor.getValue().getAmount()).isEqualTo(200L);
        assertThat(txnCaptor.getValue().getOrderRef()).isEqualTo("order-5");
    }

    @Test
    @DisplayName("refund 金额非正时抛 PARAM_002")
    void refundShouldRejectNonPositiveAmount() {
        assertThatThrownBy(() -> pointsGrantDomainService.refund(1L, 0L, "order-6"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("PARAM_002");
    }

    // ==================== adjust ====================

    @Test
    @DisplayName("adjust 正数：创建 MANUAL 批次并写 ADJUST 流水（含操作人）")
    void adjustPositiveShouldCreateManualBatch() {
        PointsAccountEntity account = account(100L);
        when(pointsAccountDomainService.getOrCreateAccountForUpdate(1L)).thenReturn(account);
        stubRule(365);

        pointsGrantDomainService.adjust(1L, 50L, "客服补偿", 99L);

        ArgumentCaptor<PointsBatchEntity> batchCaptor = ArgumentCaptor.forClass(PointsBatchEntity.class);
        verify(pointsBatchRepository).save(batchCaptor.capture());
        assertThat(batchCaptor.getValue().getGrantType()).isEqualTo(GrantType.MANUAL);

        assertThat(account.getBalance()).isEqualTo(150L);

        ArgumentCaptor<PointsTransactionEntity> txnCaptor = ArgumentCaptor.forClass(PointsTransactionEntity.class);
        verify(pointsTransactionRepository).save(txnCaptor.capture());
        assertThat(txnCaptor.getValue().getType()).isEqualTo(TransactionType.ADJUST);
        assertThat(txnCaptor.getValue().getAmount()).isEqualTo(50L);
        assertThat(txnCaptor.getValue().getOperatorId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("adjust 负数：FIFO 消耗批次并扣减余额")
    void adjustNegativeShouldConsumeBatches() {
        PointsAccountEntity account = account(200L);
        when(pointsAccountDomainService.getOrCreateAccountForUpdate(1L)).thenReturn(account);
        PointsBatchEntity batch = activeBatch(200L);
        when(pointsBatchRepository.findActiveBatchesByUserId(1L)).thenReturn(List.of(batch));

        pointsGrantDomainService.adjust(1L, -80L, "纠错回收", 99L);

        assertThat(batch.getRemaining()).isEqualTo(120L);
        assertThat(account.getBalance()).isEqualTo(120L);

        ArgumentCaptor<PointsTransactionEntity> captor = ArgumentCaptor.forClass(PointsTransactionEntity.class);
        verify(pointsTransactionRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualTo(-80L);
        assertThat(captor.getValue().getBalanceAfter()).isEqualTo(120L);
    }

    @Test
    @DisplayName("adjust 负数超出余额时抛 POINTS_001")
    void adjustNegativeShouldRejectInsufficientBalance() {
        when(pointsAccountDomainService.getOrCreateAccountForUpdate(1L)).thenReturn(account(30L));

        assertThatThrownBy(() -> pointsGrantDomainService.adjust(1L, -100L, "回收", 99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("POINTS_001");

        verify(pointsTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("adjust delta 为 0 时不做任何操作")
    void adjustZeroShouldBeNoOp() {
        pointsGrantDomainService.adjust(1L, 0L, "无操作", 99L);

        verify(pointsAccountDomainService, never()).getOrCreateAccountForUpdate(anyLong());
        verify(pointsTransactionRepository, never()).save(any());
    }
}
