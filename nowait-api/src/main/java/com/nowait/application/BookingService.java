package com.nowait.application;

import com.nowait.application.dto.response.booking.BookingRes;
import com.nowait.application.dto.response.booking.DailyBookingStatusRes;
import com.nowait.application.dto.response.booking.TimeSlotDto;
import com.nowait.application.event.BookingEventPublisher;
import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.booking.BookingSlot;
import com.nowait.domain.repository.BookingRepository;
import com.nowait.domain.repository.BookingSlotRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final BookingSlotRepository bookingSlotRepository;
    private final BookingRepository bookingRepository;
    private final BookingEventPublisher bookingEventPublisher;
    private final UserService userService;
    private final PlaceService placeService;

    public DailyBookingStatusRes getDailyBookingStatus(Long placeId, LocalDate date) {
        List<BookingSlot> bookingSlots = bookingSlotRepository.findAllByPlaceIdAndDate(
            placeId, date);

        List<TimeSlotDto> timeSlots = bookingSlots.stream()
            .collect(Collectors.groupingBy(BookingSlot::getTime))
            .entrySet().stream()
            .map(entry -> new TimeSlotDto(entry.getKey(), isAvailable(entry.getValue())))
            .toList();

        return new DailyBookingStatusRes(placeId, date, timeSlots);
    }

    @Transactional
    public BookingRes book(Long loginId, Long placeId, LocalDate date, LocalTime time,
        Integer partySize) {
        validateUserExist(loginId, "존재하지 않는 사용자의 요청입니다.");
        validatePlaceExist(placeId, "존재하지 않는 식당입니다.");

        BookingSlot slot = findAvailableSlot(placeId, date, time);
        Booking booking = bookingRepository.save(Booking.of(loginId, slot, partySize));

        bookingEventPublisher.publishBookedEvent(booking, placeId);

        return BookingRes.of(booking, slot);
    }

    private boolean isAvailable(List<BookingSlot> slots) {
        // 모든 슬롯이 예약된 경우에만 false 반환
        return slots.stream().anyMatch(slot -> !slot.isBooked());
    }

    private BookingSlot findAvailableSlot(Long placeId, LocalDate date, LocalTime time) {
        return bookingSlotRepository.findFirstByPlaceIdAndDateAndTimeAndIsBookedFalse(placeId, date,
            time).orElseThrow(() -> new IllegalArgumentException("예약 가능한 테이블이 없습니다."));
    }

    private void validateUserExist(Long userId, String errorMessage) {
        if (!userService.existsById(userId)) {
            throw new EntityNotFoundException(errorMessage);
        }
    }

    private void validatePlaceExist(Long placeId, String errorMessage) {
        if (!placeService.existsById(placeId)) {
            throw new EntityNotFoundException(errorMessage);
        }
    }
}
