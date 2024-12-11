package com.nowait.application.dto.response.booking;

import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.booking.BookingSlot;

public record BookingRes(
    Long bookingId,
    String bookingStatus,
    boolean depositRequired,
    boolean confirmRequired
) {

    public static BookingRes of(Booking booking, BookingSlot slot) {
        return new BookingRes(
            booking.getId(),
            booking.getStatus().getDescription(),
            slot.isDepositRequired(),
            slot.isConfirmRequired()
        );
    }
}
