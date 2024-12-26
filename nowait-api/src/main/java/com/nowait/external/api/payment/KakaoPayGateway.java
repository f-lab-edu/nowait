package com.nowait.external.api.payment;

import com.nowait.application.PaymentGateway;
import com.nowait.application.dto.response.payment.PaymentInfo;
import com.nowait.domain.model.booking.Booking;
import com.nowait.external.api.payment.dto.KakaoPayReadyReq;
import com.nowait.external.api.payment.dto.KakaoPayReadyRes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KakaoPayGateway implements PaymentGateway {

    private static final String AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "SECRET_KEY ";
    private final KakaoPayProperties properties;

    @Override
    public boolean supports(PaymentType paymentType) {
        return PaymentType.KAKAO_PAY.equals(paymentType);
    }

    @Override
    public PaymentInfo prepare(Long userId, Booking booking, int amount) {
        KakaoPayReadyRes response = RestClient.create()
            .post()
            .uri(properties.readyRequestUrl())
            .header(AUTHORIZATION, TOKEN_PREFIX + properties.secretKey())
            .contentType(MediaType.APPLICATION_JSON)
            .body(KakaoPayReadyReq.of(userId, booking, amount, properties))
            .retrieve()
            .body(KakaoPayReadyRes.class);

        return new PaymentInfo(
            response.tid(),
            response.nextRedirectAppUrl(),
            response.nextRedirectMobileUrl(),
            response.nextRedirectPcUrl(),
            response.androidAppScheme(),
            response.iosAppScheme(),
            response.createdAt()
        );
    }
}
