package com.nowait.booking.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BookingStatus {
    CONFIRMED("확정됨"),
    PENDING_CONFIRM("확정 대기 중"),
    PENDING_PAYMENT("결재 대기 중"),
    CANCELLED("취소됨");

    private final String description;
}
