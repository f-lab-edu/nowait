package com.nowait.domain.model.payment;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentToken {

    @Id
    String token;
    Long paymentId;

    public static PaymentToken of(String token, Long paymentId) {
        return new PaymentToken(token, paymentId);
    }
}
