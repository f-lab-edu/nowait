package com.nowait.domain.model.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    READY("결제 준비 중"),
    IN_PROGRESS("승인 진행 중"),
    DONE("승인 완료"),
    CANCELED("취소됨"),
    ABORTED("승인 실패"),
    EXPIRED("유효 기간 만료됨");

    private final String description;
}
