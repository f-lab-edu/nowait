package com.nowait.domain.model.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AmountDepositPolicyUnitTest {

    AmountDepositPolicy amountDepositPolicy;
    int amount;

    @Mock
    Booking booking;

    @BeforeEach
    void setUp() {
        amount = 10_000;
        amountDepositPolicy = new AmountDepositPolicy(amount);
    }

    @DisplayName("예약금 금액은 인당 예약금 금액 * 예약 인원 수로 계산된다.")
    @Test
    void getDepositAmount() {
        // given
        int partySize = 2;
        when(booking.getPartySize()).thenReturn(partySize);

        // when
        int depositAmount = amountDepositPolicy.getDepositAmount(booking);

        // then
        assertThat(depositAmount).isEqualTo(amount * partySize);
    }

}
