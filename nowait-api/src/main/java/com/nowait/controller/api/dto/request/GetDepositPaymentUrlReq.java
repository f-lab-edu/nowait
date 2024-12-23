package com.nowait.controller.api.dto.request;

public record GetDepositPaymentUrlReq(
    long bookingId,
    String paymentMethod,
    int amount
) {

}
