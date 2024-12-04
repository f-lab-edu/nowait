package com.nowait.booking.domain.repository;

import com.nowait.booking.domain.model.BookingSlot;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingSlotRepository extends JpaRepository<BookingSlot, Long> {

    List<BookingSlot> findAllByPlaceIdAndDate(Long placeId, LocalDate date);

    Optional<BookingSlot> findFirstByPlaceIdAndDateAndTimeAndIsBookedFalse(Long placeId,
        LocalDate date, LocalTime time);
}
