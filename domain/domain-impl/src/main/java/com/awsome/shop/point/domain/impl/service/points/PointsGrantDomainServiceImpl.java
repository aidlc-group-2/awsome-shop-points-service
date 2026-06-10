package com.awsome.shop.point.domain.impl.service.points;

import com.awsome.shop.point.common.enums.PointsErrorCode;
import com.awsome.shop.point.common.exception.BusinessException;
import com.awsome.shop.point.domain.model.points.*;
import com.awsome.shop.point.domain.service.points.PointsAccountDomainService;
import com.awsome.shop.point.domain.service.points.PointsGrantDomainService;
import com.awsome.shop.point.repository.points.PointsAccountRepository;
import com.awsome.shop.point.repository.points.PointsBatchRepository;
import com.awsome.shop.point.repository.points.PointsRuleRepository;
import com.awsome.shop.point.repository.points.PointsTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分发放/扣减/退回领域服务实现
 *
 * <p>核心业务逻辑：
 * - 发放：创建批次（含过期时间）+ 更新余额 + 写流水
 * - 扣减：FIFO 消耗批次 + 更新余额 + 写流水
 * - 退回：新建批次（退回类型）+ 更新余额 + 写流水
 * - 调整：根据正负值增减余额 + 写流水
 * </p>
 */
@Service
@RequiredArgsConstructor
public class PointsGrantDomainServiceImpl implements PointsGrantDomainService {

    private final PointsAccountDomainService pointsAccountDomainService;
    private final PointsAccountRepository pointsAccountRepository;
    private final PointsBatchRepository pointsBatchRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final PointsRuleRepository pointsRuleRepository;

    @Override
    @Transactional
    public void grant(Long userId, long amount, GrantType grantType, String reason) {
        if (amount <= 0) {
            throw new BusinessException(PointsErrorCode.INVALID_GRANT_AMOUNT);
        }

        // 获取或创建账户
        PointsAccountEntity account = pointsAccountDomainService.getOrCreateAccount(userId);

        // 计算过期时间
        LocalDateTime expireAt = calculateExpireAt();

        // 创建积分批次
        PointsBatchEntity batch = new PointsBatchEntity();
        batch.setUserId(userId);
        batch.setAmount(amount);
        batch.setRemaining(amount);
        batch.setGrantType(grantType);
        batch.setReason(reason);
        batch.setExpireAt(expireAt);
        batch.setStatus(BatchStatus.ACTIVE);
        pointsBatchRepository.save(batch);

        // 更新余额
        account.addBalance(amount);
        pointsAccountRepository.update(account);

        // 写流水
        PointsTransactionEntity transaction = new PointsTransactionEntity();
        transaction.setUserId(userId);
        transaction.setType(TransactionType.GRANT);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setBatchId(batch.getId());
        transaction.setReason(reason);
        pointsTransactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void deduct(Long userId, long amount, String orderRef) {
        if (amount <= 0) {
            throw new BusinessException(PointsErrorCode.INVALID_DEDUCT_AMOUNT);
        }

        // 获取账户并校验余额
        PointsAccountEntity account = pointsAccountDomainService.getOrCreateAccount(userId);
        if (account.getBalance() < amount) {
            throw new BusinessException(PointsErrorCode.INSUFFICIENT_BALANCE);
        }

        // FIFO 消耗批次
        List<PointsBatchEntity> batches = pointsBatchRepository.findActiveBatchesByUserId(userId);
        long remaining = amount;
        for (PointsBatchEntity batch : batches) {
            if (remaining <= 0) break;
            long consumed = batch.consume(remaining);
            remaining -= consumed;
            pointsBatchRepository.update(batch);
        }

        // 更新余额
        account.deductBalance(amount);
        pointsAccountRepository.update(account);

        // 写流水
        PointsTransactionEntity transaction = new PointsTransactionEntity();
        transaction.setUserId(userId);
        transaction.setType(TransactionType.DEDUCT);
        transaction.setAmount(-amount);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setOrderRef(orderRef);
        transaction.setReason("兑换扣减");
        pointsTransactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void refund(Long userId, long amount, String orderRef) {
        if (amount <= 0) {
            throw new BusinessException(PointsErrorCode.INVALID_GRANT_AMOUNT);
        }

        // 获取或创建账户
        PointsAccountEntity account = pointsAccountDomainService.getOrCreateAccount(userId);

        // 创建退回批次（退回的积分有效期从规则中获取）
        LocalDateTime expireAt = calculateExpireAt();

        PointsBatchEntity batch = new PointsBatchEntity();
        batch.setUserId(userId);
        batch.setAmount(amount);
        batch.setRemaining(amount);
        batch.setGrantType(GrantType.OTHER);
        batch.setReason("兑换取消退回，订单: " + orderRef);
        batch.setExpireAt(expireAt);
        batch.setStatus(BatchStatus.ACTIVE);
        pointsBatchRepository.save(batch);

        // 更新余额
        account.addBalance(amount);
        pointsAccountRepository.update(account);

        // 写流水
        PointsTransactionEntity transaction = new PointsTransactionEntity();
        transaction.setUserId(userId);
        transaction.setType(TransactionType.REFUND);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setOrderRef(orderRef);
        transaction.setBatchId(batch.getId());
        transaction.setReason("兑换取消退回");
        pointsTransactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void adjust(Long userId, long delta, String reason, Long operatorId) {
        if (delta == 0) {
            return;
        }

        PointsAccountEntity account = pointsAccountDomainService.getOrCreateAccount(userId);

        if (delta > 0) {
            // 增加：创建批次
            LocalDateTime expireAt = calculateExpireAt();
            PointsBatchEntity batch = new PointsBatchEntity();
            batch.setUserId(userId);
            batch.setAmount(delta);
            batch.setRemaining(delta);
            batch.setGrantType(GrantType.MANUAL);
            batch.setReason(reason);
            batch.setExpireAt(expireAt);
            batch.setStatus(BatchStatus.ACTIVE);
            pointsBatchRepository.save(batch);

            account.addBalance(delta);
        } else {
            // 减少：校验余额并 FIFO 消耗
            long absAmount = Math.abs(delta);
            if (account.getBalance() < absAmount) {
                throw new BusinessException(PointsErrorCode.INSUFFICIENT_BALANCE);
            }

            List<PointsBatchEntity> batches = pointsBatchRepository.findActiveBatchesByUserId(userId);
            long remaining = absAmount;
            for (PointsBatchEntity batch : batches) {
                if (remaining <= 0) break;
                long consumed = batch.consume(remaining);
                remaining -= consumed;
                pointsBatchRepository.update(batch);
            }

            account.deductBalance(absAmount);
        }

        pointsAccountRepository.update(account);

        // 写流水
        PointsTransactionEntity transaction = new PointsTransactionEntity();
        transaction.setUserId(userId);
        transaction.setType(TransactionType.ADJUST);
        transaction.setAmount(delta);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setReason(reason);
        transaction.setOperatorId(operatorId);
        pointsTransactionRepository.save(transaction);
    }

    /**
     * 根据规则配置计算过期时间
     */
    private LocalDateTime calculateExpireAt() {
        PointsRuleEntity rule = pointsRuleRepository.getRule();
        if (rule == null || rule.getValidityDays() == null || rule.getValidityDays() == 0) {
            return null; // 永不过期
        }
        return LocalDateTime.now().plusDays(rule.getValidityDays());
    }
}
