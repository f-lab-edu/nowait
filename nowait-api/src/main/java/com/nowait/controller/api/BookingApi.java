package com.nowait.controller.api;

import static java.util.Objects.isNull;

import com.nowait.application.BookingService;
import com.nowait.application.LockBookingFacade;
import com.nowait.application.dto.response.booking.BookingRes;
import com.nowait.application.dto.response.booking.DailyBookingStatusRes;
import com.nowait.application.dto.response.booking.GetBookingInfoRes;
import com.nowait.application.dto.response.booking.GetDepositInfoRes;
import com.nowait.controller.api.dto.request.BookingReq;
import com.nowait.controller.api.dto.response.ApiResult;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingApi {

    private final BookingService bookingService;
    private final LockBookingFacade lockBookingFacade;
    private final ExecutorService executorService;


    /**
     * 가게 예약 현황 조회 API
     *
     * @param placeId 가게 식별자
     * @param date    예약 현황을 확인할 날짜 (default: 오늘)
     * @return 해당 날짜의 시간별 예약 현황
     */
    @GetMapping
    public CompletableFuture<ApiResult<DailyBookingStatusRes>> getDailyBookingStatus(
        @RequestParam Long placeId,
        @RequestParam(required = false) LocalDate date
    ) {
        LocalDate targetDate = isNull(date) ? LocalDate.now() : date;
        return CompletableFuture.supplyAsync(
                () -> bookingService.getDailyBookingStatus(placeId, targetDate), executorService)
            .thenApply(ApiResult::ok);
    }

    /**
     * 테이블 예약 API
     *
     * @param request 예약 정보
     * @return 예약 결과
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompletableFuture<ApiResult<BookingRes>> book(
        @RequestBody @Valid BookingReq request
    ) {
        // TODO: Auth 기능 구현 시 loginId를 Authentication에서 가져오도록 수정
        Long loginId = 1L;
        return CompletableFuture.supplyAsync(
                () -> lockBookingFacade.book(loginId, request.placeId(), request.date(),
                    request.time(), request.partySize()), executorService)
            .thenApply((data) -> ApiResult.of(HttpStatus.CREATED, data));
    }

    /**
     * 예약금 정보 상세 조회 API (예약자만 가능)
     *
     * @param bookingId 예약 식별자
     * @return 예약금 정보 조회 결과
     */
    @GetMapping("/{bookingId}/deposit-info")
    public ApiResult<GetDepositInfoRes> getDepositInfo(
        @PathVariable Long bookingId
    ) {
        // TODO: 예약금 정보 조회 비즈니스 로직 호출

        return ApiResult.ok(
            new GetDepositInfoRes(bookingId, 1L, true, 20_000, "1인 예약금 x 2명",
                "- 1일 전 취소: 100% 환불\n- 당일 취소: 환불 불가\n- 노쇼 시: 환불 불가"
            )
        );
    }

    /**
     * 예약 정보 조회 API (예약자만 가능)
     *
     * @param bookingId 예약 식별자
     * @return 예약 정보 조회 결과
     */
    @GetMapping("/{bookingId}")
    public ApiResult<GetBookingInfoRes> getBookingInfo(
        @PathVariable Long bookingId
    ) {
        // TODO: 예약 정보 조회 비즈니스 로직 호출

        return ApiResult.ok(
            new GetBookingInfoRes(
                bookingId,
                LocalDate.of(2024, 12, 25),
                LocalTime.of(18, 0),
                2,
                "CONFIRMED",
                LocalDateTime.of(2024, 11, 25, 17, 10, 0),
                1L, "모수", "한남동 안성재 셰프의 감각적인 미슐랭 3스타 파인다이닝",
                "RESTAURANT",
                "02-1234-5678",
                "한남동 738-11",
                "서울 용산구 이태원로55가길 45",
                true,
                20_000,
                "1인 예약금 x 2명",
                "- 1일 전 취소: 100% 환불\n- 당일 취소: 환불 불가\n- 노쇼 시: 환불 불가",
                1L,
                "PAYMENT_COMPLETED",
                "KAKAO_PAY",
                20_000,
                LocalDateTime.of(2024, 11, 25, 17, 12, 0)
            )
        );
    }

    /**
     * 예약 확정 API (가게 관리자만 가능)
     *
     * @param bookingId 예약 식별자
     * @return 예약 확정 결과
     */
    @PostMapping("/{bookingId}/confirm")
    public ApiResult<Void> confirmBooking(
        @PathVariable Long bookingId
    ) {
        // TODO: 예약 확정 비즈니스 로직 호출

        return ApiResult.ok(null);
    }

    /**
     * 예약 취소 API (예약자만 가능)
     *
     * @param bookingId 예약 식별자
     * @return 예약 취소 결과
     */
    @PostMapping("/{bookingId}/cancel")
    public ApiResult<Void> cancelBooking(
        @PathVariable Long bookingId
    ) {
        // TODO: 예약 취소 비즈니스 로직 호출

        return ApiResult.ok(null);
    }
}
