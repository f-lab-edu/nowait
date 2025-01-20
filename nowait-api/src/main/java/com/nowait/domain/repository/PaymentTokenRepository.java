package com.nowait.domain.repository;

import com.nowait.domain.model.payment.PaymentToken;

public interface PaymentTokenRepository {

    PaymentToken save(PaymentToken paymentToken);
}
