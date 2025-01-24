package com.nowait.application.dto.response.payment;

import java.time.LocalDateTime;

public record ApproveRes(
    String paymentKey,
    Integer amount,
    Long bookingId,
    LocalDateTime approvedAt,
    ApproveFailure failure
) {

    public static ApproveRes sucess(String paymentKey, int amount, Long bookingId,
        LocalDateTime approvedAt) {
        return new ApproveRes(paymentKey, amount, bookingId, approvedAt, null);
    }

    public static ApproveRes fail(String code, String message, boolean retryable) {
        return new ApproveRes(null, null, null, null,
            new ApproveFailure(code, message, retryable));
    }
}
