package com.nowait.external.api.payment;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.nowait.application.PaymentExecutor;
import com.nowait.application.dto.response.payment.ApproveRes;
import com.nowait.config.TossPaymentProperties;
import com.nowait.controller.api.dto.response.ApiResult;
import com.nowait.external.api.payment.TossPayApproveRes.Failure;
import java.util.Base64;
import java.util.Set;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class TossPaymentExecutor implements PaymentExecutor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String COLON = ":";
    private static final String BASIC_PREFIX = "Basic ";
    private static final Set<String> RETRYABLE_ERROR_CODES = Set.of(
        "PROVIDER_ERROR",
        "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING",
        "FAILED_INTERNAL_SYSTEM_PROCESSING",
        "UNKNOWN_PAYMENT_ERROR"
    );


    private final TossPaymentProperties properties;

    @Override
    public ApiResult<ApproveRes> executeApproval(Long bookingId, Integer amount,
        String paymentKey, String idempotencyKey) {
        // 1. 헤더 설정
        String secretKey = properties.secretKey() + COLON;
        String encryptedSecretKey =
            BASIC_PREFIX + Base64.getEncoder().encodeToString(secretKey.getBytes());
        Consumer<HttpHeaders> headers = header -> {
            header.setContentType(APPLICATION_JSON);
            header.set(AUTHORIZATION_HEADER, encryptedSecretKey);
            header.set(IDEMPOTENCY_KEY_HEADER, idempotencyKey);
        };

        // 2. 요청 객체 생성
        TossPayApproveReq request = new TossPayApproveReq(amount, bookingId.toString(), paymentKey);

        // 3. 요청
        ResponseEntity<TossPayApproveRes> response = RestClient.create()
            .post()
            .uri(properties.confirmUrl())
            .headers(headers)
            .body(request)
            .retrieve()
            .toEntity(TossPayApproveRes.class);

        // 4-1. 응답 처리 - 성공
        TossPayApproveRes body = response.getBody();
        if (response.getStatusCode().is2xxSuccessful()) {
            return ApiResult.ok(
                ApproveRes.sucess(body.paymentKey(), body.totalAmount(),
                    Long.valueOf(body.orderId()), body.approvedAt()));
        }

        // 4-2. 응답 처리 - 실패
        Failure failure = body.failure();
        return ApiResult.of(HttpStatus.valueOf(response.getStatusCode().value()),
            ApproveRes.fail(failure.code(), failure.message(), isRetryable(failure.code())));
    }

    private boolean isRetryable(String code) {
        return RETRYABLE_ERROR_CODES.contains(code);
    }
}
