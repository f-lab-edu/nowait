package com.nowait.application;

import com.nowait.application.event.PaymentFailedEvent;
import com.nowait.application.event.PaymentRequestedEvent;
import com.nowait.application.event.PaymentRetryEvent;
import com.nowait.domain.model.payment.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public class PaymentRetryConsumer {

    private static final int MAX_RETRIES = 3;

    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(
        topics = "payments.retry",
        groupId = "nowait"
    )
    @Transactional
    public void handleRetryPayment(PaymentRetryEvent event, Acknowledgment acknowledgment) {
        // 1. 결제 조회
        Payment payment = paymentService.getByPaymentKey(event.paymentKey());

        // 2-1. 재시도 최대 횟수 확인
        if (payment.getRetryCount() < MAX_RETRIES) {
            // 2-1-1. 재시도 횟수 증가
            payment.incrementRetryCount();

            // 2-1-2. 결제 승인 재시도
            PaymentRequestedEvent paymentRequestedEvent = new PaymentRequestedEvent(
                event.bookingId(), event.amount(), event.paymentKey(),
                String.valueOf(payment.getId()));
            kafkaTemplate.send("payments.requested", paymentRequestedEvent);

            // 2-1-3. 카프카 오프셋 커밋
            acknowledgment.acknowledge();
            return;
        }

        // 2-2. 최대 재시도 횟수 초과
        PaymentFailedEvent failedEvent = new PaymentFailedEvent(event.paymentKey(),
            "RETRY_EXCEEDED", "최대 재시도 횟수를 초과했습니다.");
        kafkaTemplate.send("payments.failed", failedEvent);

        log.info("최대 재시도 횟수 초과: {}", payment.getId());

        // 2-3. 카프카 오프셋 커밋
        acknowledgment.acknowledge();
    }
}
