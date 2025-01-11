package com.nowait.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nowait.application.dto.response.booking.DailyBookingStatusRes;
import com.nowait.application.event.BookingEventPublisher;
import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.booking.BookingSlot;
import com.nowait.domain.model.booking.BookingStatus;
import com.nowait.domain.repository.BookingRepository;
import com.nowait.domain.repository.BookingSlotRepository;
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
class BookingServiceUnitTest {

    @InjectMocks
    BookingService bookingService;

    @Mock
    BookingSlotRepository bookingSlotRepository;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    BookingEventPublisher bookingEventPublisher;

    @Mock
    UserService userService;

    @Mock
    PlaceService placeService;

    @Nested
    @DisplayName("예약 현황 조회 테스트")
    class GetDailyBookingStatusTest {

        long placeId;
        LocalDate date;
        @Mock
        BookingSlot slotAt18;
        @Mock
        BookingSlot slotAt19;
        @Mock
        Booking bookingAt18;
        @Mock
        Booking bookingAt19;

        @BeforeEach
        void setUp() {
            placeId = 1L;
            date = LocalDate.of(2024, 12, 25);
        }

        @DisplayName("특정 날짜에 예약 가능 시간과 예약 상태를 확인할 수 있다")
        @Test
        void getDailyBookingStatus() {
            // given
            when(bookingSlotRepository.findAllByPlaceIdAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(List.of(slotAt18, slotAt19));

            when(slotAt18.getTime()).thenReturn(LocalTime.of(18, 0));
            when(slotAt18.getId()).thenReturn(1L);
            when(bookingRepository.findAllByBookingSlotId(slotAt18.getId())).thenReturn(List.of());
            when(slotAt18.getCount()).thenReturn(1);

            when(slotAt19.getTime()).thenReturn(LocalTime.of(19, 0));
            when(slotAt19.getId()).thenReturn(2L);
            when(bookingRepository.findAllByBookingSlotId(slotAt19.getId())).thenReturn(
                List.of(bookingAt19));
            when(bookingAt19.getStatus()).thenReturn(BookingStatus.CONFIRMED);
            when(slotAt19.getCount()).thenReturn(1);

            // when
            DailyBookingStatusRes response = bookingService.getDailyBookingStatus(placeId, date);

            // then
            assertThat(response.timeList()).hasSize(2);
            assertThat(response.timeList().get(0).time()).isEqualTo(LocalTime.of(18, 0));
            assertThat(response.timeList().get(0).available()).isTrue();
            assertThat(response.timeList().get(1).time()).isEqualTo(LocalTime.of(19, 0));
            assertThat(response.timeList().get(1).available()).isFalse();

            verify(bookingSlotRepository).findAllByPlaceIdAndDate(placeId, date);
            verify(bookingRepository).findAllByBookingSlotId(slotAt18.getId());
            verify(bookingRepository).findAllByBookingSlotId(slotAt19.getId());
        }

        @DisplayName("해당 시간의 모든 테이블이 예약된 경우에만 예약 불가능한 상태가 된다")
        @Test
        void getDailyBookingStatus2() {
            // given
            when(bookingSlotRepository.findAllByPlaceIdAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(List.of(slotAt18));

            when(slotAt18.getTime()).thenReturn(LocalTime.of(18, 0));
            when(slotAt18.getId()).thenReturn(1L);
            when(bookingRepository.findAllByBookingSlotId(slotAt18.getId()))
                .thenReturn(List.of(bookingAt18));
            when(slotAt18.getCount()).thenReturn(2);

            // when
            DailyBookingStatusRes response = bookingService.getDailyBookingStatus(placeId, date);

            // then
            assertThat(response.timeList()).hasSize(1);
            assertThat(response.timeList().get(0).time()).isEqualTo(LocalTime.of(18, 0));
            assertThat(response.timeList().get(0).available()).isTrue();

            verify(bookingSlotRepository).findAllByPlaceIdAndDate(placeId, date);
            verify(bookingRepository).findAllByBookingSlotId(slotAt18.getId());
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
            when(userService.existsById(loginId)).thenReturn(true);
            when(slot.getId()).thenReturn(1L);
            when(bookingRepository.findAllByBookingSlotId(slot.getId())).thenReturn(List.of());
            when(slot.getCount()).thenReturn(1);
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(slot.getPlaceId()).thenReturn(placeId);

            // when
            bookingService.book(loginId, slot, partySize);

            // then
            verify(userService).existsById(loginId);
            verify(bookingRepository).findAllByBookingSlotId(slot.getId());
            verify(bookingRepository).save(any(Booking.class));
            verify(bookingEventPublisher).publishBookedEvent(booking, placeId);
        }

        @DisplayName("해당 시간대에 이미 예약이 다 찬 경우 예약을 할 수 없다")
        @Test
        void alreadyFullyBooked() {
            // given
            when(userService.existsById(loginId)).thenReturn(true);
            when(slot.getId()).thenReturn(1L);
            when(bookingRepository.findAllByBookingSlotId(slot.getId())).thenReturn(
                List.of(booking));
            when(booking.getStatus()).thenReturn(BookingStatus.CONFIRMED);
            when(slot.getCount()).thenReturn(1);

            // when & then
            assertThatThrownBy(() -> bookingService.book(loginId, slot, partySize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 가능한 테이블이 없습니다.");

            verify(userService).existsById(loginId);
            verify(bookingRepository).findAllByBookingSlotId(slot.getId());
            verifyNoInteractions(bookingEventPublisher);
        }

        @DisplayName("서비스 가입자만 예약을 할 수 있다.")
        @Test
        void bookByNonExistUser() {
            // given
            when(userService.existsById(loginId)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> bookingService.book(loginId, slot, partySize))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 사용자의 요청입니다.");

            verify(userService).existsById(loginId);
            verifyNoInteractions(bookingSlotRepository, bookingRepository, bookingEventPublisher);
        }
    }

    @Nested
    @DisplayName("예약 슬롯 조회 테스트")
    class GetSlotBy {

        @Mock
        BookingSlot slot;
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

        @DisplayName("식당 식별자, 날짜, 시간으로 예약 슬롯을 조회할 수 있다.")
        @Test
        void getSlotBy() {
            // given
            when(placeService.existsById(placeId)).thenReturn(true);
            when(bookingSlotRepository.findByPlaceIdAndDateAndTime(placeId, date, time))
                .thenReturn(Optional.of(slot));

            // when
            BookingSlot result = bookingService.getSlotBy(placeId, date, time);

            // then
            assertThat(result).isEqualTo(slot);

            verify(placeService).existsById(placeId);
            verify(bookingSlotRepository).findByPlaceIdAndDateAndTime(placeId, date, time);
        }

        @DisplayName("존재하지 않는 식당 식별자로 예약 슬롯을 조회할 수 없다.")
        @Test
        void getSlotByWithNonExistPlace() {
            // given
            when(placeService.existsById(placeId)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> bookingService.getSlotBy(placeId, date, time))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 식당입니다.");

            verify(placeService).existsById(placeId);
            verifyNoInteractions(bookingSlotRepository);
        }

        @DisplayName("해당 시간대의 예약 슬롯이 없는 경우에는 예약 슬롯을 조회할 수 없다.")
        @Test
        void getNonExistSlot() {
            // given
            when(placeService.existsById(placeId)).thenReturn(true);
            when(bookingSlotRepository.findByPlaceIdAndDateAndTime(placeId, date, time))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bookingService.getSlotBy(placeId, date, time))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 시간대의 예약 슬롯이 존재하지 않습니다.");

            verify(placeService).existsById(placeId);
            verify(bookingSlotRepository).findByPlaceIdAndDateAndTime(placeId, date, time);
        }
    }
}
