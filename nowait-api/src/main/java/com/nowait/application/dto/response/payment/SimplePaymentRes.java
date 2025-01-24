package com.nowait.application.dto.response.payment;

import com.nowait.domain.model.payment.Payment;
import com.nowait.domain.model.payment.PaymentStatus;

public record SimplePaymentRes(
    Long id,
    PaymentStatus status
) {

    public static SimplePaymentRes of(Payment payment) {
        return new SimplePaymentRes(payment.getId(), payment.getStatus());
    }
}
