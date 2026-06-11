package com.awsome.shop.point.domain.model.points;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PointsAccountEntity 单元测试
 *
 * <p>domain-model 模块无测试依赖，实体测试放在 domain-impl 中执行。</p>
 */
class PointsAccountEntityTest {

    private PointsAccountEntity account(long balance) {
        PointsAccountEntity account = new PointsAccountEntity();
        account.setUserId(1L);
        account.setBalance(balance);
        return account;
    }

    @Test
    @DisplayName("addBalance 应累加余额")
    void addBalanceShouldIncrease() {
        PointsAccountEntity account = account(100L);

        account.addBalance(50L);

        assertThat(account.getBalance()).isEqualTo(150L);
    }

    @Test
    @DisplayName("deductBalance 余额充足时正常扣减")
    void deductBalanceShouldDecrease() {
        PointsAccountEntity account = account(100L);

        account.deductBalance(40L);

        assertThat(account.getBalance()).isEqualTo(60L);
    }

    @Test
    @DisplayName("deductBalance 可扣到 0")
    void deductBalanceShouldAllowExactBalance() {
        PointsAccountEntity account = account(100L);

        account.deductBalance(100L);

        assertThat(account.getBalance()).isZero();
    }

    @Test
    @DisplayName("deductBalance 余额不足时抛 IllegalStateException")
    void deductBalanceShouldThrowWhenInsufficient() {
        PointsAccountEntity account = account(30L);

        assertThatThrownBy(() -> account.deductBalance(31L))
                .isInstanceOf(IllegalStateException.class);
        assertThat(account.getBalance()).isEqualTo(30L);
    }
}
