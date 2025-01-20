package com.nowait.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.nowait.application.dto.response.payment.PaymentTokenRes;
import com.nowait.controller.api.dto.request.ReadyPaymentReq;
import com.nowait.controller.api.dto.response.ApiResult;
import java.time.Clock;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@SqlGroup({
    @Sql(scripts = "classpath:/sql/payment_api_test_data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "classpath:/sql/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
})
class PaymentApiIntegrationTest {

    static final String AUTH_HEADER = "Authorization";

    @Autowired
    TestRestTemplate template;
    String authorization;
    LocalDateTime requestAt;

    @BeforeEach
    void setUp() {
        String accessToken = "access-token";
        authorization = "Bearer " + accessToken;
        requestAt = LocalDateTime.of(2024, 11, 2, 1, 59, 59);
    }

    @Nested
    @DisplayName("결제 준비 테스트")
    @ExtendWith(MockitoExtension.class)
    class ReadyPayment {

        @MockBean
        Clock clock;
        LocalDateTime requestAt;

        @BeforeEach
        void setUp() {
            requestAt = LocalDateTime.of(2024, 11, 2, 1, 59, 59);
        }

        @DisplayName("사용자는 예약금 결제 준비를 요청하고 결제 토큰을 발급받는다.")
        @Test
        void ready() {
            // given
            ReadyPaymentReq request = new ReadyPaymentReq(1L, 20_000);

            when(clock.instant()).thenReturn(requestAt.toInstant(
                Clock.systemDefaultZone().getZone().getRules().getOffset(requestAt)));
            when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());

            String url = "/api/payments/ready";
            RequestEntity<ReadyPaymentReq> headerAndBody = RequestEntity
                .post(url)
                .header(AUTH_HEADER, authorization)
                .body(request);

            // when
            ResponseEntity<ApiResult<PaymentTokenRes>> result = template.exchange(
                url,
                HttpMethod.POST,
                headerAndBody,
                new ParameterizedTypeReference<>() {
                }
            );

            // then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

            ApiResult<PaymentTokenRes> body = result.getBody();
            assertThat(body).isNotNull();
            assertThat(body.code()).isEqualTo(200);
            assertThat(body.status()).isEqualTo(HttpStatus.OK);

            PaymentTokenRes data = body.data();
            assertThat(data).isNotNull();
            assertThat(data.paymentToken()).isNotNull();
        }
    }
}

