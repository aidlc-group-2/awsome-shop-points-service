package com.awsome.shop.point.domain.impl.service.points;

import com.awsome.shop.point.domain.model.points.PointsAccountEntity;
import com.awsome.shop.point.domain.service.points.PointsAccountDomainService;
import com.awsome.shop.point.repository.points.PointsAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 积分账户领域服务实现
 */
@Service
@RequiredArgsConstructor
public class PointsAccountDomainServiceImpl implements PointsAccountDomainService {

    private final PointsAccountRepository pointsAccountRepository;

    @Override
    public PointsAccountEntity getOrCreateAccount(Long userId) {
        PointsAccountEntity account = pointsAccountRepository.getByUserId(userId);
        if (account == null) {
            account = createAccount(userId);
        }
        return account;
    }

    @Override
    public PointsAccountEntity getOrCreateAccountForUpdate(Long userId) {
        PointsAccountEntity account = pointsAccountRepository.getByUserIdForUpdate(userId);
        if (account == null) {
            // 账户不存在则先创建（处理并发首次创建的唯一键冲突），再加锁读取
            createAccount(userId);
            account = pointsAccountRepository.getByUserIdForUpdate(userId);
        }
        return account;
    }

    /**
     * 创建账户，并发首次创建撞唯一键时吞掉异常（已被另一线程创建）。
     */
    private PointsAccountEntity createAccount(Long userId) {
        PointsAccountEntity account = new PointsAccountEntity();
        account.setUserId(userId);
        account.setBalance(0L);
        try {
            pointsAccountRepository.save(account);
        } catch (DuplicateKeyException e) {
            return pointsAccountRepository.getByUserId(userId);
        }
        return account;
    }

    @Override
    public long getBalance(Long userId) {
        PointsAccountEntity account = pointsAccountRepository.getByUserId(userId);
        return account == null ? 0L : account.getBalance();
    }
}
