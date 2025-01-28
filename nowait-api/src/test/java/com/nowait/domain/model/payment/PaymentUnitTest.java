package com.nowait.domain.model.payment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentUnitTest {

    Long bookingId;
    Long userId;
    int amount;

    @BeforeEach
    void setUp() {
        bookingId = 1L;
        userId = 1L;
        amount = 20_000;
    }

    @DisplayName("결제를 생성한다.")
    @Test
    void of() {
        // when
        Payment payment = Payment.of(bookingId, userId, amount);

        // then
        assertThat(payment.getBookingId()).isEqualTo(bookingId);
        assertThat(payment.getUserId()).isEqualTo(userId);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(payment.getRetryCount()).isEqualTo(0);
    }

    @DisplayName("재시도 횟수를 증가시킬 수 있다.")
    @Test
    void incrementRetryCount() {
        // given
        Payment payment = Payment.of(bookingId, userId, amount);
        assertThat(payment.getRetryCount()).isEqualTo(0);

        // when
        payment.incrementRetryCount();

        // then
        assertThat(payment.getRetryCount()).isEqualTo(1);
    }

    @DisplayName("결제 시 결제 상태를 ABORTED로 변경하고 실패 정보를 저장한다.")
    @Test
    void fail() {
        // given
        String code = "code";
        String message = "message";
        Payment payment = Payment.of(bookingId, userId, amount);

        // when
        payment.fail(code, message);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.ABORTED);
        assertThat(payment.getErrorCode()).isEqualTo(code);
        assertThat(payment.getErrorMessage()).isEqualTo(message);
    }
}
