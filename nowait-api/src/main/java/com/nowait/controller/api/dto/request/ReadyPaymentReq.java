package com.nowait.controller.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReadyPaymentReq(
    @NotNull
    Long bookingId,
    @NotNull
    Integer amount
) {

}
