package com.nowait.external.api.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.kakao-pay")
public record KakaoPayProperties(
    String cid,
    String secretKey,
    String readyRequestUrl,
    String approveRequestUrl,
    String approvalUrl,
    String cancelUrl,
    String failUrl
) {

}
