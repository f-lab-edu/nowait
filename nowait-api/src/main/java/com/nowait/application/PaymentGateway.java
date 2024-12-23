package com.nowait.application;

import com.nowait.application.dto.response.payment.PaymentInfo;
import com.nowait.domain.model.booking.Booking;

public interface PaymentGateway {

    PaymentInfo prepare(Long userId, Booking booking, int amount);
}
