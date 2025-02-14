package com.nowait.domain.model.booking;

import com.nowait.domain.model.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "booking_slot")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookingSlot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "time", nullable = false)
    private LocalTime time;

    @Column(name = "is_booked")
    private boolean isBooked;

    @Column(name = "deposit_required")
    private boolean depositRequired;

    @Column(name = "confirm_required")
    private boolean confirmRequired;

    @Column(name = "deposit_policy_id")
    private Long depositPolicyId;

    public void setBooked(boolean isBooked) {
        if (isBooked) {
            validateBookingPossible();
        }

        this.isBooked = isBooked;
    }

    private void validateBookingPossible() {
        if (isBooked()) {
            throw new IllegalArgumentException("이미 예약된 슬롯입니다.");
        }
    }
}
