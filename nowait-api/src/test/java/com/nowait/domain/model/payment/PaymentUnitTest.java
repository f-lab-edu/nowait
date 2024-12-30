package com.nowait.domain.model.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nowait.application.dto.response.payment.PaymentResult;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PaymentUnitTest {

    Long bookingId;
    String tid;
    PaymentType paymentType;
    int amount;
    LocalDateTime readyAt;
    String payToken;

    @BeforeEach
    void setUp() {
        bookingId = 1L;
        tid = "T76cf8a7222b2ae7bb99";
        paymentType = PaymentType.KAKAO_PAY;
        amount = 20_000;
        readyAt = LocalDateTime.of(2024, 12, 1, 12, 0);
        payToken = UUID.randomUUID().toString();
    }

    @DisplayName("결제 생성 테스트")
    @Nested
    class CreatePaymentTest {

        @DisplayName("결제를 생성할 수 있다.")
        @Test
        void of() {
            // when
            Payment payment = Payment.of(payToken, bookingId, tid, paymentType, amount, readyAt);

            // then
            assertThat(payment.getBookingId()).isEqualTo(bookingId);
            assertThat(payment.getTid()).isEqualTo(tid);
            assertThat(payment.getPaymentType()).isEqualTo(paymentType);
            assertThat(payment.getTotalAmount()).isEqualTo(amount);
            assertThat(payment.getReadyAt()).isEqualTo(readyAt);
        }

        @DisplayName("결제 토큰 없이는 결제를 생성할 수 없다.")
        @Test
        void ofWithoutPayToken() {
            // when & then
            assertThatThrownBy(() -> Payment.of(null, bookingId, tid, paymentType, amount, readyAt))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("결제 토큰은 필수값입니다.");
        }

        @DisplayName("예약 식별자 없이는 결제를 생성할 수 없다.")
        @Test
        void ofWithoutBookingId() {
            // when & then
            assertThatThrownBy(() -> Payment.of(payToken, null, tid, paymentType, amount, readyAt))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("예약 식별자는 필수값입니다.");
        }

        @DisplayName("결제 요청 식별자 없이는 결제를 생성할 수 없다.")
        @Test
        void ofWithoutTid() {
            // when & then
            assertThatThrownBy(
                () -> Payment.of(payToken, bookingId, null, paymentType, amount, readyAt))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("TID는 필수값입니다.");
        }

        @DisplayName("결제 수단 없이는 결제를 생성할 수 없다.")
        @Test
        void ofWithoutPaymentType() {
            // when & then
            assertThatThrownBy(() -> Payment.of(payToken, bookingId, tid, null, amount, readyAt))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("결제 수단은 필수값입니다.");
        }

    }

    @DisplayName("결제 확정 테스트")
    @Nested
    class ConfirmPaymentTest {

        Payment payment;
        LocalDateTime approvedAt;
        PaymentResult result;

        @BeforeEach
        void setUp() {
            payment = Payment.of(payToken, bookingId, tid, PaymentType.KAKAO_PAY, amount, readyAt);
            approvedAt = LocalDateTime.of(2024, 12, 1, 12, 3);
            result = new PaymentResult(readyAt, approvedAt, amount);
        }

        @DisplayName("결제를 확정할 수 있다.")
        @Test
        void confirm() {
            // when
            payment.confirm(result);

            // then
            assertThat(payment.getApprovedAt()).isEqualTo(result.approvedAt());
        }

        @DisplayName("결제 결과가 없으면 결제를 확정할 수 없다.")
        @Test
        void confirmWithoutResult() {
            // when & then
            assertThatThrownBy(() -> payment.confirm(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("결제 결과는 필수값입니다.");
        }

        @DisplayName("결제 승인 시각이 없으면 결제를 확정할 수 없다.")
        @Test
        void confirmWithoutApprovedAt() {
            // when & then
            assertThatThrownBy(() -> payment.confirm(new PaymentResult(readyAt, null, amount)))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("결제 승인 시각은 필수값입니다.");
        }
    }
}
