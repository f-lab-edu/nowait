package com.nowait.application.event;

import java.time.LocalDateTime;

public record PaymentSuccessEvent(
    String paymentKey,
    Integer amount,
    Long bookingId,
    LocalDateTime approvedAt
) {

}
