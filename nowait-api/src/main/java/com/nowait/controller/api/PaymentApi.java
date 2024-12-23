package com.nowait.controller.api;

import com.nowait.application.dto.response.payment.GetDepositPaymentUrlRes;
import com.nowait.controller.api.dto.request.ApproveDepositPaymentReq;
import com.nowait.controller.api.dto.request.GetDepositPaymentUrlReq;
import com.nowait.controller.api.dto.response.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApi {

    /**
     * 예약금 결제 url 요청 API
     *
     * @param request 예약금 결제 url 요청
     * @return 예약금 결제 url
     */
    @PostMapping("/deposit")
    public ApiResult<GetDepositPaymentUrlRes> getDepositPaymentUrl(
        @RequestBody @Valid GetDepositPaymentUrlReq request
    ) {
        // TODO: 예약금 결제 비즈니스 로직 호출

        return ApiResult.ok(null);
    }

    /**
     * 예약금 결제 승인 요청 API
     *
     * @param request 예약금 결제 승인 요청
     * @return 예약금 결제 url
     */
    @PostMapping("/deposit/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResult<Void> approveDepositPaymentUrl(
        @RequestBody @Valid ApproveDepositPaymentReq request
    ) {
        // TODO: 예약금 결제 승인 로직 호출

        return ApiResult.of(HttpStatus.NO_CONTENT);
    }
}
