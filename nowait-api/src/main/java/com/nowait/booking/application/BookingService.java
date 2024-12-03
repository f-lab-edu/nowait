package com.nowait.booking.application;

import com.nowait.booking.domain.model.BookingSlot;
import com.nowait.booking.domain.repository.BookingSlotRepository;
import com.nowait.booking.dto.TimeSlotDto;
import com.nowait.booking.dto.response.DailyBookingStatusRes;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingSlotRepository bookingSlotRepository;

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

    private boolean isAvailable(List<BookingSlot> slots) {
        // 모든 슬롯이 예약된 경우에만 false 반환
        return slots.stream().anyMatch(slot -> !slot.isBooked());
    }
}
