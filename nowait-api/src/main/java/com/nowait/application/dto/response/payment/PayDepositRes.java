package com.nowait.application.dto.response.payment;

public record PayDepositRes(
    Long paymentId,
    String paymentStatus,
    String paymentMethod,
    String url
) {

}
