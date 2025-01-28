package com.nowait.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.nowait.application.event.PaymentFailedEvent;
import com.nowait.application.event.PaymentRequestedEvent;
import com.nowait.application.event.PaymentRetryEvent;
import com.nowait.domain.model.payment.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
class PaymentRetryConsumerUnitTest {

    @InjectMocks
    PaymentRetryConsumer paymentRetryConsumer;
    @Mock
    PaymentService paymentService;
    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    Acknowledgment acknowledgment;
    @Mock
    Payment payment;

    String paymentKey;
    int amount;
    Long bookingId;

    @BeforeEach
    void setUp() {
        paymentKey = "paymentKey";
        amount = 20_000;
        bookingId = 1L;
    }

    @DisplayName("재시도 가능한 결제는 재시도한다.")
    @Test
    void handleFailedPayment() {
        // given
        PaymentRetryEvent event = new PaymentRetryEvent(paymentKey, amount, bookingId);
        when(paymentService.getByPaymentKey(paymentKey)).thenReturn(payment);
        when(payment.getRetryCount()).thenReturn(2);
        when(payment.getId()).thenReturn(1L);
        when(kafkaTemplate.send(eq("payments.requested"), any())).thenReturn(
            null);

        // when
        paymentRetryConsumer.handleRetryPayment(event, acknowledgment);

        // then
        verify(paymentService).getByPaymentKey(paymentKey);
        verify(kafkaTemplate).send("payments.requested", new PaymentRequestedEvent(
            bookingId, amount, paymentKey, "1"));
        verify(acknowledgment, times(1)).acknowledge();

        verifyNoMoreInteractions(paymentService, kafkaTemplate, acknowledgment);
    }

    @DisplayName("이미 3번의 재시도를 했으면, 결제 실패 이벤트를 발행한다.")
    @Test
    void handleFailedPaymentExceedRetry() {
        // given
        PaymentRetryEvent event = new PaymentRetryEvent(paymentKey, amount, bookingId);
        when(paymentService.getByPaymentKey(paymentKey)).thenReturn(payment);
        when(payment.getRetryCount()).thenReturn(3);
        when(payment.getId()).thenReturn(1L);
        when(kafkaTemplate.send(eq("payments.failed"), any())).thenReturn(
            null);

        // when
        paymentRetryConsumer.handleRetryPayment(event, acknowledgment);

        // then
        verify(paymentService).getByPaymentKey(paymentKey);
        verify(kafkaTemplate).send("payments.failed", new PaymentFailedEvent(
            paymentKey, "RETRY_EXCEEDED", "최대 재시도 횟수를 초과했습니다."));
        verify(acknowledgment, times(1)).acknowledge();

        verifyNoMoreInteractions(paymentService, kafkaTemplate, acknowledgment);
    }

}
