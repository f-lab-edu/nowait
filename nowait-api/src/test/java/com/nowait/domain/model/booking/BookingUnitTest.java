package com.nowait.domain.model.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingUnitTest {

    @Mock
    BookingSlot slot;
    Long userId;
    Long bookingSlotId;
    Integer partySize;

    @BeforeEach
    void setUp() {
        userId = 1L;
        bookingSlotId = 1L;
        partySize = 2;
    }

    @Nested
    @DisplayName("예약 생성 테스트")
    class CreatePaymentTest {

        @DisplayName("예약을 생성한다.")
        @Test
        void makeBook() {
            try (MockedStatic<BookingStatus> mockedStatic = mockStatic(BookingStatus.class)) {
                // given
                when(slot.getId()).thenReturn(bookingSlotId);
                mockedStatic.when(() -> BookingStatus.getStatusAfterBooking(slot))
                    .thenReturn(BookingStatus.CONFIRMED);

                // when
                Booking booking = Booking.of(userId, slot, partySize);

                // then
                assertThat(booking.getBookingSlotId()).isEqualTo(bookingSlotId);
                assertThat(booking.getUserId()).isEqualTo(userId);
                assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
                assertThat(booking.getPartySize()).isEqualTo(partySize);

                verify(slot).book();
                mockedStatic.verify(() -> BookingStatus.getStatusAfterBooking(slot));
            }
        }

        @DisplayName("예약자 없이 예약을 생성할 수 없다.")
        @Test
        void makeBookWithoutUserId() {
            // when & then
            assertThatThrownBy(() -> Booking.of(null, slot, partySize))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("예약자 식별자는 필수값입니다.");

            verifyNoInteractions(slot);
        }

        @DisplayName("예약 슬롯 없이 예약을 생성할 수 없다.")
        @Test
        void makeBookWithoutSlot() {
            // when & then
            assertThatThrownBy(() -> Booking.of(userId, null, partySize))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("예약 슬롯은 필수값입니다.");

            verifyNoInteractions(slot);
        }

        @DisplayName("예약 인원 정보가 없으면 1명으로 예약을 생성한다.")
        @Test
        void makeBookWithoutPartySize() {

            try (MockedStatic<BookingStatus> mockedStatic = mockStatic(BookingStatus.class)) {
                // given
                when(slot.getId()).thenReturn(bookingSlotId);
                mockedStatic.when(() -> BookingStatus.getStatusAfterBooking(slot))
                    .thenReturn(BookingStatus.CONFIRMED);

                // when
                Booking booking = Booking.of(userId, slot, null);

                // then
                assertThat(booking.getPartySize()).isEqualTo(1);
                verify(slot).book();
            }
        }

        @DisplayName("예약 인원이 0명 이하인 경우 예약을 생성할 수 없다.")
        @Test
        void makeBookWithInvalidPartySize() {
            // when & then
            assertThatThrownBy(() -> Booking.of(userId, slot, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인원 수는 0이상 입니다.");

            verifyNoInteractions(slot);
        }
    }

    @Nested
    @DisplayName("예약자 검증 테스트")
    class ValidateOwnerTest {

        @DisplayName("사용자가 예약자인지 확인할 수 있다.")
        @Test
        void validateOwner() {
            // given
            Booking booking = Booking.of(userId, slot, partySize);

            // when & then
            assertDoesNotThrow(() -> booking.validateOwner(userId));
        }

        @DisplayName("사용자가 예약자가 아닌 경우 예외를 발생시킨다.")
        @Test
        void validateOwnerWithInvalidUser() {
            // given
            Booking booking = Booking.of(userId, slot, partySize);

            // when & then
            assertThatThrownBy(() -> booking.validateOwner(userId + 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약자가 아닙니다.");
        }
    }

    @Nested
    @DisplayName("예약 가능 확인 테스트")
    class CheckPaymentAvailableTest {

        @DisplayName("결제 가능한 예약인지 확인할 수 있다.")
        @Test
        void isPaymentAvailable() {
            try (MockedStatic<BookingStatus> mockedStatic = mockStatic(BookingStatus.class)) {
                // given
                mockedStatic.when(() -> BookingStatus.getStatusAfterBooking(slot))
                    .thenReturn(BookingStatus.PENDING_PAYMENT);
                Booking booking = Booking.of(userId, slot, partySize);

                // when
                boolean result = booking.isPaymentAvailable();

                // then
                assertThat(result).isTrue();
            }
        }
    }


    @Nested
    @DisplayName("결제 완료 테스트")
    class CompletePaymentTest {

        @DisplayName("결제 완료 후 예약 상태를 변경할 수 있다.")
        @Test
        void completePayment() {
            try (MockedStatic<BookingStatus> mockedStatic = mockStatic(BookingStatus.class)) {
                // given
                mockedStatic.when(() -> BookingStatus.getStatusAfterBooking(slot))
                    .thenReturn(BookingStatus.PENDING_PAYMENT);
                Booking booking = Booking.of(userId, slot, partySize);

                // when
                booking.completePayment(slot);

                // then
                assertThat(booking.getStatus()).isNotEqualTo(BookingStatus.PENDING_PAYMENT);
            }
        }

        @DisplayName("이미 결제가 완료된 상태에서는 결제를 완료할 수 없다.")
        @Test
        void completePaymentWhenAlreadyPaid() {
            try (MockedStatic<BookingStatus> mockedStatic = mockStatic(BookingStatus.class)) {
                // given
                mockedStatic.when(() -> BookingStatus.getStatusAfterBooking(slot))
                    .thenReturn(BookingStatus.CONFIRMED);
                Booking booking = Booking.of(userId, slot, partySize);

                // when & then
                assertThatThrownBy(() -> booking.completePayment(slot))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("결제할 수 없는 예약입니다.");
            }
        }
    }
}
