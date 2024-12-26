package com.nowait.domain.model.payment;

import static java.util.Objects.requireNonNull;

import com.nowait.application.dto.response.payment.PaymentResult;
import com.nowait.domain.model.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "payment")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "tid", nullable = false)
    private String tid;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "varchar(30)")
    private PaymentType paymentType;

    @Column(name = "total_amount")
    private int totalAmount;

    @Column(name = "ready_at")
    private LocalDateTime readyAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    public static Payment of(Long bookingId, String tid, PaymentType paymentType,
        int amount, LocalDateTime readyAt) {
        validateBookingId(bookingId);
        validateTid(tid);
        validatePaymentType(paymentType);

        return new Payment(
            null,
            bookingId,
            tid,
            paymentType,
            amount,
            readyAt,
            null,
            null);
    }

    public void updatePaymentResult(PaymentResult result) {
        requireNonNull(result, "결제 결과는 필수값입니다.");
        requireNonNull(result.approvedAt(), "결제 승인 시각은 필수값입니다.");
        validateAmount(result.totalAmount());

        this.approvedAt = result.approvedAt();
    }

    private static void validateBookingId(Long bookingId) {
        requireNonNull(bookingId, "예약 식별자는 필수값입니다.");
    }

    private static void validateTid(String tid) {
        requireNonNull(tid, "TID는 필수값입니다.");
    }

    private static void validatePaymentType(PaymentType paymentType) {
        requireNonNull(paymentType, "결제 수단은 필수값입니다.");
    }

    private void validateAmount(int payAmount) {
        if (this.totalAmount != payAmount) {
            log.error("결제 금액이 일치하지 않습니다. [결제금액: {}, 결제승인금액: {}]", this.totalAmount, payAmount);
        }
    }
}
