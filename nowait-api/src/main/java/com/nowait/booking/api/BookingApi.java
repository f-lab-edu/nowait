package com.nowait.booking.api;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

import com.nowait.booking.dto.TimeSlotDto;
import com.nowait.booking.dto.request.BookingReq;
import com.nowait.booking.dto.response.BookingRes;
import com.nowait.booking.dto.response.DailyBookingStatusRes;
import com.nowait.booking.dto.response.GetBookingInfoRes;
import com.nowait.booking.dto.response.GetDepositInfoRes;
import com.nowait.common.api.dto.ApiResult;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingApi {

    /**
     * 가게 예약 현황 조회 API
     *
     * @param placeId 가게 식별자
     * @param date    예약 현황을 확인할 날짜 (default: 오늘)
     * @return 해당 날짜의 시간별 예약 현황
     */
    @GetMapping
    public ResponseEntity<ApiResult<DailyBookingStatusRes>> getDailyBookingStatus(
        @RequestParam Long placeId,
        @RequestParam(required = false) LocalDate date
    ) {
        // TODO: 예약 현황 조회 비즈니스 로직 호출
        ApiResult<DailyBookingStatusRes> result = ApiResult.ok(
            DailyBookingStatusRes.builder()
                .placeId(placeId)
                .date(date)
                .timeList(List.of(new TimeSlotDto(LocalTime.of(18, 0), true)))
                .build()
        );

        return ok(result);
    }

    /**
     * 테이블 예약 API
     *
     * @param request 예약 정보
     * @return 예약 결과
     */
    @PostMapping
    public ResponseEntity<ApiResult<BookingRes>> book(
        @RequestBody @Valid BookingReq request
    ) {
        // TODO: 테이블 예약 비즈니스 로직 호출
        ApiResult<BookingRes> result = ApiResult.of(
            HttpStatus.CREATED,
            BookingRes.builder()
                .bookingId(1L)
                .bookingStatus("PENDING_PAYMENT")
                .confirmRequired(true)
                .depositRequired(true)
                .build()
        );

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/1")
            .build()
            .toUri();

        return created(location)
            .body(result);
    }

    /**
     * 예약금 정보 상세 조회 API (예약자만 가능)
     *
     * @param bookingId 예약 식별자
     * @return 예약금 정보 조회 결과
     */
    @GetMapping("/{bookingId}/deposit-info")
    public ResponseEntity<ApiResult<GetDepositInfoRes>> getDepositInfo(
        @PathVariable Long bookingId
    ) {
        // TODO: 예약금 정보 조회 비즈니스 로직 호출
        ApiResult<GetDepositInfoRes> result = ApiResult.ok(
            GetDepositInfoRes.builder()
                .bookingId(bookingId)
                .placeId(1L)
                .required(true)
                .amount(20_000)
                .description("1인 예약금 x 2명")
                .refundPolicy("- 1일 전 취소: 100% 환불\n- 당일 취소: 환불 불가\n- 노쇼 시: 환불 불가")
                .build()
        );

        return ok(result);
    }

    /**
     * 예약 정보 조회 API (예약자만 가능)
     *
     * @param bookingId 예약 식별자
     * @return 예약 정보 조회 결과
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResult<GetBookingInfoRes>> getBookingInfo(
        @PathVariable Long bookingId
    ) {
        // TODO: 예약 정보 조회 비즈니스 로직 호출
        ApiResult<GetBookingInfoRes> result = ApiResult.ok(
            GetBookingInfoRes.builder()
                .bookingId(bookingId)
                .date(LocalDate.of(2024, 12, 25))
                .time(LocalTime.of(18, 0))
                .partySize(2)
                .bookingStatus("CONFIRMED")
                .bookedAt(LocalDateTime.of(2024, 11, 25, 17, 10, 0))
                .placeId(1L)
                .placeName("모수")
                .placeDescription("한남동 안성재 셰프의 감각적인 미슐랭 3스타 파인다이닝")
                .placeType("RESTAURANT")
                .placePhoneNumber("02-1234-5678")
                .placeOldAddress("한남동 738-11")
                .placeRoadAddress("서울 용산구 이태원로55가길 45")
                .depositRequired(true)
                .depositAmount(20_000)
                .depositDescription("1인 예약금 x 2명")
                .refundPolicy("- 1일 전 취소: 100% 환불\n- 당일 취소: 환불 불가\n- 노쇼 시: 환불 불가")
                .paymentId(1L)
                .paymentStatus("PAYMENT_COMPLETED")
                .paymentMethod("KAKAO_PAY")
                .paymentAmount(20_000)
                .paidAt(LocalDateTime.of(2024, 11, 25, 17, 12, 0))
                .build()
        );

        return ok(result);
    }

    /**
     * 예약 확정 API (가게 관리자만 가능)
     *
     * @param bookingId 예약 식별자
     * @return 예약 확정 결과
     */
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<ApiResult<Void>> confirmBooking(
        @PathVariable Long bookingId
    ) {
        // TODO: 예약 확정 비즈니스 로직 호출
        ApiResult<Void> result = ApiResult.ok(null);

        return ok(result);
    }

    /**
     * 예약 취소 API (예약자만 가능)
     *
     * @param bookingId 예약 식별자
     * @return 예약 취소 결과
     */
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResult<Void>> cancelBooking(
        @PathVariable Long bookingId
    ) {
        // TODO: 예약 취소 비즈니스 로직 호출
        ApiResult<Void> result = ApiResult.ok(null);

        return ok(result);
    }
}
