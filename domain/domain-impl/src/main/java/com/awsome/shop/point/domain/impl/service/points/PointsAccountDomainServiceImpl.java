package com.awsome.shop.point.domain.impl.service.points;

import com.awsome.shop.point.domain.model.points.PointsAccountEntity;
import com.awsome.shop.point.domain.service.points.PointsAccountDomainService;
import com.awsome.shop.point.repository.points.PointsAccountRepository;
import lombok.RequiredArgsConstructor;
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
            account = new PointsAccountEntity();
            account.setUserId(userId);
            account.setBalance(0L);
            pointsAccountRepository.save(account);
        }
        return account;
    }

    @Override
    public long getBalance(Long userId) {
        PointsAccountEntity account = pointsAccountRepository.getByUserId(userId);
        return account == null ? 0L : account.getBalance();
    }
}
