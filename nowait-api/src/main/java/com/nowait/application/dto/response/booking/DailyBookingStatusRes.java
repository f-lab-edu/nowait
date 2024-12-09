package com.nowait.application.dto.response.booking;

import java.time.LocalDate;
import java.util.List;

public record DailyBookingStatusRes(
    Long placeId,
    LocalDate date,
    List<TimeSlotDto> timeList
) {

}
