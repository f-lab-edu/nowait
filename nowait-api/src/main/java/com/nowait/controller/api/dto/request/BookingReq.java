package com.nowait.controller.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @param placeId   가게 식별자
 * @param date      예약 날짜
 * @param time      예약 시간
 * @param partySize 예약 인원 (default: 1)
 */
public record BookingReq(
    @NotNull
    Long placeId,
    @NotNull
    LocalDate date,
    @NotNull
    LocalTime time,
    Integer partySize
) {

}
