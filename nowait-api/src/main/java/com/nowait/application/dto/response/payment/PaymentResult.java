package com.nowait.application.dto.response.payment;

import java.time.LocalDateTime;

public record PaymentResult(
    LocalDateTime readyAt,
    LocalDateTime approvedAt,
    int totalAmount
) {

}
