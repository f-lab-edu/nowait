package com.nowait.external.api.payment;

import com.nowait.application.PaymentGateway;
import com.nowait.application.PaymentGatewayFactory;
import com.nowait.domain.model.payment.PaymentType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentGatewayFactoryImpl implements PaymentGatewayFactory {

    private final List<PaymentGateway> paymentGateways;

    @Override
    public PaymentGateway createPaymentGateway(PaymentType paymentType) {
        return paymentGateways.stream()
            .filter(paymentGateway -> paymentGateway.supports(paymentType))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 결제 수단입니다."));
    }
}
