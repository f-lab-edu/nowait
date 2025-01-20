package com.nowait.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import com.nowait.application.dto.response.booking.BookingRes;
import com.nowait.application.dto.response.booking.DailyBookingStatusRes;
import com.nowait.application.dto.response.booking.GetBookingInfoRes;
import com.nowait.application.dto.response.booking.GetDepositInfoRes;
import com.nowait.controller.api.dto.request.BookingReq;
import com.nowait.controller.api.dto.response.ApiResult;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@SqlGroup({
    @Sql(scripts = "classpath:/sql/booking_api_test_data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "classpath:/sql/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
})
class BookingApiIntegrationTest {

    @Autowired
    TestRestTemplate template;

    String authorization;

    @BeforeEach
    void setUp() {
        String accessToken = "access-token";
        authorization = "Bearer " + accessToken;
    }

    @Nested
    @DisplayName("예약 현황 조회 테스트")
    class DailyBookingStatusTest {

        @DisplayName("사용자는 특정 날짜에 가게의 예약 가능 시간을 확인할 수 있다")
        @Test
        void getDailyBookingStatus() {
            // given
            long placeId = 1L;
            LocalDate date = LocalDate.of(2024, 12, 25);

            String url = UriComponentsBuilder.fromPath("/api/bookings")
                .queryParam("placeId", placeId)
                .queryParam("date", date)
                .toUriString();

            // when
            ResponseEntity<ApiResult<DailyBookingStatusRes>> result = template.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
            );

            // then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

            ApiResult<DailyBookingStatusRes> body = result.getBody();
            assertThat(body).isNotNull();
            assertThat(body.code()).isEqualTo(200);
            assertThat(body.status()).isEqualTo(HttpStatus.OK);

            DailyBookingStatusRes data = body.data();
            assertThat(data).isNotNull();
            assertThat(data.placeId()).isEqualTo(placeId);
            assertThat(data.date()).isEqualTo(date);
            assertThat(data.timeList()).hasSize(1);
            assertThat(data.timeList().get(0).time()).isEqualTo("18:00");
            assertThat(data.timeList().get(0).available()).isTrue();
        }
    }

    @DisplayName("사용자는 특정 날짜와 시간에 테이블을 예약할 수 있다")
    @Test
    void book() {
        // given
        BookingReq request = new BookingReq(1L, LocalDate.of(2024, 12, 25),
            LocalTime.of(18, 0), 2);

        String url = "/api/bookings";
        RequestEntity<BookingReq> headerAndBody = RequestEntity
            .post(url)
            .header("Authorization", authorization)
            .body(request);

        // when
        ResponseEntity<ApiResult<BookingRes>> response = template.exchange(
            url,
            HttpMethod.POST,
            headerAndBody,
            new ParameterizedTypeReference<>() {
            }
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ApiResult<BookingRes> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.code()).isEqualTo(201);
        assertThat(body.status()).isEqualTo(HttpStatus.CREATED);

        BookingRes data = body.data();
        assertThat(data).isNotNull();
        assertThat(data.bookingId()).isEqualTo(1L);
        assertThat(data.bookingStatus()).isEqualTo("PENDING_PAYMENT");
        assertThat(data.depositRequired()).isTrue();
        assertThat(data.confirmRequired()).isTrue();
    }

    @DisplayName("예약자는 예약금 정보를 확인할 수 있다.")
    @Test
    void getDepositInfo() {
        // given
        long bookingId = 1L;

        String url = UriComponentsBuilder.fromPath("/api/bookings/{bookingId}/deposit-info")
            .buildAndExpand(bookingId)
            .toUriString();

        RequestEntity<Void> header = RequestEntity
            .get(url)
            .header("Authorization", authorization)
            .build();

        // when
        ResponseEntity<ApiResult<GetDepositInfoRes>> response = template.exchange(
            url,
            HttpMethod.GET,
            header,
            new ParameterizedTypeReference<>() {
            }
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ApiResult<GetDepositInfoRes> result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo(200);
        assertThat(result.status()).isEqualTo(HttpStatus.OK);

        GetDepositInfoRes data = result.data();
        assertThat(data).isNotNull();
        assertThat(data.bookingId()).isEqualTo(1L);
        assertThat(data.placeId()).isEqualTo(1L);
        assertThat(data.required()).isTrue();
        assertThat(data.amount()).isEqualTo(20_000);
        assertThat(data.description()).isEqualTo("1인 예약금 x 2명");
        assertThat(data.refundPolicy()).isEqualTo(
            "- 1일 전 취소: 100% 환불\n- 당일 취소: 환불 불가\n- 노쇼 시: 환불 불가");
    }

    @DisplayName("예약자는 예약 정보를 확인할 수 있다.")
    @Test
    void getBookingInfo() {
        // given
        long bookingId = 1L;

        String url = UriComponentsBuilder.fromPath("/api/bookings/{bookingId}")
            .buildAndExpand(bookingId)
            .toUriString();

        RequestEntity<Void> header = RequestEntity
            .get(url)
            .header("Authorization", authorization)
            .build();

        // when
        ResponseEntity<ApiResult<GetBookingInfoRes>> response = template.exchange(
            url,
            HttpMethod.GET,
            header,
            new ParameterizedTypeReference<>() {
            }
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ApiResult<GetBookingInfoRes> result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo(200);
        assertThat(result.status()).isEqualTo(HttpStatus.OK);

        GetBookingInfoRes data = result.data();
        assertThat(data).isNotNull();
        assertThat(data.bookingId()).isEqualTo(1L);
        assertThat(data.date()).isEqualTo("2024-12-25");
        assertThat(data.time()).isEqualTo("18:00");
        assertThat(data.partySize()).isEqualTo(2);
        assertThat(data.bookingStatus()).isEqualTo("CONFIRMED");
        assertThat(data.bookedAt()).isEqualTo("2024-11-25T17:10:00");
        assertThat(data.placeId()).isEqualTo(1L);
        assertThat(data.placeName()).isEqualTo("모수");
        assertThat(data.placeDescription()).isEqualTo("한남동 안성재 셰프의 감각적인 미슐랭 3스타 파인다이닝");
        assertThat(data.depositRequired()).isTrue();
        assertThat(data.depositAmount()).isEqualTo(20_000);
        assertThat(data.depositDescription()).isEqualTo("1인 예약금 x 2명");
        assertThat(data.refundPolicy()).isEqualTo(
            "- 1일 전 취소: 100% 환불\n- 당일 취소: 환불 불가\n- 노쇼 시: 환불 불가");
        assertThat(data.paymentId()).isEqualTo(1L);
        assertThat(data.paymentStatus()).isEqualTo("PAYMENT_COMPLETED");
        assertThat(data.paymentMethod()).isEqualTo("KAKAO_PAY");
        assertThat(data.paymentAmount()).isEqualTo(20_000);
        assertThat(data.paidAt()).isEqualTo("2024-11-25T17:12:00");
    }

    @DisplayName("가게 관리자는 확정 대기 중인 예약을 확정할 수 있다.")
    @Test
    void confirmBooking() {
        // given
        long bookingId = 1L;

        String url = UriComponentsBuilder.fromPath("/api/bookings/{bookingId}/confirm")
            .buildAndExpand(bookingId)
            .toUriString();

        RequestEntity<Void> header = RequestEntity
            .post(url)
            .header("Authorization", authorization)
            .build();

        // when

        ResponseEntity<ApiResult<Void>> response = template.exchange(
            url,
            HttpMethod.POST,
            header,
            new ParameterizedTypeReference<>() {
            }
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ApiResult<Void> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.code()).isEqualTo(200);
        assertThat(body.status()).isEqualTo(HttpStatus.OK);
        assertThat(body.data()).isNull();
    }

    @DisplayName("예약자는 방문 예정인 예약을 취소할 수 있다.")
    @Test
    void cancelBooking() {
        // given
        long bookingId = 1L;

        String url = UriComponentsBuilder.fromPath("/api/bookings/{bookingId}/cancel")
            .buildAndExpand(bookingId)
            .toUriString();

        RequestEntity<Void> header = RequestEntity
            .post(url)
            .header("Authorization", authorization)
            .build();

        // when
        ResponseEntity<ApiResult<Void>> response = template.exchange(
            url,
            HttpMethod.POST,
            header,
            new ParameterizedTypeReference<>() {
            }
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ApiResult<Void> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.code()).isEqualTo(200);
        assertThat(body.status()).isEqualTo(HttpStatus.OK);
        assertThat(body.data()).isNull();
    }
}
