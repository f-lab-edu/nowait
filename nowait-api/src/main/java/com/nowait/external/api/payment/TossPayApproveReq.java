package com.nowait.external.api.payment;

public record TossPayApproveReq(
    int amount,
    String orderId,
    String paymentKey
) {

}
