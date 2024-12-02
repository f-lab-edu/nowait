package com.nowait.booking.dto.response;

public record GetDepositInfoRes(
    Long bookingId,
    Long placeId,
    boolean required,
    Integer amount,
    String description,
    String refundPolicy
) {

}
