package com.nowait.controller.api.dto.request;

public record PayDepositReq(
    Long bookingId,
    String paymentMethod,
    Integer amount,
    String currency
) {

}
