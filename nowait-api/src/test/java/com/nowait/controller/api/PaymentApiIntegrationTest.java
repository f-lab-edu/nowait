package com.nowait.controller.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.nowait.application.dto.response.payment.ReadyPaymentRes;
import com.nowait.config.TestConfig;
import com.nowait.controller.api.dto.request.ReadyPaymentReq;
import com.nowait.controller.api.dto.response.ApiResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
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
@Import(TestConfig.class)
class PaymentApiIntegrationTest {

    static final String AUTH_HEADER = "Authorization";

    @Autowired
    TestRestTemplate template;
    String authorization;

    @BeforeEach
    void setUp() {
        String accessToken = "access-token";
        authorization = "Bearer " + accessToken;
    }

    @Nested
    @DisplayName("결제 준비 테스트")
    class ReadyPayment {

        @DisplayName("사용자는 예약금 결제 준비를 요청하고 결제 토큰을 발급받는다.")
        @Test
        void ready() {
            // given
            ReadyPaymentReq request = new ReadyPaymentReq(1L, 20_000);

            String url = "/api/payments/ready";
            RequestEntity<ReadyPaymentReq> headerAndBody = RequestEntity
                .post(url)
                .header(AUTH_HEADER, authorization)
                .body(request);

            // when
            ResponseEntity<ApiResult<ReadyPaymentRes>> result = template.exchange(
                url,
                HttpMethod.POST,
                headerAndBody,
                new ParameterizedTypeReference<>() {
                }
            );

            // then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

            ApiResult<ReadyPaymentRes> body = result.getBody();
            assertThat(body).isNotNull();
            assertThat(body.code()).isEqualTo(200);
            assertThat(body.status()).isEqualTo(HttpStatus.OK);

            ReadyPaymentRes data = body.data();
            assertThat(data).isNotNull();
            assertThat(data.id()).isNotNull();
        }
    }
}

