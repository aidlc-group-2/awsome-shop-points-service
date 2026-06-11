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
import org.springframework.dao.DuplicateKeyException;
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

        // 悲观锁获取账户，串行化并发余额变动
        PointsAccountEntity account = pointsAccountDomainService.getOrCreateAccountForUpdate(userId);

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

        // 幂等：同一订单的扣减若已执行，直接返回（order-service 重试安全）
        if (pointsTransactionRepository.existsByOrderRefAndType(orderRef, TransactionType.DEDUCT)) {
            return;
        }

        // 悲观锁获取账户并校验余额
        PointsAccountEntity account = pointsAccountDomainService.getOrCreateAccountForUpdate(userId);
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
        // 批次合计可用额应与余额一致；若不足说明账实漂移，回滚以防超扣
        if (remaining > 0) {
            throw new BusinessException(PointsErrorCode.INSUFFICIENT_BALANCE);
        }

        // 更新余额
        account.deductBalance(amount);
        pointsAccountRepository.update(account);

        // 写流水（uk_order_ref_type 唯一约束作为并发幂等兜底）
        PointsTransactionEntity transaction = new PointsTransactionEntity();
        transaction.setUserId(userId);
        transaction.setType(TransactionType.DEDUCT);
        transaction.setAmount(-amount);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setOrderRef(orderRef);
        transaction.setReason("兑换扣减");
        saveTransactionIdempotent(transaction);
    }

    @Override
    @Transactional
    public void refund(Long userId, long amount, String orderRef) {
        if (amount <= 0) {
            throw new BusinessException(PointsErrorCode.INVALID_GRANT_AMOUNT);
        }

        // 幂等：同一订单的退回若已执行，直接返回（Saga 补偿重试安全，防止凭空铸造积分）
        if (pointsTransactionRepository.existsByOrderRefAndType(orderRef, TransactionType.REFUND)) {
            return;
        }

        // 悲观锁获取账户
        PointsAccountEntity account = pointsAccountDomainService.getOrCreateAccountForUpdate(userId);

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
        saveTransactionIdempotent(transaction);
    }

    @Override
    @Transactional
    public void adjust(Long userId, long delta, String reason, Long operatorId) {
        if (delta == 0) {
            return;
        }

        PointsAccountEntity account = pointsAccountDomainService.getOrCreateAccountForUpdate(userId);

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
            // 减少：校验余额并 FIFO 消耗。用 negateExact 防止 Long.MIN_VALUE 取反溢出
            long absAmount;
            try {
                absAmount = Math.negateExact(delta);
            } catch (ArithmeticException e) {
                throw new BusinessException(PointsErrorCode.INVALID_DEDUCT_AMOUNT);
            }
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
            if (remaining > 0) {
                throw new BusinessException(PointsErrorCode.INSUFFICIENT_BALANCE);
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
     * 保存流水，依赖 uk_order_ref_type 唯一约束做并发幂等兜底。
     *
     * <p>先查后写存在竞态窗口，并发重试可能同时通过 exists 检查；
     * 此处捕获唯一键冲突，将其视为幂等成功（已有另一并发请求落库）。</p>
     */
    private void saveTransactionIdempotent(PointsTransactionEntity transaction) {
        try {
            pointsTransactionRepository.save(transaction);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(PointsErrorCode.DUPLICATE_OPERATION, (Object) transaction.getOrderRef());
        }
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
