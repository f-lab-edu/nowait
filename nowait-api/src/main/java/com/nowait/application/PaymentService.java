package com.nowait.application;

import com.nowait.config.PaymentProperties;
import com.nowait.controller.api.dto.response.PaymentTokenRes;
import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.payment.Payment;
import com.nowait.domain.model.payment.PaymentToken;
import com.nowait.domain.repository.PaymentRepository;
import com.nowait.domain.repository.PaymentTokenRepository;
import com.nowait.utils.ShortUUIDGenerator;
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
    private final PaymentTokenRepository paymentTokenRepository;
    private final BookingService bookingService;
    private final DepositService depositService;

    @Transactional
    public PaymentTokenRes ready(Long loginId, Long bookingId, Integer amount,
        LocalDateTime requestTime) {
        // 1. 필요한 엔티티 조회
        Booking booking = bookingService.getById(bookingId);

        // 2-1. 검증 - 예약자 본인인지 확인
        booking.validateOwner(loginId);
        // 2-2. 검증 - 결제 금액이 예약금과 일치하는지 확인
        depositService.validateDepositAmount(booking, amount);
        // 2-3. 검증 - 결제 가능한 상태인지 확인
        validateCanBookingReady(booking, requestTime);

        // 3. 결제 생성 및 저장
        Payment payment = paymentRepository.save(Payment.of(bookingId, loginId, amount));

        // 4. PaymentToken 생성 및 저장
        String token = ShortUUIDGenerator.generate();
        paymentTokenRepository.save(PaymentToken.of(token, payment.getId()));

        return new PaymentTokenRes(token);
    }

    private void validateCanBookingReady(Booking booking, LocalDateTime requestTime) {
        // 1. 예약 상태가 '결제 대기 중'인지 확인
        validatePayableBookingStatus(booking);

        // 2. 결제 대기 시간이 지나지 않았는지 확인
        if (isPassedPaymentWaitingTime(booking, requestTime)) {
            throw new IllegalArgumentException("결제 대기 시간이 지났습니다.");
        }
    }

    private void validatePayableBookingStatus(Booking booking) {
        if (!booking.isPaymentAvailable()) {
            throw new IllegalArgumentException("결제할 수 없는 예약입니다.");
        }
    }

    private boolean isPassedPaymentWaitingTime(Booking booking, LocalDateTime requestTime) {
        return requestTime.isAfter(
            booking.getCreatedAt().plusHours(property.depositPaymentWaitHours()));
    }
}
