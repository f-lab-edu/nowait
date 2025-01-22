package com.nowait.domain.repository;

import com.nowait.domain.model.payment.PaymentToken;
import java.util.Optional;

public interface PaymentTokenRepository {

    PaymentToken save(PaymentToken paymentToken);

    Optional<PaymentToken> findById(String paymentToken);
}
