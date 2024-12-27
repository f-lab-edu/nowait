package com.nowait.external.api.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Amount(
    int total,
    @JsonProperty("tax_free")
    int taxFree,
    int vat,
    int point,
    int discount,
    @JsonProperty("green_deposit")
    int greenDeposit
) {

}
