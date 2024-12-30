package com.nowait.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nowait.application.dto.response.payment.PaymentInfo;
import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.booking.BookingSlot;
import com.nowait.domain.model.booking.DepositPolicy;
import com.nowait.domain.model.payment.Payment;
import com.nowait.domain.model.payment.PaymentType;
import com.nowait.domain.repository.PaymentRepository;
import com.nowait.exception.PaymentApprovalException;
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
    PaymentRepository paymentRepository;

    @Mock
    PaymentGatewayFactory paymentGatewayFactory;

    @Mock
    BookingService bookingService;

    @Nested
    @DisplayName("예약금 결제 준비 테스트")
    class PrepareDepositPaymentTest {

        @Mock
        Booking booking;

        @Mock
        BookingSlot bookingSlot;

        @Mock
        DepositPolicy depositPolicy;

        @Mock
        PaymentGateway paymentGateway;

        @Mock
        Payment payment;

        @Mock
        PaymentInfo paymentInfo;

        int amount;
        Long loginId;
        Long bookingId;
        PaymentType paymentType;
        LocalDateTime requestTime;

        @BeforeEach
        void setUp() {
            amount = 20_000;
            loginId = 1L;
            bookingId = 1L;
            paymentType = PaymentType.KAKAO_PAY;
            requestTime = LocalDateTime.of(2024, 12, 1, 14, 0);
        }

        @DisplayName("결제 준비 요청을 할 수 있다.")
        @Test
        void prepare() {
            // given
            when(bookingService.getById(anyLong())).thenReturn(booking);
            when(bookingService.getBookingSlotById(anyLong())).thenReturn(bookingSlot);
            when(bookingService.getDepositPolicyById(anyLong())).thenReturn(depositPolicy);
            when(depositPolicy.getDepositAmount(booking)).thenReturn(amount);
            doNothing().when(booking).validateOwner(loginId);
            when(booking.isPaymentAvailable()).thenReturn(true);
            when(booking.getCreatedAt()).thenReturn(LocalDateTime.of(2024, 12, 1, 12, 0));
            when(paymentGatewayFactory.createPaymentGateway(paymentType)).thenReturn(
                paymentGateway);
            when(paymentGateway.prepare(loginId, booking, amount)).thenReturn(paymentInfo);
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
            when(booking.getId()).thenReturn(bookingId);
            when(paymentInfo.tid()).thenReturn("T76cf8a7222b2ae7bb99");
            when(paymentInfo.createdAt()).thenReturn(LocalDateTime.of(2024, 12, 1, 12, 0));
            when(payment.getId()).thenReturn(1L);
            when(paymentInfo.redirectPcUrl()).thenReturn(any(String.class));

            // when
            paymentService.prepare(loginId, bookingId, paymentType, amount, requestTime);

            // then
            verify(bookingService).getById(bookingId);
            verify(bookingService).getBookingSlotById(booking.getBookingSlotId());
            verify(bookingService).getDepositPolicyById(bookingSlot.getDepositPolicyId());
            verify(depositPolicy).getDepositAmount(booking);
            verify(booking).validateOwner(loginId);
            verify(booking).getCreatedAt();
            verify(paymentGatewayFactory).createPaymentGateway(paymentType);
            verify(paymentGateway).prepare(loginId, booking, amount);
            verify(paymentRepository).save(any(Payment.class));
        }

        @DisplayName("결제 요청 금액이 실제 예약 금액과 일치하지 않는 경우, 결제 준비를 할 수 없다.")
        @Test
        void prepareWithWrongDepositAmount() {
            // given
            when(bookingService.getById(anyLong())).thenReturn(booking);
            when(bookingService.getBookingSlotById(anyLong())).thenReturn(bookingSlot);
            when(bookingService.getDepositPolicyById(anyLong())).thenReturn(depositPolicy);
            when(depositPolicy.getDepositAmount(booking)).thenReturn(amount);

            // when
            assertThatThrownBy(
                () -> paymentService.prepare(loginId, bookingId, paymentType, 1_000_000,
                    requestTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약금 금액이 일치하지 않습니다.");

            // then
            verify(bookingService).getById(bookingId);
            verify(bookingService).getBookingSlotById(booking.getBookingSlotId());
            verify(bookingService).getDepositPolicyById(bookingSlot.getDepositPolicyId());
            verify(depositPolicy).getDepositAmount(booking);

            verifyNoInteractions(paymentGatewayFactory);
            verifyNoInteractions(paymentGateway);
            verifyNoInteractions(paymentRepository);
            verifyNoInteractions(paymentInfo);
            verifyNoInteractions(payment);
        }

        @DisplayName("이미 결제가 완료된 예약인 경우, 결제 준비를 할 수 없다.")
        @Test
        void preparePaymentCompletedDeposit() {
            // given
            when(bookingService.getById(anyLong())).thenReturn(booking);
            when(bookingService.getBookingSlotById(anyLong())).thenReturn(bookingSlot);
            when(bookingService.getDepositPolicyById(anyLong())).thenReturn(depositPolicy);
            when(depositPolicy.getDepositAmount(booking)).thenReturn(amount);

            // when
            assertThatThrownBy(
                () -> paymentService.prepare(loginId, bookingId, paymentType, amount, requestTime))
                .isInstanceOf(PaymentApprovalException.class)
                .hasMessage("결제할 수 없는 예약입니다.");

            // then
            verify(bookingService).getById(bookingId);
            verify(bookingService).getBookingSlotById(booking.getBookingSlotId());
            verify(bookingService).getDepositPolicyById(bookingSlot.getDepositPolicyId());
            verify(depositPolicy).getDepositAmount(booking);
            verify(booking).validateOwner(loginId);

            verifyNoInteractions(paymentGatewayFactory);
            verifyNoInteractions(paymentGateway);
            verifyNoInteractions(paymentInfo);
            verifyNoInteractions(payment);
        }

        @DisplayName("예약 후 2시간 이내에 결제를 시도하지 않는 경우, 예약이 취소된다.")
        @Test
        void prepareDepositPaymentAfter2Hours() {
            // given
            when(bookingService.getById(anyLong())).thenReturn(booking);
            when(bookingService.getBookingSlotById(anyLong())).thenReturn(bookingSlot);
            when(bookingService.getDepositPolicyById(anyLong())).thenReturn(depositPolicy);
            when(depositPolicy.getDepositAmount(booking)).thenReturn(amount);
            doNothing().when(booking).validateOwner(loginId);
            when(booking.isPaymentAvailable()).thenReturn(true);
            when(booking.getCreatedAt()).thenReturn(LocalDateTime.of(2024, 12, 1, 12, 0));

            LocalDateTime requestTime = LocalDateTime.of(2024, 12, 1, 14, 1);

            // when & the
            assertThatThrownBy(
                () -> paymentService.prepare(loginId, bookingId, paymentType, amount, requestTime))
                .isInstanceOf(PaymentApprovalException.class)
                .hasMessage("결제 대기 시간이 지났습니다.");
        }
    }


}
