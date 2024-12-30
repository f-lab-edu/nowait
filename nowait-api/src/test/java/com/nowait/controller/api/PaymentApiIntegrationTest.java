package com.nowait.controller.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.nowait.application.dto.response.payment.ReadyDepositPaymentRes;
import com.nowait.controller.api.dto.request.ApproveDepositPaymentReq;
import com.nowait.controller.api.dto.request.ReadyDepositPaymentReq;
import com.nowait.controller.api.dto.response.ApiResult;
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

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PaymentApiIntegrationTest {

    @Autowired
    TestRestTemplate template;

    String authorization;

    @BeforeEach
    void setUp() {
        String accessToken = "access-token";
        authorization = "Bearer " + accessToken;
    }

    @Nested
    @DisplayName("예약금 결제 준비 테스트")
    class ReadyDepositPayment {

        @DisplayName("사용자는 예약금 결제를 위해 카카오페이 결제 URL을 요청할 수 있다")
        @Test
        void readyDepositPaymentWithKakaoPay() {
            // given
            ReadyDepositPaymentReq request = new ReadyDepositPaymentReq(1L, "KAKAO_PAY", 20_000);

            String url = "/api/payments/deposit/ready";
            RequestEntity<ReadyDepositPaymentReq> headerAndBody = RequestEntity
                .post(url)
                .header("Authorization", authorization)
                .body(request);

            // when
            ResponseEntity<ApiResult<ReadyDepositPaymentRes>> result = template.exchange(
                url,
                HttpMethod.POST,
                headerAndBody,
                new ParameterizedTypeReference<>() {
                }
            );

            // then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

            ApiResult<ReadyDepositPaymentRes> body = result.getBody();
            assertThat(body).isNotNull();
            assertThat(body.code()).isEqualTo(200);
            assertThat(body.status()).isEqualTo(HttpStatus.OK);

            ReadyDepositPaymentRes data = body.data();
            assertThat(data).isNotNull();
            assertThat(data.url()).isNotNull();
        }
    }

    @Nested
    @DisplayName("예약금 결제 승인 테스트")
    class ApproveDepositPayment {

        @DisplayName("사용자는 카카오페이 결제 후 결제 승인 요청을 할 수 있다.")
        @Test
        void approveDepositPaymentWithKakaoPay() {
            // given
            ApproveDepositPaymentReq request = new ApproveDepositPaymentReq(1L, "pgToken",
                "payToken");

            String url = "/api/payments/deposit/approve";
            RequestEntity<ApproveDepositPaymentReq> headerAndBody = RequestEntity
                .post(url)
                .header("Authorization", authorization)
                .body(request);

            // when
            ResponseEntity<ApiResult<Void>> result = template.exchange(
                url,
                HttpMethod.POST,
                headerAndBody,
                new ParameterizedTypeReference<>() {
                }
            );

            // then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

            ApiResult<Void> body = result.getBody();
            assertThat(body).isNotNull();
            assertThat(body.code()).isEqualTo(204);
            assertThat(body.status()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }
}
