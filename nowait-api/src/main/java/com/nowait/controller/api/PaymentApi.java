package com.nowait.controller.api;

import com.nowait.application.PaymentService;
import com.nowait.application.dto.response.payment.PaymentTokenRes;
import com.nowait.controller.api.dto.request.ReadyPaymentReq;
import com.nowait.controller.api.dto.response.ApiResult;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApi {

    private final PaymentService paymentService;
    private final ExecutorService executorService;
    private final Clock clock;

    /**
     * 결제 준비 요청 API
     *
     * @param request 결제 준비 요청 (bookingId, amount)
     * @return 결제 토큰
     */
    @PostMapping("/ready")
    public CompletableFuture<ApiResult<PaymentTokenRes>> ready(
        @RequestBody @Valid ReadyPaymentReq request
    ) {
        // TODO: Auth 기능 구현 시 loginId를 Authentication에서 가져오도록 수정
        Long loginId = 1L;
        return CompletableFuture.supplyAsync(
                () -> paymentService.ready(loginId, request.bookingId(), request.amount(),
                    LocalDateTime.now(clock)), executorService)
            .thenApply(ApiResult::ok);
    }
            .thenApply(ApiResult::ok);
    }
}
