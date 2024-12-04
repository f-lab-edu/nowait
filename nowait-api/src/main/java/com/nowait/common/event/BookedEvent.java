package com.nowait.common.event;

public record BookedEvent(
    Long bookingId,
    Long placeId,
    Long userId
) {

}
