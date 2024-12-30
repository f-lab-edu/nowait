package com.nowait.domain.model.booking;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoneDepositPolicyUnitTest {

    NoneDepositPolicy noneDepositPolicy;

    @BeforeEach
    void setUp() {
        noneDepositPolicy = new NoneDepositPolicy();
    }

    @DisplayName("예약금 금액은 항상 0이다.")
    @Test
    void getDepositAmount() {
        // given
        Booking booking = new Booking();

        // when
        int depositAmount = noneDepositPolicy.getDepositAmount(booking);

        // then
        assertThat(depositAmount).isEqualTo(0);
    }

}
