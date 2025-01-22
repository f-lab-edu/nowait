package com.nowait.application;

import com.nowait.application.dto.response.payment.ReadyPaymentRes;
import com.nowait.config.PaymentProperties;
import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.payment.Payment;
import com.nowait.domain.repository.PaymentRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentProperties property;
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final DepositService depositService;

    @Transactional
    public ReadyPaymentRes ready(Long loginId, Long bookingId, Integer amount,
        LocalDateTime requestAt) {
        // 1. 필요한 엔티티 조회
        Booking booking = bookingService.getById(bookingId);

        // 2-1. 검증 - 예약자 본인인지 확인
        booking.validateOwner(loginId);
        // 2-2. 검증 - 결제 금액이 예약금과 일치하는지 확인
        depositService.validateDepositAmount(booking, amount);
        // 2-3. 검증 - 결제 가능한 상태인지 확인
        validateCanBookingReady(booking, requestAt);

        // 3. 결제 생성 및 저장
        Payment payment = paymentRepository.save(Payment.of(bookingId, loginId, amount));

        return new ReadyPaymentRes(payment.getId());
    }

    private void validateCanBookingReady(Booking booking, LocalDateTime requestAt) {
        // 1. 예약 상태가 '결제 대기 중'인지 확인
        validatePayableBookingStatus(booking);

        // 2. 결제 대기 시간이 지나지 않았는지 확인
        if (isPassedPaymentWaitingTime(booking, requestAt)) {
            throw new IllegalArgumentException("결제 대기 시간이 지났습니다.");
        }
    }

    private void validatePayableBookingStatus(Booking booking) {
        if (!booking.isPaymentAvailable()) {
            throw new IllegalArgumentException("결제할 수 없는 예약입니다.");
        }
    }

    private boolean isPassedPaymentWaitingTime(Booking booking, LocalDateTime requestAt) {
        return requestAt.isAfter(
            booking.getCreatedAt().plusHours(property.depositPaymentWaitHours()));
    }
}
