package com.nowait.application;

import com.nowait.application.dto.response.payment.PaymentInfo;
import com.nowait.application.dto.response.payment.PaymentResult;
import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.payment.Payment;
import com.nowait.domain.model.payment.PaymentType;

public interface PaymentGateway {

    PaymentInfo prepare(Long userId, Booking booking, int amount);

    boolean supports(PaymentType paymentType);

    PaymentResult approve(Long userId, Payment payment, String pgToken);
}
