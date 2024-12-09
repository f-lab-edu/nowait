package com.nowait.domain.model.booking;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.nowait.domain.model.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "booking")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "booking_slot_id", nullable = false, unique = true)
    private Long bookingSlotId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(20)")
    private BookingStatus status;

    @Column(name = "party_size")
    private Integer partySize;

    public static Booking of(Long userId, BookingSlot slot, Integer partySize) {
        validateUserId(userId);
        validateBookingSlot(slot);
        validatePartySize(partySize);

        slot.book();

        return new Booking(
            null,
            slot.getId(),
            userId,
            BookingStatus.getStatusAfterBook(slot),
            partySize
        );
    }

    private static void validateUserId(Long userId) {
        if (isNull(userId)) {
            throw new IllegalArgumentException("예약자 식별자는 필수값입니다.");
        }
    }

    private static void validateBookingSlot(BookingSlot slot) {
        if (isNull(slot)) {
            throw new IllegalArgumentException("예약 슬롯은 필수값입니다.");
        }
    }

    private static void validatePartySize(Integer partySize) {
        if (nonNull(partySize) && partySize <= 0) {
            throw new IllegalArgumentException("인원 수는 0이상 입니다.");
        }
    }
}
