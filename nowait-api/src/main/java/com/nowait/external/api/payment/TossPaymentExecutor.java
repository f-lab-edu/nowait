package com.nowait.external.api.payment;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.nowait.application.event.PaymentFailedEvent;
import com.nowait.application.event.PaymentRequestedEvent;
import com.nowait.application.event.PaymentRetryEvent;
import com.nowait.application.event.PaymentSuccessEvent;
import com.nowait.config.TossPaymentProperties;
import com.nowait.external.api.payment.TossPayApproveRes.Failure;
import java.util.Base64;
import java.util.Set;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class TossPaymentExecutor {

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

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TossPaymentProperties properties;

    @KafkaListener(
        topics = "payments.requested",
        groupId = "nowait"
    )
    public void executeApproval(PaymentRequestedEvent event) {
        // 1. 헤더 설정
        String secretKey = properties.secretKey() + COLON;
        String encryptedSecretKey =
            BASIC_PREFIX + Base64.getEncoder().encodeToString(secretKey.getBytes());
        Consumer<HttpHeaders> headers = header -> {
            header.setContentType(APPLICATION_JSON);
            header.set(AUTHORIZATION_HEADER, encryptedSecretKey);
            header.set(IDEMPOTENCY_KEY_HEADER, event.idempotencyKey());
        };

        // 2. 요청 객체 생성
        TossPayApproveReq request = new TossPayApproveReq(event.amount(),
            String.valueOf(event.bookingId()), event.paymentKey());

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
            PaymentSuccessEvent successEvent = new PaymentSuccessEvent(event.paymentKey(),
                body.totalAmount(), Long.valueOf(body.orderId()), body.approvedAt());
            kafkaTemplate.send("payments.success", successEvent);
            return;
        }

        Failure failure = body.failure();
        if (isRetryable(response.getStatusCode(), failure.code())) {
            // 4-3. 응답 처리 - 재시도
            PaymentRetryEvent retryEvent = new PaymentRetryEvent(event.paymentKey(), event.amount(),
                event.bookingId());
            kafkaTemplate.send("payments.retry", retryEvent);
            return;
        }

        // 4-2. 응답 처리 - 실패 (재시도 불가능)
        PaymentFailedEvent failedEvent = new PaymentFailedEvent(event.paymentKey(), failure.code(),
            failure.message());
        kafkaTemplate.send("payments.failed", failedEvent);
    }

    private boolean isRetryable(HttpStatusCode statueCode, String code) {
        HttpStatus httpStatus = HttpStatus.valueOf(statueCode.value());
        return statueCode.is5xxServerError() || httpStatus == HttpStatus.REQUEST_TIMEOUT ||
            RETRYABLE_ERROR_CODES.contains(code);
    }
}
