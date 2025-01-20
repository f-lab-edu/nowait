package com.nowait.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("payment")
@Getter
public record PaymentProperties(
    int depositPaymentWaitHours,
    int approvalWaitMinutes
) {

}
