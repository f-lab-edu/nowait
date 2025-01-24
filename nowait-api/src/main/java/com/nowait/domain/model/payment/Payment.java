package com.nowait.domain.model.payment;

import com.nowait.domain.model.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "payment_key")
    private String paymentKey;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    public static Payment of(Long bookingId, Long userId, Integer amount) {
        return new Payment(null, bookingId, userId, null, PaymentStatus.READY, amount, null, null);
    }

    public void validateDetails(String paymentKey, Long bookingId, Integer amount) {
        if (!Objects.equals(paymentKey, this.paymentKey) || !Objects.equals(bookingId,
            this.bookingId) || !Objects.equals(amount, this.amount)) {
            throw new IllegalArgumentException("결제 정보가 일치하지 않습니다.");
        }
    }

    public void changeStatusTo(PaymentStatus paymentStatus) {
        this.status = paymentStatus;
    }

    public void fail(String code, String message) {
        this.status = PaymentStatus.ABORTED;
        this.errorCode = code;
        this.errorMessage = message;
    }

    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }
}

