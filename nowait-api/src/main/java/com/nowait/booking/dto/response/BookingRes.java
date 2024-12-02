package com.nowait.booking.dto.response;

public record BookingRes(
    Long bookingId,
    String bookingStatus,
    boolean depositRequired,
    boolean confirmRequired
) {

}
