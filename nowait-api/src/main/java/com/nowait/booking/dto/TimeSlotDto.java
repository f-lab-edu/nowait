package com.nowait.booking.dto;

import java.time.LocalTime;

public record TimeSlotDto(
    LocalTime time,
    boolean available
) {

}
