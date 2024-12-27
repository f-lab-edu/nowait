package com.nowait.domain.model.payment;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum PaymentType {
    KAKAO_PAY("카카오페이");

    private final String description;

    PaymentType(String description) {
        this.description = description;
    }

    public static PaymentType of(String paymentType) {
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(paymentType))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 결제 수단입니다."));
    }
}
