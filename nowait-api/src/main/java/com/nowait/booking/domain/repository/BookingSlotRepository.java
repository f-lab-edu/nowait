package com.nowait.booking.domain.repository;

import com.nowait.booking.domain.model.BookingSlot;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingSlotRepository extends JpaRepository<BookingSlot, Long> {

    List<BookingSlot> findAllByPlaceIdAndDate(Long placeId, LocalDate date);
}
