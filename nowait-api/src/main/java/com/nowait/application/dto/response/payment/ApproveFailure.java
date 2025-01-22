package com.nowait.application.dto.response.payment;

public record ApproveFailure(
    String code,
    String message,
    boolean retrievable
) {

}
