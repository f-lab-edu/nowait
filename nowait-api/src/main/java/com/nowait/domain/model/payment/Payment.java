package com.nowait.domain.model.payment;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    private Long bookingId;
    private String tid;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "varchar(30)")
    private PaymentType paymentType;

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


    private static void validateBookingId(Long bookingId) {
        requireNonNull(bookingId, "예약 식별자는 필수값입니다.");
    }

    private static void validateTid(String tid) {
        requireNonNull(tid, "TID는 필수값입니다.");
    }

    private static void validatePaymentType(PaymentType paymentType) {
        requireNonNull(paymentType, "결제 수단은 필수값입니다.");
    }

}
