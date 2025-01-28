package com.nowait.application.event;

public record PaymentRetryEvent(
    String paymentKey,
    Integer amount,
    Long bookingId
) {

}
