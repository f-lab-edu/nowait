package com.nowait.controller.api.dto.response.booking;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record GetBookingInfoRes(
    // 예약 관련 정보
    Long bookingId,
    LocalDate date,
    @JsonFormat(shape = STRING, pattern = "HH:mm")
    LocalTime time,
    Integer partySize,
    String bookingStatus,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime bookedAt,

    // 가게 관련 정보
    Long placeId,
    String placeName,
    String placeDescription,
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
    Integer paymentAmount,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime paidAt
) {

}
