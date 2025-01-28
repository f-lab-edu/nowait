package com.nowait.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.nowait.application.event.PaymentRequestedEvent;
import com.nowait.config.PaymentProperties;
import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.payment.Payment;
import com.nowait.domain.model.payment.PaymentStatus;
import com.nowait.domain.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

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
    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

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

    @Nested
    @DisplayName("결제 승인 테스트")
    class ApprovePaymentTest {

        @Mock
        Payment payment;
        @Mock
        Booking booking;

        Long loginId;
        Long paymentId;
        String paymentKey;
        Long bookingId;
        int amount;
        LocalDateTime requestAt;

        @BeforeEach
        void setUp() {
            loginId = 1L;
            paymentId = 1L;
            bookingId = 1L;
            paymentKey = "paymentKey";
            amount = 20_000;
            requestAt = LocalDateTime.of(2024, 12, 1, 0, 5);
        }

        @DisplayName("결제 승인 요청을 할 수 있다.")
        @Test
        void approve() {
            // given
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            when(payment.getBookingId()).thenReturn(bookingId);
            when(bookingService.getById(bookingId)).thenReturn(booking);
            doNothing().when(booking).validateOwner(loginId);
            when(payment.getBookingId()).thenReturn(bookingId);
            when(payment.getAmount()).thenReturn(amount);
            when(booking.isPaymentAvailable()).thenReturn(true);
            when(payment.getCreatedAt()).thenReturn(LocalDateTime.of(2024, 12, 1, 0, 0));
            when(property.approvalWaitMinutes()).thenReturn(5);
            doNothing().when(payment).setPaymentKey(paymentKey);
            when(kafkaTemplate.send(eq("payments.requested"), any())).thenReturn(null);
            doNothing().when(payment).changeStatusTo(PaymentStatus.IN_PROGRESS);

            // when
            paymentService.approve(loginId, paymentId, paymentKey, bookingId, amount, requestAt);

            // then
            verify(paymentRepository).findById(paymentId);
            verify(bookingService).getById(bookingId);
            verify(booking).validateOwner(loginId);
            verify(booking).isPaymentAvailable();
            verify(payment).getCreatedAt();
            verify(property).approvalWaitMinutes();
            verify(payment).setPaymentKey(paymentKey);
            verify(kafkaTemplate).send(eq("payments.requested"), any(PaymentRequestedEvent.class));
            verify(payment).changeStatusTo(PaymentStatus.IN_PROGRESS);
        }

        @DisplayName("예약자 본인이 아닌 경우 결제 승인을 할 수 없다.")
        @Test
        void approveNotOwner() {
            // given
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            when(payment.getBookingId()).thenReturn(bookingId);
            when(bookingService.getById(bookingId)).thenReturn(booking);
            doThrow(new IllegalArgumentException("예약자가 아닙니다.")).when(booking)
                .validateOwner(loginId);

            // when & then
            assertThatThrownBy(
                () -> paymentService.approve(loginId, paymentId, paymentKey, bookingId, amount,
                    requestAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약자가 아닙니다.");

            verifyNoInteractions(kafkaTemplate);
            verifyNoMoreInteractions(payment, booking);
        }

        @DisplayName("결제 정보가 일치하지 않는 경우 결제 승인을 할 수 없다.")
        @Test
        void approveNotMatch() {
            // given
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            when(payment.getBookingId()).thenReturn(bookingId);
            when(bookingService.getById(bookingId)).thenReturn(booking);
            doNothing().when(booking).validateOwner(loginId);
            when(payment.getAmount()).thenReturn(amount + 1_000);

            // when & then
            assertThatThrownBy(
                () -> paymentService.approve(loginId, paymentId, paymentKey, bookingId, amount,
                    requestAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 정보가 일치하지 않습니다.");

            verifyNoInteractions(kafkaTemplate);
            verifyNoMoreInteractions(payment, booking);
        }

        @DisplayName("결제가 이미 완료된 경우 결제 승인을 할 수 없다.")
        @Test
        void approveCompletedPayment() {
            // given
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            when(payment.getBookingId()).thenReturn(bookingId);
            when(bookingService.getById(bookingId)).thenReturn(booking);
            doNothing().when(booking).validateOwner(loginId);
            when(payment.getBookingId()).thenReturn(bookingId);
            when(payment.getAmount()).thenReturn(amount);
            when(booking.isPaymentAvailable()).thenReturn(false);

            // when & then
            assertThatThrownBy(
                () -> paymentService.approve(loginId, paymentId, paymentKey, bookingId, amount,
                    requestAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제할 수 없는 예약입니다.");

            verifyNoInteractions(kafkaTemplate);
            verifyNoMoreInteractions(payment, booking);
        }

        @DisplayName("결제 승인 대기 시간(5분)이 지난 경우 결제 승인을 할 수 없다.")
        @Test
        void approveExpired() {
            // given
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            when(payment.getBookingId()).thenReturn(bookingId);
            when(bookingService.getById(bookingId)).thenReturn(booking);
            doNothing().when(booking).validateOwner(loginId);
            when(payment.getBookingId()).thenReturn(bookingId);
            when(payment.getAmount()).thenReturn(amount);
            when(booking.isPaymentAvailable()).thenReturn(true);
            when(payment.getCreatedAt()).thenReturn(LocalDateTime.of(2024, 12, 1, 0, 0));
            when(property.approvalWaitMinutes()).thenReturn(5);

            LocalDateTime requestAt = LocalDateTime.of(2024, 12, 1, 0, 6);

            // when & then
            assertThatThrownBy(
                () -> paymentService.approve(loginId, paymentId, paymentKey, bookingId, amount,
                    requestAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 승인 대기 시간이 지났습니다.");

            verifyNoInteractions(kafkaTemplate);
            verifyNoMoreInteractions(payment, booking);
        }
    }

    @Nested
    @DisplayName("결제 키(paymentKey)로 결제를 조회할 수 있다.")
    class GetByPaymentKeyTest {

        @Mock
        Payment payment;
        String paymentKey;

        @BeforeEach
        void setUp() {
            paymentKey = "paymentKey";
        }

        @DisplayName("결제 키로 결제를 조회할 수 있다.")
        @Test
        void getByPaymentKey() {
            // given
            when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(Optional.of(payment));

            // when
            Payment result = paymentService.getByPaymentKey(paymentKey);

            // then
            assertThat(result).isEqualTo(payment);
        }

        @DisplayName("해당 결제 키를 가진 결제가 없다면, 예외를 발생시킨다.")
        @Test
        void getByPaymentKeyNotFound() {
            // given
            when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.getByPaymentKey(paymentKey))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("결제 정보가 존재하지 않습니다.");
        }
    }
}
