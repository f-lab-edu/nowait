package com.nowait.booking.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nowait.booking.domain.model.BookingSlot;
import com.nowait.booking.domain.repository.BookingSlotRepository;
import com.nowait.booking.dto.response.DailyBookingStatusRes;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(BookingService.class)
class BookingServiceTest {

    @Autowired
    BookingService bookingService;

    @MockBean
    BookingSlotRepository bookingSlotRepository;

    @Nested
    @DisplayName("예약 현황 조회 테스트")
    class GetDailyBookingStatusTest {

        long placeId;
        LocalDate date;

        @BeforeEach
        void setUp() {
            placeId = 1L;
            date = LocalDate.of(2024, 12, 25);
        }

        @DisplayName("특정 날짜에 예약 가능 시간과 예약 상태를 확인할 수 있다")
        @Test
        void getDailyBookingStatus() {
            // given
            List<BookingSlot> bookingSlots = List.of(
                createBookingSlot(LocalTime.of(18, 0), false),
                createBookingSlot(LocalTime.of(19, 0), true)
            );

            when(bookingSlotRepository.findAllByPlaceIdAndDate(any(Long.class),
                any(LocalDate.class))).thenReturn(bookingSlots);

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
                createBookingSlot(LocalTime.of(18, 0), true),
                createBookingSlot(LocalTime.of(18, 0), false),
                createBookingSlot(LocalTime.of(19, 0), true),
                createBookingSlot(LocalTime.of(19, 0), true)
            );

            when(bookingSlotRepository.findAllByPlaceIdAndDate(any(Long.class),
                any(LocalDate.class))).thenReturn(bookingSlots);

            // when
            DailyBookingStatusRes response = bookingService.getDailyBookingStatus(placeId, date);

            // then
            assertThat(response.timeList()).hasSize(2);
            assertThat(response.timeList().get(0).time()).isEqualTo(LocalTime.of(18, 0));
            assertThat(response.timeList().get(0).available()).isTrue();
            assertThat(response.timeList().get(1).time()).isEqualTo(LocalTime.of(19, 0));
            assertThat(response.timeList().get(1).available()).isFalse();
        }

        private BookingSlot createBookingSlot(LocalTime time, boolean isBooked) {
            return new BookingSlot(null, placeId, null, date, time, isBooked, false, false, null);
        }
    }
}
