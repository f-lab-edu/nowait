package com.nowait.application;

import com.nowait.domain.model.payment.PaymentType;

public interface PaymentGatewayFactory {

    PaymentGateway createPaymentGateway(PaymentType paymentMethod);
}
