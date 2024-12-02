package com.nowait.auth.dto.response;

public record LoginRes(
    String accessToken,
    String refreshToken
) {

}
