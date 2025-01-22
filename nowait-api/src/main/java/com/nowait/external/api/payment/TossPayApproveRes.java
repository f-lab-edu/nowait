package com.nowait.external.api.payment;

import java.time.LocalDateTime;

public record TossPayApproveRes(
    String paymentKey,
    String orderId,
    LocalDateTime approvedAt,
    int totalAmount,
    Failure failure
) {

    public record Failure(
        String code,
        String message
    ) {

    }

}
