package com.nowait.application;

import com.nowait.controller.api.dto.response.PaymentTokenRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    public PaymentTokenRes ready(Long loginId, Long bookingId, Integer amount) {
        // TODO: 결제 생성 로직 구현
        return null;
    }
}
