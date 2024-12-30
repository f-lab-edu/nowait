package com.nowait.application.event;

public record BookedEvent(
    Long bookingId,
    Long placeId,
    Long userId
) {

}
