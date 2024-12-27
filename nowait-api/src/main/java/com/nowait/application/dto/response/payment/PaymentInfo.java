package com.nowait.application.dto.response.payment;

import java.time.LocalDateTime;

public record PaymentInfo(
    String tid,
    String redirectAppUrl,
    String redirectMobileUrl,
    String redirectPcUrl,
    String redirectAndroidAppScheme,
    String redirectIosAppScheme,
    LocalDateTime createdAt
) {

}
