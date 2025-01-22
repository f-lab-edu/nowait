package com.nowait.external.persistence;

import com.nowait.domain.model.payment.PaymentToken;
import com.nowait.domain.repository.PaymentTokenRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryPaymentTokenRepository implements PaymentTokenRepository {

    private final Map<String, PaymentToken> storage = new ConcurrentHashMap<>();

    @Override
    public PaymentToken save(PaymentToken paymentToken) {
        storage.put(paymentToken.getToken(), paymentToken);
        return paymentToken;
    }

    @Override
    public Optional<PaymentToken> findById(String paymentToken) {
        return Optional.ofNullable(storage.get(paymentToken));
    }
}
