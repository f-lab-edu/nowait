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
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    public static final int APPROVE_WAITING_MINUTES = 10;
    public static final int PAYMENT_WAITING_HOURS = 2;

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayFactory paymentGatewayFactory;
    private final BookingService bookingService;

    @Transactional
    public ReadyDepositPaymentRes prepare(Long loginId, Long bookingId, PaymentType paymentType,
        int amount, LocalDateTime requestTime) {
        // 1. 필요한 엔티티 조회
        Booking booking = bookingService.getById(bookingId);
        BookingSlot slot = bookingService.getBookingSlotById(booking.getBookingSlotId());
        DepositPolicy depositPolicy = bookingService.getDepositPolicyById(
            slot.getDepositPolicyId());

        // 2. 검증
        validateDepositAmount(amount, booking, depositPolicy);
        booking.validateOwner(loginId);
        validateCanBookingReady(booking, requestTime);

        // 3. 결제 준비
        PaymentGateway paymentGateway = paymentGatewayFactory.createPaymentGateway(paymentType);
        String payToken = UUID.randomUUID().toString();
        PaymentInfo paymentInfo = paymentGateway.prepare(loginId, booking, amount, payToken);
        paymentRepository.save(
            Payment.of(payToken, booking.getId(), paymentInfo.tid(), paymentType, amount,
                paymentInfo.createdAt()));

        return new ReadyDepositPaymentRes(paymentInfo.redirectPcUrl());
    }

    @Transactional
    public void approve(Long loginId, String payToken, String pgToken, LocalDateTime requestTime) {
        // 1. 필요한 엔티티 조회
        Payment payment = getByPayToken(payToken);
        Booking booking = bookingService.getById(payment.getBookingId());

        // 2. 검증
        booking.validateOwner(loginId);
        validateApprovalPayment(payment, booking, requestTime);

        // 3. 결제 승인
        PaymentGateway paymentGateway = paymentGatewayFactory.createPaymentGateway(
            payment.getPaymentType());
        PaymentResult result = paymentGateway.approve(loginId, payment, pgToken);
        payment.confirm(result);

        // 4. 예약 상태 변경
        BookingSlot slot = bookingService.getBookingSlotById(booking.getBookingSlotId());
        booking.completePayment(slot);
    }

    private Payment getByPayToken(String payToken) {
        return paymentRepository.findByToken(payToken)
            .orElseThrow(() -> new EntityNotFoundException("결제 정보가 존재하지 않습니다."));
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
