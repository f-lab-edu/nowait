package com.nowait.booking.dto.response;

import lombok.Builder;

@Builder
public record BookingRes(
    Long bookingId,
    String bookingStatus,
    boolean depositRequired,
    boolean confirmRequired
) {

}
