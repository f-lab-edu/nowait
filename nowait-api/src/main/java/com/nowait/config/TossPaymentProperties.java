package com.nowait.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.toss-pay")
public record TossPaymentProperties(
    String secretKey,
    String confirmUrl
) {

}
