package com.nowait.external.api.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoPayApproveReq(
    String cid,
    String tid,
    @JsonProperty("partner_order_id")
    String partnerOrderId,
    @JsonProperty("partner_user_id")
    String partnerUserId,
    @JsonProperty("pg_token")
    String pgToken
) {

    public static Object of(String cid, String tid, Long bookingId, Long userId, String pgToken) {
        return new KakaoPayApproveReq(cid, tid, bookingId.toString(), userId.toString(), pgToken);
    }
}
