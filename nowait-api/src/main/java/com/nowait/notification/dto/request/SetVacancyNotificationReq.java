package com.nowait.notification.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record SetVacancyNotificationReq(
    @NotNull
    Long placeId,
    @NotNull
    LocalDate date,
    @NotNull
    LocalTime time,
    @NotNull
    Integer partySize,
    String email,
    String phoneNumber
) {

}
