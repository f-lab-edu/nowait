package com.nowait.external.api.payment;

import com.nowait.application.PaymentGateway;
import com.nowait.application.PaymentGatewayFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentGatewayFactoryImpl implements PaymentGatewayFactory {

    private final KakaoPayProperties kakaoPayProperties;

    @Override
    public PaymentGateway createPaymentGateway(String paymentMethod) {
        // TODO: 추후 결제 방식 추가 (ex. naver-pay, toss-pay)
        return new KakaoPayGateway(kakaoPayProperties);
    }
}
