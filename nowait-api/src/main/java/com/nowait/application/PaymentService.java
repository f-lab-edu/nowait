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
import com.nowait.exception.PaymentApprovalException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    public static final int APPROVE_WAITING_MINUTES = 10;
    public static final int PAYMENT_WAITING_HOURS = 10;

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayFactory paymentGatewayFactory;
    private final BookingService bookingService;

    @Transactional
    public ReadyDepositPaymentRes prepare(Long loginId, Long bookingId, PaymentType paymentType,
        int amount, LocalDateTime requestTime) {
        Booking booking = bookingService.getById(bookingId);
        BookingSlot slot = bookingService.getBookingSlotById(booking.getBookingSlotId());
        DepositPolicy depositPolicy = bookingService.getDepositPolicyById(
            slot.getDepositPolicyId());

        validateDepositAmount(amount, booking, depositPolicy);
        booking.validateOwner(loginId);
        validateCanBookingReady(booking, requestTime);

        PaymentGateway paymentGateway = paymentGatewayFactory.createPaymentGateway(paymentType);
        PaymentInfo paymentInfo = paymentGateway.prepare(loginId, booking, amount);
        Payment payment = paymentRepository.save(
            Payment.of(booking.getId(), paymentInfo.tid(), paymentType, amount,
                paymentInfo.createdAt()));

        return new ReadyDepositPaymentRes(payment.getId(), paymentInfo.redirectPcUrl());
    }

    @Transactional
    public void approve(Long loginId, Long paymentId, String pgToken, LocalDateTime requestTime) {
        Payment payment = getById(paymentId);
        Booking booking = bookingService.getById(payment.getBookingId());

        booking.validateOwner(loginId);
        validateApprovalPayment(payment, booking, requestTime);

        PaymentGateway paymentGateway = paymentGatewayFactory.createPaymentGateway(
            payment.getPaymentType());
        PaymentResult result = paymentGateway.approve(loginId, payment, pgToken);
        payment.updatePaymentResult(result);

        BookingSlot slot = bookingService.getBookingSlotById(booking.getBookingSlotId());
        booking.completePayment(slot);
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

    private void validateCanBookingReady(Booking booking, LocalDateTime requestTime) {
        // 1. 예약 상태가 '결제 대기 중'인지 확인
        validatePayableBookingStatus(booking);

        // 2. 결제 대기 시간(2시간)이 지나지 않았더라면 OK
        if (isPassedPaymentWaitingTime(booking, requestTime)) {
            throw new PaymentApprovalException("결제 대기 시간이 지났습니다.");
        }
    }

    private void validateApprovalPayment(Payment payment, Booking booking,
        LocalDateTime requestTime) {
        // 1. 예약 상태가 '결제 대기 중'인지 확인
        validatePayableBookingStatus(booking);

        // 2. 결제 준비 대기 시간이 지나지 않았더라면 OK
        if (isPassedApproveWaitingTime(payment, requestTime)) {
            throw new PaymentApprovalException("결제 대기 시간이 지났습니다.");
        }
    }

    private static boolean isPassedPaymentWaitingTime(Booking booking, LocalDateTime requestTime) {
        return requestTime.isAfter(booking.getCreatedAt().plusHours(PAYMENT_WAITING_HOURS));
    }

    private static boolean isPassedApproveWaitingTime(Payment payment, LocalDateTime requestTime) {
        return requestTime.isAfter(payment.getReadyAt().plusMinutes(APPROVE_WAITING_MINUTES));
    }

    private static void validatePayableBookingStatus(Booking booking) {
        if (!booking.isPaymentAvailable()) {
            throw new PaymentApprovalException("결제할 수 없는 예약입니다.");
        }
    }
}
