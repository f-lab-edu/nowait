package com.nowait.application;

import com.nowait.application.dto.response.payment.PaymentInfo;
import com.nowait.application.dto.response.payment.PaymentResult;
import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.payment.Payment;
import com.nowait.domain.model.payment.PaymentType;

public interface PaymentGateway {

    boolean supports(PaymentType paymentType);
    
    PaymentInfo prepare(Long userId, Booking booking, int amount);

    PaymentResult approve(Long userId, Payment payment, String pgToken);
}
