package com.nowait.booking.application;

import com.nowait.booking.domain.model.Booking;
import com.nowait.booking.domain.model.BookingSlot;
import com.nowait.booking.domain.repository.BookingRepository;
import com.nowait.booking.domain.repository.BookingSlotRepository;
import com.nowait.booking.dto.TimeSlotDto;
import com.nowait.booking.dto.response.BookingRes;
import com.nowait.booking.dto.response.DailyBookingStatusRes;
import com.nowait.place.domain.repository.PlaceRepository;
import com.nowait.user.domain.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final BookingEventPublisher bookingEventPublisher;

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
        Booking booking = bookingRepository.save(Booking.of(loginId, partySize, slot));

        bookingEventPublisher.publishBookedEvent(booking, placeId);

        return BookingRes.of(booking, slot);
    }

    private boolean isAvailable(List<BookingSlot> slots) {
        // 모든 슬롯이 예약된 경우에만 false 반환
        return slots.stream().anyMatch(slot -> !slot.isBooked());
    }

    private void validateUserExist(Long userId, String errorMessage) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(errorMessage);
        }
    }

    private void validatePlaceExist(Long placeId, String errorMessage) {
        if (!placeRepository.existsById(placeId)) {
            throw new EntityNotFoundException(errorMessage);
        }
    }

    private BookingSlot findAvailableSlot(Long placeId, LocalDate date, LocalTime time) {
        return bookingSlotRepository.findFirstByPlaceIdAndDateAndTimeAndIsBookedFalse(placeId, date,
            time).orElseThrow(() -> new IllegalArgumentException("예약 가능한 테이블이 없습니다."));
    }
}
