package com.nowait.application;

import com.nowait.application.dto.response.payment.GetDepositPaymentUrlRes;
import com.nowait.application.dto.response.payment.PaymentInfo;
import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.booking.BookingSlot;
import com.nowait.domain.model.booking.DepositPolicy;
import com.nowait.domain.model.payment.Payment;
import com.nowait.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentGatewayFactory paymentGatewayFactory;
    private final BookingService bookingService;
    private final PaymentRepository paymentRepository;

    @Transactional
    public GetDepositPaymentUrlRes prepare(Long loginId, Long bookingId, String paymentMethod,
        int amount) {
        PaymentGateway paymentGateway = paymentGatewayFactory.createPaymentGateway(paymentMethod);
        Booking booking = bookingService.getById(bookingId);
        BookingSlot slot = bookingService.getBookingSlotById(booking.getBookingSlotId());
        DepositPolicy depositPolicy = bookingService.getDepositPolicyById(
            slot.getDepositPolicyId());

        validateDepositAmount(amount, booking, depositPolicy);
        booking.validateOwner(loginId);

        PaymentInfo paymentInfo = paymentGateway.prepare(loginId, booking, amount);
        paymentRepository.save(Payment.of(booking.getId(), paymentInfo.tid()));

        return new GetDepositPaymentUrlRes(paymentInfo.redirectPcUrl());
    }

    private void validateDepositAmount(int amount, Booking booking, DepositPolicy depositPolicy) {
        int depositAmount = depositPolicy.getDepositAmount(booking);
        if (amount != depositAmount) {
            throw new IllegalArgumentException("예약금 금액이 일치하지 않습니다.");
        }
    }
}
