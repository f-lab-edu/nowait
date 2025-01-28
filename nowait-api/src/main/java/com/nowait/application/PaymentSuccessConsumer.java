package com.nowait.application;

import com.nowait.application.event.PaymentSuccessEvent;
import com.nowait.domain.model.payment.Payment;
import com.nowait.domain.model.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class PaymentSuccessConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
        topics = "payments.success",
        groupId = "nowait"
    )
    @Transactional
    public void handleSuccessPayment(PaymentSuccessEvent event, Acknowledgment acknowledgment) {
        // 1. 결제 조회
        Payment payment = paymentService.getByPaymentKey(event.paymentKey());

        // 2. 결제 상태 변경
        payment.changeStatusTo(PaymentStatus.DONE);

        // 3. 카프카 오프셋 커밋
        acknowledgment.acknowledge();
    }
}
