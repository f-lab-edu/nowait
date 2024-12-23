package com.nowait.external.api.payment.dto;

import java.time.LocalDateTime;

public record KakaoPayReadyRes(
    String tid,
    String nextRedirectAppUrl,
    String nextRedirectMobileUrl,
    String nextRedirectPcUrl,
    String androidAppScheme,
    String iosAppScheme,
    LocalDateTime createdAt
) {

}
