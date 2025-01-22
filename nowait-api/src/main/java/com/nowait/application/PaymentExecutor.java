package com.nowait.application;

import com.nowait.application.dto.response.payment.ApproveRes;
import com.nowait.controller.api.dto.response.ApiResult;

public interface PaymentExecutor {

    ApiResult<ApproveRes> executeApproval(Long bookingId, Integer amount, String paymentKey);
}
