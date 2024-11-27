package com.nowait.booking.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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
}
