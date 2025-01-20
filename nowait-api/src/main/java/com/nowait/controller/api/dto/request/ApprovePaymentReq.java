package com.nowait.controller.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApprovePaymentReq(
    @NotBlank
    String paymentToken,
    @NotBlank
    String paymentKey,
    @NotNull
    Long bookingId,
    @NotNull
    Integer amount
) {

}
