package com.nowait.booking.domain.repository;

import com.nowait.booking.domain.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

}
