package com.nowait.external.api.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nowait.domain.model.booking.Booking;
import com.nowait.external.api.payment.KakaoPayProperties;

public record KakaoPayReadyReq(
    String cid,
    @JsonProperty("partner_order_id")
    String partnerOrderId,
    @JsonProperty("partner_user_id")
    String partnerUserId,
    @JsonProperty("item_name")
    String itemName,
    int quantity,
    @JsonProperty("total_amount")
    int totalAmount,
    @JsonProperty("tax_free_amount")
    int taxFreeAmount,
    @JsonProperty("approval_url")
    String approvalUrl,
    @JsonProperty("cancel_url")
    String cancelUrl,
    @JsonProperty("fail_url")
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
