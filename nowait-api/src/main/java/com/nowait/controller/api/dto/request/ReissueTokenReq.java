package com.nowait.controller.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReissueTokenReq(
    @NotBlank
    String refreshToken
) {

}
