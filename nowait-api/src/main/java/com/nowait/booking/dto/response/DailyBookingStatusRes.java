package com.nowait.booking.dto.response;

import com.nowait.booking.dto.TimeSlotDto;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record DailyBookingStatusRes(
    Long placeId,
    LocalDate date,
    List<TimeSlotDto> timeList
) {

}
