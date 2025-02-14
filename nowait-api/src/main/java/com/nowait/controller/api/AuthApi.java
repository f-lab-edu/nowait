package com.nowait.controller.api;

import com.nowait.application.dto.response.auth.GetLoginPageRes;
import com.nowait.application.dto.response.auth.LoginRes;
import com.nowait.controller.api.dto.request.ReissueTokenReq;
import com.nowait.controller.api.dto.response.ApiResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthApi {

    /**
     * 소셜 로그인 페이지 조회 API
     *
     * @param socialType OAuth 제공자 (ex. kakao)
     * @return 소셜 로그인 페이지 응답
     */
    @GetMapping("/oauth/login/{socialType}")
    public ApiResult<GetLoginPageRes> getLoginPage(
        @PathVariable String socialType
    ) {
        // TODO: 소셜 로그인 페이지 조회 비즈니스 로직 호출

        return ApiResult.ok(null);
    }

    /**
     * 소셜 로그인 API
     *
     * @param socialType OAuth 제공자 (ex. kakao)
     * @param authCode   OAuth 인증 코드
     * @return 로그인 응답 (Access Token / Refresh Token)
     */
    @PostMapping("/oauth/login/{socialType}")
    public ApiResult<LoginRes> socialLogin(
        @PathVariable String socialType,
        @RequestParam String authCode
    ) {
        // TODO: 로그인 처리를 위한 비즈니스 로직 호출

        return ApiResult.ok(null);
    }

    /**
     * 토큰 재발급 API (자동 로그인)
     *
     * @param request 토큰 재발급 요청 (Refresh Token)
     * @return 토큰 재발급 응답 (Access Token / Refresh Token)
     */
    @PostMapping("/auth/reissue")
    public ApiResult<LoginRes> reissue(
        @RequestBody @Valid ReissueTokenReq request
    ) {
        // TODO: 토큰 재발급 처리를 위한 비즈니스 로직 호출

        return ApiResult.ok(null);
    }
}
