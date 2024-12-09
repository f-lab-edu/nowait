package com.nowait.controller.api.dto.response.booking;

import java.time.LocalDate;
import java.util.List;

public record DailyBookingStatusRes(
    Long placeId,
    LocalDate date,
    List<TimeSlotDto> timeList
) {

}
