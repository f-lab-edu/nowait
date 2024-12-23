package com.nowait.application;

public interface PaymentGatewayFactory {

    PaymentGateway createPaymentGateway(String paymentMethod);
}
