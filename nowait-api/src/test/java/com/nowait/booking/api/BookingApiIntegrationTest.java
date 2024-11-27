package com.nowait.booking.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowait.booking.dto.request.BookingReq;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class BookingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("사용자는 특정 날짜에 가게의 예약 가능 시간을 확인할 수 있다")
    @Test
    void getDailyBookingStatus() throws Exception {
        // given
        long placeId = 1L;
        LocalDate date = LocalDate.of(2024, 12, 25);

        // when
        ResultActions result = mockMvc.perform(
            get("/api/bookings")
                .queryParam("placeId", Long.toString(placeId))
                .queryParam("date", date.toString())
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.status").value("OK"))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.placeId").value("1"))
            .andExpect(jsonPath("$.data.date").value("2024-12-25"))
            .andExpect(jsonPath("$.data.timeList").isArray())
            .andExpect(jsonPath("$.data.timeList[0].time").value("18:00"))
            .andExpect(jsonPath("$.data.timeList[0].available").value(true));
    }

    @DisplayName("사용자는 특정 날짜와 시간에 테이블을 예약할 수 있다")
    @Test
    void book() throws Exception {
        // given
        BookingReq request = createBookRequest(1L, LocalDate.of(2024, 12, 25),
            LocalTime.of(18, 0), 2);

        // when
        ResultActions result = mockMvc.perform(
            post("/api/bookings")
                .header("Authorization", "Bearer " + "access-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("201"))
            .andExpect(jsonPath("$.status").value("CREATED"))
            .andExpect(jsonPath("$.message").value("CREATED"))
            .andExpect(jsonPath("$.data.bookingId").value("1"))
            .andExpect(jsonPath("$.data.bookingStatus").value("PENDING_PAYMENT"))
            .andExpect(jsonPath("$.data.depositRequired").value(true))
            .andExpect(jsonPath("$.data.confirmRequired").value(true));
    }

    @DisplayName("예약자는 예약금 정보를 확인할 수 있다.")
    @Test
    void getDepositInfo() throws Exception {
        // given
        long bookingId = 1L;

        // when
        ResultActions result = mockMvc.perform(
            get("/api/bookings/{bookingId}/deposit-info", bookingId)
                .header("Authorization", "Bearer " + "access-token")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.status").value("OK"))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.bookingId").value("1"))
            .andExpect(jsonPath("$.data.placeId").value("1"))
            .andExpect(jsonPath("$.data.required").value(true))
            .andExpect(jsonPath("$.data.amount").value(20_000))
            .andExpect(jsonPath("$.data.description").value("1인 예약금 x 2명"))
            .andExpect(jsonPath("$.data.refundPolicy").value(
                "- 1일 전 취소: 100% 환불\n- 당일 취소: 환불 불가\n- 노쇼 시: 환불 불가"));
    }

    private static BookingReq createBookRequest(long placeId, LocalDate date, LocalTime time,
        int partySize) {
        return BookingReq.builder()
            .placeId(placeId)
            .date(date)
            .time(time)
            .partySize(partySize)
            .build();
    }


}
