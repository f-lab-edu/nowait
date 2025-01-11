package com.nowait.domain.repository;

import com.nowait.domain.model.booking.Booking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookingSlotId(Long slotId);
}
