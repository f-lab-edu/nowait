package com.nowait.booking.dto;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record TimeSlotDto(
    @JsonFormat(shape = STRING, pattern = "HH:mm")
    LocalTime time,
    boolean available
) {

}
