package com.nowait.external.api.payment.dto;

import com.nowait.domain.model.booking.Booking;
import com.nowait.external.api.payment.KakaoPayProperties;

public record KakaoPayReadyReq(
    String cid,
    String partnerOrderId,
    String partnerUserId,
    String itemName,
    int quantity,
    int totalAmount,
    int taxFreeAmount,
    String approvalUrl,
    String cancelUrl,
    String failUrl

) {


    public static KakaoPayReadyReq of(Long userId, Booking booking, int amount,
        KakaoPayProperties properties) {
        return new KakaoPayReadyReq(
            properties.cid(),
            booking.getId().toString(),
            userId.toString(),
            "예약금",
            booking.getPartySize(),
            amount,
            amount,
            properties.approvalUrl(),
            properties.cancelUrl(),
            properties.failUrl()
        );
    }
}
