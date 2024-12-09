package com.nowait.controller.api.dto.response.auth;

public record LoginRes(
    String accessToken,
    String refreshToken
) {

}
