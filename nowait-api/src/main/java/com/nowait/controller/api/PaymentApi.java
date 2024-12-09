package com.nowait.controller.api;

import com.nowait.controller.api.dto.ApiResult;
import com.nowait.controller.api.dto.request.PayDepositReq;
import com.nowait.controller.api.dto.response.payment.PayDepositRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApi {

    /**
     * 예약금 결제 API
     *
     * @param request 예약금 결제 요청
     * @return 예약금 결제 결과
     */
    @PostMapping("/deposit")
    public ApiResult<PayDepositRes> payDeposit(
        @RequestBody @Valid PayDepositReq request
    ) {
        // TODO: 예약금 결제 비즈니스 로직 호출

        return ApiResult.ok(null);
    }
}
