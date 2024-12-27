package com.nowait.external.api.payment;

import com.nowait.application.PaymentGateway;
import com.nowait.application.dto.response.payment.PaymentInfo;
import com.nowait.application.dto.response.payment.PaymentResult;
import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.payment.Payment;
import com.nowait.domain.model.payment.PaymentType;
import com.nowait.exception.PaymentApprovalException;
import com.nowait.external.api.payment.dto.request.KakaoPayApproveReq;
import com.nowait.external.api.payment.dto.request.KakaoPayReadyReq;
import com.nowait.external.api.payment.dto.response.KakaoPayApproveRes;
import com.nowait.external.api.payment.dto.response.KakaoPayReadyRes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Override
    public PaymentResult approve(Long userId, Payment payment, String pgToken) {
        ResponseEntity<KakaoPayApproveRes> response = RestClient.create()
            .post()
            .uri(properties.approveRequestUrl())
            .header(AUTHORIZATION, TOKEN_PREFIX + properties.secretKey())
            .contentType(MediaType.APPLICATION_JSON)
            .body(KakaoPayApproveReq.of(properties.cid(), payment.getTid(), payment.getBookingId(),
                userId, pgToken))
            .retrieve()
            .toEntity(KakaoPayApproveRes.class);

        HttpStatusCode statusCode = response.getStatusCode();

        if (!statusCode.is2xxSuccessful()) {
            // TODO: 추후 리트라이 로직 추가
            throw new PaymentApprovalException("결제 승인에 실패하였습니다.");
        }

        KakaoPayApproveRes body = response.getBody();

        // TODO: S3(또는 별도 스토리지)에 결제 정보 저장

        return new PaymentResult(
            body.createdAt(),
            body.approvedAt(),
            body.amount().total()
        );
    }
}
