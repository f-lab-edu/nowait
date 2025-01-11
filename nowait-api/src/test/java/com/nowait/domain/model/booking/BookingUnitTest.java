package com.nowait.domain.model.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
