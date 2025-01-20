package com.nowait.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("payment")
public record PaymentProperties(
    int depositPaymentWaitHours,
    int approvalWaitMinutes
) {

}
