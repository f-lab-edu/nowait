package com.nowait.controller.api.dto.response.payment;

public record PayDepositRes(
    Long paymentId,
    String paymentStatus,
    String paymentMethod,
    String url
) {

}
