package com.nowait.excaption;

public class PaymentApprovalException extends RuntimeException {

    public PaymentApprovalException(String message) {
        super(message);
    }
}
