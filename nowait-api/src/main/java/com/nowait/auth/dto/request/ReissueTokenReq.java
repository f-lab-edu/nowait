package com.nowait.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReissueTokenReq(
    @NotBlank
    String refreshToken
) {

}
