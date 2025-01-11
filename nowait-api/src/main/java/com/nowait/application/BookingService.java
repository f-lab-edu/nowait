package com.nowait.application;

import com.nowait.application.dto.response.booking.BookingRes;
import com.nowait.application.dto.response.booking.DailyBookingStatusRes;
import com.nowait.application.dto.response.booking.TimeSlotDto;
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
            .map(slot -> new TimeSlotDto(slot.getTime(), !isAllBooked(slot)))
            .toList();

        return new DailyBookingStatusRes(placeId, date, timeSlots);
    }

    @Transactional
    public BookingRes book(Long loginId, Long placeId, LocalDate date, LocalTime time,
        Integer partySize) {
        validateUserExist(loginId, "존재하지 않는 사용자의 요청입니다.");
        validatePlaceExist(placeId, "존재하지 않는 식당입니다.");

        BookingSlot slot = bookingSlotRepository.findByPlaceIdAndDateAndTime(placeId, date, time)
            .orElseThrow(() -> new IllegalArgumentException("해당 시간대의 예약이 불가능합니다."));
        validateBookingPossible(slot);
        Booking booking = bookingRepository.save(Booking.of(loginId, slot, partySize));

        bookingEventPublisher.publishBookedEvent(booking, placeId);

        return BookingRes.of(booking, slot);
    }

    private boolean isAllBooked(BookingSlot slot) {
        List<Booking> bookings = bookingRepository.findAllByBookingSlotId(slot.getId());

        long activeCount = bookings.stream()
            .filter(this::isActiveBooking)
            .count();

        return activeCount >= slot.getCount();
    }

    private boolean isActiveBooking(Booking booking) {
        // TODO: 결제 대기 중이라면 결제 서비스에게 유효한 결제 상태인지 확인하는 로직 추가
        return booking.getStatus() != BookingStatus.CANCELLED;
    }

    private void validateBookingPossible(BookingSlot slot) {
        if (isAllBooked(slot)) {
            throw new IllegalArgumentException("예약 가능한 테이블이 없습니다.");
        }
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
