package com.nowait.booking.dto.response;

import com.nowait.booking.domain.model.Booking;
import com.nowait.booking.domain.model.BookingSlot;

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
