package com.nowait.application;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nowait.application.event.PaymentSuccessEvent;
import com.nowait.domain.model.payment.Payment;
import com.nowait.domain.model.payment.PaymentStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
class PaymentSuccessConsumerUnitTest {

    @InjectMocks
    PaymentSuccessConsumer paymentSuccessConsumer;
    @Mock
    PaymentService paymentService;
    @Mock
    Acknowledgment acknowledgment;
    @Mock
    Payment payment;

    String paymentKey;
    int amount;
    Long bookingId;
    LocalDateTime approvedAt;

    @BeforeEach
    void setUp() {
        paymentKey = "paymentKey";
        amount = 20_000;
        bookingId = 1L;
        approvedAt = LocalDateTime.now();
    }

    @DisplayName("결제 성공 시 결제 정보를 저장한다.")
    @Test
    void handleSuccessPayment() {
        // given
        PaymentSuccessEvent event = new PaymentSuccessEvent(paymentKey, amount, bookingId,
            approvedAt);
        when(paymentService.getByPaymentKey(paymentKey)).thenReturn(payment);
        doNothing().when(payment).changeStatusTo(PaymentStatus.DONE);
        doNothing().when(acknowledgment).acknowledge();

        // when
        paymentSuccessConsumer.handleSuccessPayment(event, acknowledgment);

        // then
        verify(paymentService).getByPaymentKey(paymentKey);
        verify(payment).changeStatusTo(PaymentStatus.DONE);
        verify(acknowledgment).acknowledge();
    }
}
