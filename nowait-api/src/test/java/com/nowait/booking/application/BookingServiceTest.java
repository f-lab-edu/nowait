package com.nowait.booking.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nowait.booking.domain.model.Booking;
import com.nowait.booking.domain.model.BookingSlot;
import com.nowait.booking.domain.model.BookingStatus;
import com.nowait.booking.domain.repository.BookingRepository;
import com.nowait.booking.domain.repository.BookingSlotRepository;
import com.nowait.booking.dto.response.DailyBookingStatusRes;
import com.nowait.place.domain.repository.PlaceRepository;
import com.nowait.user.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @InjectMocks
    BookingService bookingService;

    @Mock
    BookingSlotRepository bookingSlotRepository;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    PlaceRepository placeRepository;

    @Mock
    BookingEventPublisher bookingEventPublisher;

    @Nested
    @DisplayName("예약 현황 조회 테스트")
    class GetDailyBookingStatusTest {

        long placeId;
        LocalDate date;

        @Mock
        BookingSlot slotAt18;
        @Mock
        BookingSlot unavailableSlotAt18;
        @Mock
        BookingSlot unavailableSlotAt19;

        @BeforeEach
        void setUp() {
            placeId = 1L;
            date = LocalDate.of(2024, 12, 25);
        }

        @DisplayName("특정 날짜에 예약 가능 시간과 예약 상태를 확인할 수 있다")
        @Test
        void getDailyBookingStatus() {
            // given
            List<BookingSlot> bookingSlots = List.of(slotAt18, unavailableSlotAt19);

            when(bookingSlotRepository.findAllByPlaceIdAndDate(any(Long.class),
                any(LocalDate.class))).thenReturn(bookingSlots);

            when(slotAt18.getTime()).thenReturn(LocalTime.of(18, 0));
            when(slotAt18.isBooked()).thenReturn(false);

            when(unavailableSlotAt19.getTime()).thenReturn(LocalTime.of(19, 0));
            when(unavailableSlotAt19.isBooked()).thenReturn(true);

            // when
            DailyBookingStatusRes response = bookingService.getDailyBookingStatus(placeId, date);

            // then
            assertThat(response.timeList()).hasSize(2);
            assertThat(response.timeList().get(0).time()).isEqualTo(LocalTime.of(18, 0));
            assertThat(response.timeList().get(0).available()).isTrue();
            assertThat(response.timeList().get(1).time()).isEqualTo(LocalTime.of(19, 0));
            assertThat(response.timeList().get(1).available()).isFalse();
        }

        @DisplayName("해당 시간의 모든 테이블이 예약된 경우에만 예약 불가능한 상태가 된다")
        @Test
        void getDailyBookingStatus2() {
            // given
            List<BookingSlot> bookingSlots = List.of(
                unavailableSlotAt18, slotAt18, unavailableSlotAt19, unavailableSlotAt19);

            when(bookingSlotRepository.findAllByPlaceIdAndDate(any(Long.class),
                any(LocalDate.class))).thenReturn(bookingSlots);

            when(slotAt18.getTime()).thenReturn(LocalTime.of(18, 0));
            when(slotAt18.isBooked()).thenReturn(false);

            when(unavailableSlotAt18.getTime()).thenReturn(LocalTime.of(18, 0));
            when(unavailableSlotAt18.isBooked()).thenReturn(true);

            when(unavailableSlotAt19.getTime()).thenReturn(LocalTime.of(19, 0));
            when(unavailableSlotAt19.isBooked()).thenReturn(true);

            // when
            DailyBookingStatusRes response = bookingService.getDailyBookingStatus(placeId, date);

            // then
            assertThat(response.timeList()).hasSize(2);
            assertThat(response.timeList().get(0).time()).isEqualTo(LocalTime.of(18, 0));
            assertThat(response.timeList().get(0).available()).isTrue();
            assertThat(response.timeList().get(1).time()).isEqualTo(LocalTime.of(19, 0));
            assertThat(response.timeList().get(1).available()).isFalse();
        }
    }

    @Nested
    @DisplayName("예약 테스트")
    class BookingTest {

        @Mock
        BookingSlot slot;
        @Mock
        Booking booking;
        Long loginId;
        Long placeId;
        LocalDate date;
        LocalTime time;
        int partySize;

        @BeforeEach
        void setUp() {
            loginId = 1L;
            placeId = 1L;
            date = LocalDate.of(2024, 12, 25);
            time = LocalTime.of(18, 0);
            partySize = 2;
        }

        @DisplayName("예약 가능한 날짜, 시간에 예약을 할 수 있다.")
        @Test
        void book() {
            // given
            when(userRepository.existsById(loginId)).thenReturn(true);
            when(placeRepository.existsById(placeId)).thenReturn(true);
            when(bookingSlotRepository.findFirstByPlaceIdAndDateAndTimeAndIsBookedFalse(
                placeId, date, time)).thenReturn(Optional.of(slot));
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(slot.book()).thenReturn(BookingStatus.CONFIRMED);
            when(booking.getStatus()).thenReturn(BookingStatus.CONFIRMED);

            // when
            bookingService.book(loginId, placeId, date, time, partySize);

            // then
            verify(userRepository).existsById(loginId);
            verify(placeRepository).existsById(placeId);
            verify(bookingSlotRepository).findFirstByPlaceIdAndDateAndTimeAndIsBookedFalse(
                placeId, date, time);
            verify(bookingRepository).save(any(Booking.class));
            verify(bookingEventPublisher).publishBookedEvent(booking, placeId);
        }

        @DisplayName("해당 시간대의 모든 테이블이 이미 예약된 경우에는 예약을 할 수 없다")
        @Test
        void alreadyFullyBooked() {
            // given
            when(userRepository.existsById(loginId)).thenReturn(true);
            when(placeRepository.existsById(placeId)).thenReturn(true);
            when(bookingSlotRepository.findFirstByPlaceIdAndDateAndTimeAndIsBookedFalse(
                placeId, date, time)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bookingService.book(loginId, placeId, date, time, partySize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 가능한 테이블이 없습니다.");

            verify(userRepository).existsById(loginId);
            verify(placeRepository).existsById(placeId);
            verify(bookingSlotRepository).findFirstByPlaceIdAndDateAndTimeAndIsBookedFalse(
                placeId, date, time);
            verifyNoInteractions(bookingRepository, bookingEventPublisher);
        }

        @DisplayName("서비스 가입자만 예약을 할 수 있다.")
        @Test
        void bookByNonExistUser() {
            // given
            when(userRepository.existsById(loginId)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> bookingService.book(loginId, placeId, date, time, partySize))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 사용자의 요청입니다.");

            verify(userRepository).existsById(loginId);
            verifyNoInteractions(placeRepository, bookingSlotRepository, bookingRepository,
                bookingEventPublisher);
        }

        @DisplayName("존재하지 않는 식당에는 예약을 할 수 없다.")
        @Test
        void bookWithNonExistPlace() {
            // given
            when(userRepository.existsById(loginId)).thenReturn(true);
            when(placeRepository.existsById(placeId)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> bookingService.book(loginId, placeId, date, time, partySize))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 식당입니다.");

            verify(userRepository).existsById(loginId);
            verify(placeRepository).existsById(placeId);
            verifyNoInteractions(bookingSlotRepository, bookingRepository, bookingEventPublisher);
        }
    }
}
