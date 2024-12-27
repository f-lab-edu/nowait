package com.nowait.controller.api.dto.request;

public record ReadyDepositPaymentReq(
    long bookingId,
    String paymentType,
    int amount
) {

}
