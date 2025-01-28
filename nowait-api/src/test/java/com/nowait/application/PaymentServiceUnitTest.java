package com.nowait.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nowait.config.PaymentProperties;
import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.payment.Payment;
import com.nowait.domain.repository.PaymentRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceUnitTest {

    @InjectMocks
    PaymentService paymentService;
    @Mock
    PaymentProperties property;
    @Mock
    PaymentRepository paymentRepository;
    @Mock
    BookingService bookingService;
    @Mock
    DepositService depositService;

    @Nested
    @DisplayName("결제 준비 테스트")
    class ReadPaymentTest {

        @Mock
        Booking booking;
        @Mock
        Payment payment;

        Long loginId;
        Long bookingId;
        int amount;
        LocalDateTime requestAt;
        String token;

        @BeforeEach
        void setUp() {
            amount = 20_000;
            loginId = 1L;
            bookingId = 1L;
            requestAt = LocalDateTime.of(2024, 12, 1, 12, 5);
        }

        @DisplayName("결제 준비 요청을 할 수 있다.")
        @Test
        void ready() {
            // given
            when(bookingService.getById(anyLong())).thenReturn(booking);
            doNothing().when(booking).validateOwner(loginId);
            doNothing().when(depositService).validateDepositAmount(booking, amount);
            when(booking.isPaymentAvailable()).thenReturn(true);
            when(booking.getCreatedAt()).thenReturn(LocalDateTime.of(2024, 12, 1, 12, 0));
            when(property.depositPaymentWaitMinutes()).thenReturn(5);
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

            // when
            paymentService.ready(loginId, bookingId, amount, requestAt);

            // then
            verify(bookingService).getById(bookingId);
            verify(booking).validateOwner(loginId);
            verify(depositService).validateDepositAmount(booking, amount);
            verify(booking).isPaymentAvailable();
            verify(booking).getCreatedAt();
            verify(property).depositPaymentWaitMinutes();
            verify(paymentRepository).save(any(Payment.class));
        }

        @DisplayName("이미 결제가 완료된 예약인 경우, 결제 준비를 할 수 없다.")
        @Test
        void readyCompletedDeposit() {
            // given
            when(bookingService.getById(anyLong())).thenReturn(booking);
            doNothing().when(booking).validateOwner(loginId);
            doNothing().when(depositService).validateDepositAmount(booking, amount);
            when(booking.isPaymentAvailable()).thenReturn(false);

            // when
            assertThatThrownBy(
                () -> paymentService.ready(loginId, bookingId, amount, requestAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제할 수 없는 예약입니다.");

            // then
            verify(bookingService).getById(bookingId);
            verify(booking).validateOwner(loginId);
            verify(depositService).validateDepositAmount(booking, amount);
            verify(booking).isPaymentAvailable();

            verifyNoInteractions(paymentRepository);
        }

        @DisplayName("예약 후 2시간 이내에 결제를 시도하지 않는 경우, 예약이 취소되어 결제를 할 수 없다.")
        @Test
        void readyPaymentAfter2Hours() {
            // given
            when(bookingService.getById(anyLong())).thenReturn(booking);
            doNothing().when(booking).validateOwner(loginId);
            doNothing().when(depositService).validateDepositAmount(booking, amount);
            when(booking.isPaymentAvailable()).thenReturn(true);
            when(booking.getCreatedAt()).thenReturn(LocalDateTime.of(2024, 12, 1, 12, 0));
            when(property.depositPaymentWaitMinutes()).thenReturn(2);

            LocalDateTime requestTime = LocalDateTime.of(2024, 12, 1, 14, 1);

            // when & the
            assertThatThrownBy(
                () -> paymentService.ready(loginId, bookingId, amount, requestTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 대기 시간이 지났습니다.");

            verifyNoInteractions(paymentRepository);
        }
    }
}
