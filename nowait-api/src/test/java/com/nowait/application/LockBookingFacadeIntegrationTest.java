package com.nowait.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.nowait.application.event.BookingEventPublisher;
import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.booking.BookingSlot;
import com.nowait.domain.repository.BookingRepository;
import com.nowait.domain.repository.BookingSlotRepository;
import com.nowait.domain.repository.LockRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class LockBookingFacadeIntegrationTest {

    LockBookingFacade lockBookingFacade;
    BookingService bookingService;

    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    LockRepository lockRepository;
    @Mock
    BookingSlotRepository bookingSlotRepository;
    @Mock
    BookingEventPublisher bookingEventPublisher;
    @Mock
    UserService userService;
    @Mock
    PlaceService placeService;
    @Mock
    BookingSlot slot;

    Long loginId;
    Long placeId;
    LocalDate date;
    LocalTime time;
    Integer partySize;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(bookingSlotRepository, bookingRepository,
            bookingEventPublisher, userService, placeService);
        lockBookingFacade = new LockBookingFacade(lockRepository, bookingService);

        loginId = 1L;
        placeId = 1L;
        date = LocalDate.of(2024, 12, 25);
        time = LocalTime.of(18, 0);
        partySize = 2;
    }

    @AfterEach
    void tearDown() {
        bookingRepository.deleteAll();
    }

    @DisplayName("동시에 여러 사용자가 예약을 시도하더라도, 예약은 가능한 만큼만 처리된다.")
    @Test
    void bookConcurrently() throws InterruptedException {
        // given
        when(userService.existsById(loginId)).thenReturn(true);
        when(placeService.existsById(placeId)).thenReturn(true);
        when(bookingSlotRepository.findByPlaceIdAndDateAndTime(placeId, date, time))
            .thenReturn(Optional.of(slot));
        when(slot.getId()).thenReturn(1L);
        when(slot.getCapacity()).thenReturn(1);
        when(slot.isDepositRequired()).thenReturn(true);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    lockBookingFacade.book(loginId, placeId, date, time, partySize);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // when & then
        List<Booking> bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(slot.getCapacity());
    }
}
