package com.nowait.controller.api;

import com.nowait.application.dto.response.payment.ReadyDepositPaymentRes;
import com.nowait.controller.api.dto.request.ApproveDepositPaymentReq;
import com.nowait.controller.api.dto.request.ReadyDepositPaymentReq;
import com.nowait.controller.api.dto.response.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApi {

    /**
     * 예약금 결제 준비 요청 API
     *
     * @param request 예약금 결제 준비 요청
     * @return 예약금 결제 준비 응답 (결제 식별자, 간편결제 redirect url)
     */
    @PostMapping("/deposit/ready")
    public ApiResult<ReadyDepositPaymentRes> readyDepositPayment(
        @RequestBody @Valid ReadyDepositPaymentReq request
    ) {
        // TODO: 예약금 결제 비즈니스 로직 호출

        return ApiResult.ok(new ReadyDepositPaymentRes(
            "https://online-payment.kakaopay.com/mockup/bridge/pc/pg/one-time/payment/1"));
    }

    /**
     * 예약금 결제 승인 요청 API
     *
     * @param request 예약금 결제 승인 요청
     * @return 예약금 결제 url
     */
    @PostMapping("/deposit/approve")
    public ApiResult<Void> approveDepositPayment(
        @RequestBody @Valid ApproveDepositPaymentReq request
    ) {
        // TODO: 예약금 결제 승인 로직 호출

        return ApiResult.of(HttpStatus.NO_CONTENT);
    }
}
