package com.nowait.controller.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApproveDepositPaymentReq(
    @NotNull
    Long paymentId,
    @NotBlank
    String pgToken
) {

}
