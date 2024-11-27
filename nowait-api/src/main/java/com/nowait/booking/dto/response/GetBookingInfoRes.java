package com.nowait.booking.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record GetBookingInfoRes(
    // 예약 관련 정보
    Long bookingId,
    LocalDate date,
    LocalTime time,
    Integer partySize,
    String bookingStatus,

    // 가게 관련 정보
    Long placeId,
    Long placeName,
    Long placeDescription,
    String placeType,
    String placePhoneNumber,
    String placeOldAddress,
    String placeRoadAddress,

    // 예약금 관련 정보
    boolean depositRequired,
    Integer depositAmount,
    String depositDescription,
    String refundPolicy,

    // 결제 관련 정보
    Long paymentId,
    String paymentStatus,
    String paymentMethod,
    String paymentAmount
) {

}
