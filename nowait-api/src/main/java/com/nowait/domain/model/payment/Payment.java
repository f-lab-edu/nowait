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

    public static Payment of(Long bookingId, String tid) {
        validateBookingId(bookingId);
        validateTid(tid);

        return new Payment(null, bookingId, tid);
    }

    private static void validateBookingId(Long bookingId) {
        requireNonNull(bookingId, "예약 식별자는 필수값입니다.");
    }

    private static void validateTid(String tid) {
        requireNonNull(tid, "TID는 필수값입니다.");
    }

}
