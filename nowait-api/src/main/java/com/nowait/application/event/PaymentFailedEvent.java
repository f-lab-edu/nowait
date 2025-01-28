package com.nowait.application.event;

public record PaymentFailedEvent(
    String paymentKey,
    String code,
    String message
) {

}
