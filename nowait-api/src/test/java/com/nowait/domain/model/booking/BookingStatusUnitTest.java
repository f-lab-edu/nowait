package com.nowait.domain.model.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingStatusUnitTest {

    @Mock
    BookingSlot slot;

    @Nested
    @DisplayName("예약 후 상태 조회 테스트")
    class GetStatusAfterBooking {

        @DisplayName("예약금이 필요한 경우, 예약 후 상태는 결제 대기 중이다.")
        @Test
        void getStatusAfterBookingPaymentRequired() {
            // given
            when(slot.isDepositRequired()).thenReturn(true);

            // when
            BookingStatus status = BookingStatus.getStatusAfterBooking(slot);

            // then
            assertThat(status).isEqualTo(BookingStatus.PENDING_PAYMENT);
        }

        @DisplayName("가게의 예약 확정이 필요한 경우, 예약 후 상태는 확정 대기 중이다.")
        @Test
        void getStatusAfterBookingConfirmRequired() {
            // given
            when(slot.isDepositRequired()).thenReturn(false);
            when(slot.isConfirmRequired()).thenReturn(true);

            // when
            BookingStatus status = BookingStatus.getStatusAfterBooking(slot);

            // then
            assertThat(status).isEqualTo(BookingStatus.PENDING_CONFIRM);
        }

        @DisplayName("예약금과 가게의 예약 확정이 모두 필요한 경우, 예약 후 상태는 결제 대기 중이다.")
        @Test
        void getStatusAfterBookingPaymentAndConfirmRequired() {
            // given
            when(slot.isDepositRequired()).thenReturn(true);
            lenient().when(slot.isConfirmRequired()).thenReturn(true);

            // when
            BookingStatus status = BookingStatus.getStatusAfterBooking(slot);

            // then
            assertThat(status).isEqualTo(BookingStatus.PENDING_PAYMENT);
        }

        @DisplayName("예약금과 가게의 예약 확정이 모두 필요하지 않은 경우, 예약 후 상태는 확정됨이다.")
        @Test
        void getStatusAfterBookNoPaymentNoConfirm() {
            // given
            when(slot.isDepositRequired()).thenReturn(false);
            when(slot.isConfirmRequired()).thenReturn(false);

            // when
            BookingStatus status = BookingStatus.getStatusAfterBooking(slot);

            // then
            assertThat(status).isEqualTo(BookingStatus.CONFIRMED);
        }
    }

    @Nested
    @DisplayName("다음 상태 조회 테스트")
    class Next {

        @Mock
        BookingSlot slot;

        @DisplayName("예약이 확정되면 예약 상태는 '확정됨'이다.")
        @Test
        void nextAfterConfirm() {
            // given
            BookingStatus status = BookingStatus.PENDING_CONFIRM;

            // when
            BookingStatus nextStatus = BookingStatus.next(status, slot);

            // then
            assertThat(nextStatus).isEqualTo(BookingStatus.CONFIRMED);
        }

        @DisplayName("결제가 완료되면, 예약 확정이 필요한 경우 예약 상태는 '확정 대기 중'이다.")
        @Test
        void nextAfterPaymentWhenConfirmRequired() {
            // given
            BookingStatus status = BookingStatus.PENDING_PAYMENT;
            when(slot.isConfirmRequired()).thenReturn(true);

            // when
            BookingStatus nextStatus = BookingStatus.next(status, slot);

            // then
            assertThat(nextStatus).isEqualTo(BookingStatus.PENDING_CONFIRM);
            verify(slot).isConfirmRequired();
        }

        @DisplayName("결제가 완료되면 예약 확정이 필요하지 않은 경우 예약 상태는 '확정됨'이다.")
        @Test
        void nextAfterPaymentWhenNoConfirmRequired() {
            // given
            BookingStatus status = BookingStatus.PENDING_PAYMENT;
            when(slot.isConfirmRequired()).thenReturn(false);

            // when
            BookingStatus nextStatus = BookingStatus.next(status, slot);

            // then
            assertThat(nextStatus).isEqualTo(BookingStatus.CONFIRMED);
        }

        @DisplayName("예약 취소 상태에서는 현재 상태를 유지한다.")
        @Test
        void nextAfterCancel() {
            // given
            BookingStatus status = BookingStatus.CANCELLED;

            // when
            BookingStatus nextStatus = BookingStatus.next(status, slot);

            // then
            assertThat(nextStatus).isEqualTo(BookingStatus.CANCELLED);
        }

        @DisplayName("예약 확정 상태에서는 현재 상태를 유지한다.")
        @Test
        void nextAfterConfirmed() {
            // given
            BookingStatus status = BookingStatus.CONFIRMED;

            // when
            BookingStatus nextStatus = BookingStatus.next(status, slot);

            // then
            assertThat(nextStatus).isEqualTo(BookingStatus.CONFIRMED);
        }
    }
}
