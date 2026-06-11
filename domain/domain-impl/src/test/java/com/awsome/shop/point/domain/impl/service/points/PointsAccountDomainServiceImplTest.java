package com.awsome.shop.point.domain.impl.service.points;

import com.awsome.shop.point.domain.model.points.PointsAccountEntity;
import com.awsome.shop.point.repository.points.PointsAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PointsAccountDomainServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class PointsAccountDomainServiceImplTest {

    @Mock
    private PointsAccountRepository pointsAccountRepository;

    @InjectMocks
    private PointsAccountDomainServiceImpl pointsAccountDomainService;

    @Test
    @DisplayName("getOrCreateAccount 已存在时直接返回")
    void getOrCreateAccountShouldReturnExisting() {
        PointsAccountEntity existing = new PointsAccountEntity();
        existing.setUserId(1L);
        existing.setBalance(500L);
        when(pointsAccountRepository.getByUserId(1L)).thenReturn(existing);

        PointsAccountEntity result = pointsAccountDomainService.getOrCreateAccount(1L);

        assertThat(result).isSameAs(existing);
        verify(pointsAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrCreateAccount 不存在时创建零余额账户")
    void getOrCreateAccountShouldCreateWithZeroBalance() {
        when(pointsAccountRepository.getByUserId(2L)).thenReturn(null);

        PointsAccountEntity result = pointsAccountDomainService.getOrCreateAccount(2L);

        ArgumentCaptor<PointsAccountEntity> captor = ArgumentCaptor.forClass(PointsAccountEntity.class);
        verify(pointsAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(2L);
        assertThat(captor.getValue().getBalance()).isZero();
        assertThat(result).isSameAs(captor.getValue());
    }

    @Test
    @DisplayName("getBalance 账户存在时返回余额")
    void getBalanceShouldReturnBalance() {
        PointsAccountEntity account = new PointsAccountEntity();
        account.setBalance(300L);
        when(pointsAccountRepository.getByUserId(1L)).thenReturn(account);

        assertThat(pointsAccountDomainService.getBalance(1L)).isEqualTo(300L);
    }

    @Test
    @DisplayName("getBalance 账户不存在时返回 0")
    void getBalanceShouldReturnZeroForMissingAccount() {
        when(pointsAccountRepository.getByUserId(9L)).thenReturn(null);

        assertThat(pointsAccountDomainService.getBalance(9L)).isZero();
    }
}
