package com.nowait.application;

import com.nowait.application.event.PaymentFailedEvent;
import com.nowait.domain.model.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class PaymentFailConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
        topics = "payments.failed",
        groupId = "nowait"
    )
    @Transactional
    public void handleFailedPayment(PaymentFailedEvent event, Acknowledgment acknowledgment) {
        // 1. 결제 조회
        Payment payment = paymentService.getByPaymentKey(event.paymentKey());

        // 2. 실패 정보 저장
        payment.fail(event.code(), event.message());

        // 3. 카프카 오프셋 커밋
        acknowledgment.acknowledge();
    }
}
