package com.nowait.application.dto.response.auth;

public record LoginRes(
    String accessToken,
    String refreshToken
) {

}
