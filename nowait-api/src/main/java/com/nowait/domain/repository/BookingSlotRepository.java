package com.nowait.domain.repository;

import com.nowait.domain.model.booking.BookingSlot;
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
