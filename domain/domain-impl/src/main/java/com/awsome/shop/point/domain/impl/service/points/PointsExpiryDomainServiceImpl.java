package com.awsome.shop.point.domain.impl.service.points;

import com.awsome.shop.point.domain.model.points.*;
import com.awsome.shop.point.domain.service.points.PointsAccountDomainService;
import com.awsome.shop.point.domain.service.points.PointsExpiryDomainService;
import com.awsome.shop.point.domain.service.points.PointsGrantDomainService;
import com.awsome.shop.point.repository.points.PointsAccountRepository;
import com.awsome.shop.point.repository.points.PointsBatchRepository;
import com.awsome.shop.point.repository.points.PointsRuleRepository;
import com.awsome.shop.point.repository.points.PointsTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分过期与周期性发放领域服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsExpiryDomainServiceImpl implements PointsExpiryDomainService {

    private final PointsBatchRepository pointsBatchRepository;
    private final PointsAccountRepository pointsAccountRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final PointsRuleRepository pointsRuleRepository;
    private final PointsAccountDomainService pointsAccountDomainService;
    private final PointsGrantDomainService pointsGrantDomainService;

    @Override
    @Transactional
    public int expirePoints() {
        LocalDateTime now = LocalDateTime.now();
        List<PointsBatchEntity> expiredBatches = pointsBatchRepository.findExpiredActiveBatches(now);

        int count = 0;
        for (PointsBatchEntity batch : expiredBatches) {
            if (batch.getRemaining() <= 0) {
                // 没有剩余额度，直接标记过期
                batch.markExpired();
                pointsBatchRepository.update(batch);
                count++;
                continue;
            }

            long expiredAmount = batch.getRemaining();

            // 标记批次为过期
            batch.markExpired();
            batch.setRemaining(0L);
            pointsBatchRepository.update(batch);

            // 扣减账户余额
            PointsAccountEntity account = pointsAccountRepository.getByUserId(batch.getUserId());
            if (account != null && account.getBalance() >= expiredAmount) {
                account.deductBalance(expiredAmount);
                pointsAccountRepository.update(account);

                // 写过期流水
                PointsTransactionEntity transaction = new PointsTransactionEntity();
                transaction.setUserId(batch.getUserId());
                transaction.setType(TransactionType.EXPIRE);
                transaction.setAmount(-expiredAmount);
                transaction.setBalanceAfter(account.getBalance());
                transaction.setBatchId(batch.getId());
                transaction.setReason("积分过期失效");
                pointsTransactionRepository.save(transaction);
            }

            count++;
        }

        if (count > 0) {
            log.info("积分过期处理完成，共处理 {} 个批次", count);
        }
        return count;
    }

    @Override
    public List<PointsBatchEntity> getExpiringBatches(Long userId, int days) {
        LocalDateTime deadline = LocalDateTime.now().plusDays(days);
        return pointsBatchRepository.findExpiringBatches(userId, deadline);
    }

    @Override
    @Transactional
    public int runPeriodicGrant() {
        PointsRuleEntity rule = pointsRuleRepository.getRule();
        if (rule == null || rule.getPeriodicAmount() == null || rule.getPeriodicAmount() <= 0) {
            log.info("周期性发放未配置或额度为0，跳过");
            return 0;
        }

        // 获取所有有积分账户的用户
        List<PointsAccountEntity> accounts = pointsAccountRepository.findAll();

        int count = 0;
        for (PointsAccountEntity account : accounts) {
            try {
                pointsGrantDomainService.grant(
                        account.getUserId(),
                        rule.getPeriodicAmount(),
                        GrantType.PERIODIC,
                        "周期性积分发放（" + rule.getPeriodicCycle() + "）"
                );
                count++;
            } catch (Exception e) {
                log.error("周期性发放失败，userId={}, error={}", account.getUserId(), e.getMessage());
            }
        }

        log.info("周期性发放完成，共发放 {} 个用户", count);
        return count;
    }
}
