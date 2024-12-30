package com.nowait.domain.model.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentTypeUnitTest {

    @DisplayName("이름으로 PaymentType을 생성할 수 있다.")
    @Test
    void createPaymentType() {
        // given
        String paymentType = "KAKAO_PAY";

        // when
        PaymentType type = PaymentType.of(paymentType);

        // then
        assertThat(type).isEqualTo(PaymentType.KAKAO_PAY);
    }

    @DisplayName("대소문자에 관계없이 이름으로 PaymentType을 생성할 수 있다.")
    @Test
    void createPaymentTypeWithLowerCase() {
        // given
        String paymentType = "kakao_Pay";

        // when
        PaymentType type = PaymentType.of(paymentType);

        // then
        assertThat(type).isEqualTo(PaymentType.KAKAO_PAY);
    }

    @DisplayName("지원하지 않는 결제 방식으로는 PaymentType을 생성할 수 없다.")
    @Test
    void canNotCreateWithUnSupportedType() {
        // given
        String paymentType = "JINNY_PAY";

        // when & then
        assertThatThrownBy(() -> PaymentType.of(paymentType))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("지원하지 않는 결제 수단입니다.");
    }
}
