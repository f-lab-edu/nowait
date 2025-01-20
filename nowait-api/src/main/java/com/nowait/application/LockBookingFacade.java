package com.nowait.application;

import com.nowait.application.dto.response.booking.BookingRes;
import com.nowait.domain.model.booking.BookingSlot;
import com.nowait.domain.repository.LockRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockBookingFacade {

    private static final String PREFIX = "booking:";

    private final LockRepository lockRepository;
    private final BookingService bookingService;


    public BookingRes book(Long loginId, Long placeId, LocalDate date, LocalTime time,
        Integer partySize) {
        BookingSlot slot = bookingService.getSlotBy(placeId, date, time);
        String key = generateKey(slot);

        while (!lockRepository.lock(key)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            return BookingRes.of(bookingService.book(loginId, slot, partySize), slot);
        } finally {
            lockRepository.unlock(key);
        }
    }

    private String generateKey(BookingSlot slot) {
        return PREFIX + slot.getId();
    }
}
