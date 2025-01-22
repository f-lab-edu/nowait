package com.nowait.application;

import com.nowait.application.dto.response.payment.ApproveFailure;
import com.nowait.application.dto.response.payment.ApproveRes;
import com.nowait.application.dto.response.payment.PaymentTokenRes;
import com.nowait.application.dto.response.payment.SimplePaymentRes;
import com.nowait.config.PaymentProperties;
import com.nowait.controller.api.dto.response.ApiResult;
import com.nowait.domain.model.booking.Booking;
import com.nowait.domain.model.booking.BookingSlot;
import com.nowait.domain.model.booking.BookingStatus;
import com.nowait.domain.model.payment.Payment;
import com.nowait.domain.model.payment.PaymentStatus;
import com.nowait.domain.model.payment.PaymentToken;
import com.nowait.domain.repository.PaymentRepository;
import com.nowait.domain.repository.PaymentTokenRepository;
import com.nowait.utils.ShortUUIDGenerator;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PaymentService {

    private static final int MAX_RETRIES = 3;

    private final PaymentProperties property;
    private final PaymentRepository paymentRepository;
    private final PaymentTokenRepository paymentTokenRepository;
    private final BookingService bookingService;
    private final DepositService depositService;
    private final PaymentExecutor paymentExecutor;

    @Transactional
    public PaymentTokenRes ready(Long loginId, Long bookingId, Integer amount,
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

        // 4. PaymentToken 생성 및 저장
        String token = ShortUUIDGenerator.generate();
        paymentTokenRepository.save(PaymentToken.of(token, payment.getId()));

        return new PaymentTokenRes(token);
    }

    @Transactional
    public SimplePaymentRes approve(Long loginId, String paymentToken, String paymentKey,
        Long bookingId, Integer amount, LocalDateTime requestAt) {
        // 1. 필요한 엔티티 조회
        PaymentToken token = paymentTokenRepository.findById(paymentToken)
            .orElseThrow(() -> new IllegalArgumentException("승인 대기 시간이 지났습니다."));
        Payment payment = paymentRepository.findById(token.getPaymentId())
            .orElseThrow(() -> new IllegalArgumentException("결제 정보가 존재하지 않습니다."));
        Booking booking = bookingService.getById(payment.getBookingId());

        // 2-1. 검증 - 예약자 본인인지 확인
        booking.validateOwner(loginId);
        // 2-2. 검증 - 결제 정보가 저장된 정보와 같은지 확인
        payment.validateDetails(paymentKey, bookingId, amount);
        // 2-3. 검증 - 예약이 결제 진행 중인지 확인
        validatePayableBookingStatus(booking);
        // 2-4. 검증 - 결제 승인 대기 시간이 지나지 않았는지 확인
        validateApprovalNotExpired(payment, requestAt);

        // 3. PaymentKey 저장
        payment.setPaymentKey(paymentKey);

        // 4. 결제 승인 (비동기)
        CompletableFuture.supplyAsync(
                () -> paymentExecutor.executeApproval(bookingId, amount, paymentKey))
            .thenAccept(result -> {
                // 4-1. 승인 성공
                ApproveRes data = result.data();
                if (result.status() == HttpStatus.OK) {
                    approveSuccessfully(data);
                    return;
                }

                // 4-2. 승인 실패
                approveFail(data);

                // 4-3. 재시도 가능한 경우 재시도
                ApproveFailure failure = data.failure();
                if (failure.retrievable()) {
                    retryApproval(bookingId, amount, paymentKey, payment);
                }
            });

        // 5. 결제 상태 변경
        payment.changeStatusTo(PaymentStatus.IN_PROGRESS);

        return SimplePaymentRes.of(payment);
    }

    @Transactional
    public void approveSuccessfully(ApproveRes data) {
        // 1. 결제 조회
        Payment payment = paymentRepository.findByPaymentKey(data.paymentKey())
            .orElseThrow(() -> new IllegalArgumentException("결제 정보가 존재하지 않습니다."));

        // 2. 결제 상태 변경
        payment.changeStatusTo(PaymentStatus.DONE);

        // 2. 예약 상태 변경
        Booking booking = bookingService.getById(payment.getBookingId());
        BookingSlot slot = bookingService.getBookingSlotById(booking.getBookingSlotId());
        booking.changeStatusTo(BookingStatus.getStatusAfterPayment(slot));
    }

    @Transactional
    public void approveFail(ApproveRes data) {
        // 1. 결제 조회
        Payment payment = paymentRepository.findByPaymentKey(data.paymentKey())
            .orElseThrow(() -> new IllegalArgumentException("결제 정보가 존재하지 않습니다."));

        // 2. 결제 상태 변경
        payment.changeStatusTo(PaymentStatus.ABORTED);

        // 3. 실패 사유 저장
        ApproveFailure failure = data.failure();
        payment.fail(failure.code(), failure.message());
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

    private void validateApprovalNotExpired(Payment payment, LocalDateTime requestAt) {
        if (isPassedApproveWaitingTime(payment, requestAt)) {
            throw new IllegalArgumentException("결제 승인 대기 시간이 지났습니다.");
        }
    }

    private boolean isPassedApproveWaitingTime(Payment payment, LocalDateTime requestTime) {
        return requestTime.isAfter(
            payment.getCreatedAt().plusMinutes(property.approvalWaitMinutes()));
    }

    private void retryApproval(Long bookingId, Integer amount, String paymentKey, Payment payment) {
        for (int retryCount = 0; retryCount < MAX_RETRIES; retryCount++) {
            try {
                // 1. 일정 시간 대기
                TimeUnit.SECONDS.sleep(1);

                // 2. 재시도 실행
                ApiResult<ApproveRes> result = paymentExecutor.executeApproval(bookingId, amount,
                    paymentKey);

                // 성공 시 상태 변경 및 종료
                if (result.status() == HttpStatus.OK) {
                    approveSuccessfully(result.data());
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("결제 승인 재시도 중 인터럽트 발생", e);
            } catch (Exception e) {
                log.error("재시도 실패: " + e.getMessage());
            }
        }

        // 최대 재시도 후 실패 처리
        log.error("최대 재시도 횟수 초과: 결제 승인 재시도 실패");
    }
}
