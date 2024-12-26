package com.nowait.application;

import com.nowait.application.dto.response.payment.PaymentInfo;
import com.nowait.application.dto.response.payment.PaymentResult;
import com.nowait.application.dto.response.payment.ReadyDepositPaymentRes;
import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.booking.BookingSlot;
import com.nowait.domain.model.booking.DepositPolicy;
import com.nowait.domain.model.payment.Payment;
import com.nowait.domain.model.payment.PaymentType;
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
    public ReadyDepositPaymentRes prepare(Long loginId, Long bookingId, PaymentType paymentType,
        int amount) {
        Booking booking = bookingService.getById(bookingId);
        BookingSlot slot = bookingService.getBookingSlotById(booking.getBookingSlotId());
        DepositPolicy depositPolicy = bookingService.getDepositPolicyById(
            slot.getDepositPolicyId());

        validateDepositAmount(amount, booking, depositPolicy);
        booking.validateOwner(loginId);

        PaymentGateway paymentGateway = paymentGatewayFactory.createPaymentGateway(paymentType);
        PaymentInfo paymentInfo = paymentGateway.prepare(loginId, booking, amount);
        Payment payment = paymentRepository.save(
            Payment.of(booking.getId(), paymentInfo.tid(), paymentType, amount,
                paymentInfo.createdAt()));

        return new ReadyDepositPaymentRes(payment.getId(), paymentInfo.redirectPcUrl());
    }

    @Transactional
    public void approve(Long loginId, Long paymentId, String pgToken) {
        Payment payment = getById(paymentId);
        Booking booking = bookingService.getById(payment.getBookingId());

        booking.validateOwner(loginId);

        PaymentGateway paymentGateway = paymentGatewayFactory.createPaymentGateway(
            payment.getPaymentType());

        PaymentResult result = paymentGateway.approve(loginId, payment, pgToken);
        payment.updatePaymentResult(result);
    }

    private Payment getById(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보가 존재하지 않습니다."));
    }

    private void validateDepositAmount(int amount, Booking booking, DepositPolicy depositPolicy) {
        int depositAmount = depositPolicy.getDepositAmount(booking);
        if (amount != depositAmount) {
            throw new IllegalArgumentException("예약금 금액이 일치하지 않습니다.");
        }
    }
}
