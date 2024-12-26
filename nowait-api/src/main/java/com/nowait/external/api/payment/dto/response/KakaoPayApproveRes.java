package com.nowait.external.api.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record KakaoPayApproveRes(
    String aid,
    String tid,
    String cid,
    String sid,
    @JsonProperty("partner_order_id")
    String partnerOrderId,
    @JsonProperty("partner_user_id")
    String partnerUserId,
    @JsonProperty("payment_method_type")
    String paymentMethodType,
    Amount amount,
    @JsonProperty("card_info")
    CardInfo cardInfo,
    @JsonProperty("item_name")
    String itemName,
    @JsonProperty("item_code")
    String itemCode,
    int quantity,
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    @JsonProperty("approved_at")
    LocalDateTime approvedAt,
    String payload
) {

}
