package com.nowait.domain.model.booking;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookingStatus {
    CONFIRMED("확정됨"),
    PENDING_CONFIRM("확정 대기 중"),
    PENDING_PAYMENT("결제 대기 중"),
    CANCELLED("취소됨");

    private final String description;

    public static BookingStatus getStatusAfterBooking(BookingSlot slot) {
        return slot.isDepositRequired() ? PENDING_PAYMENT
            : slot.isConfirmRequired() ? PENDING_CONFIRM : CONFIRMED;
    }
}
