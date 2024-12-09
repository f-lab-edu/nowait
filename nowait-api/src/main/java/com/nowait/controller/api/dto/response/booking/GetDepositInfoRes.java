package com.nowait.controller.api.dto.response.booking;

public record GetDepositInfoRes(
    Long bookingId,
    Long placeId,
    boolean required,
    Integer amount,
    String description,
    String refundPolicy
) {

}
