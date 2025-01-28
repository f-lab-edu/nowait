package com.nowait.application;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nowait.application.event.PaymentFailedEvent;
import com.nowait.domain.model.payment.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
class PaymentFailConsumerUnitTest {

    @InjectMocks
    PaymentFailConsumer paymentFailConsumer;
    @Mock
    PaymentService paymentService;
    @Mock
    Acknowledgment acknowledgment;
    @Mock
    Payment payment;

    String paymentKey;
    String code;
    String message;

    @BeforeEach
    void setUp() {
        paymentKey = "paymentKey";
        code = "code";
        message = "message";
    }

    @DisplayName("결제 실패 시 실패 정보를 저장한다.")
    @Test
    void handleFailedPayment() {
        // given
        PaymentFailedEvent event = new PaymentFailedEvent(paymentKey, code, message);
        when(paymentService.getByPaymentKey(paymentKey)).thenReturn(payment);
        doNothing().when(payment).fail(code, message);

        // when
        paymentFailConsumer.handleFailedPayment(event, acknowledgment);

        // then
        verify(paymentService).getByPaymentKey(paymentKey);
        verify(payment).fail(code, message);
        verify(acknowledgment).acknowledge();
    }
}
