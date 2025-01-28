package com.nowait.application.event;

public record PaymentRequestedEvent(
    Long bookingId,
    Integer amount,
    String paymentKey,
    String idempotencyKey
) {

}
