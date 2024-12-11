package com.nowait.domain.model.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BookingSlotUnitTest {

    BookingSlot slot;

    @BeforeEach
    void setUp() {
        slot = new BookingSlot();
    }

    @DisplayName("슬롯을 예약할 수 있다.")
    @Test
    void book() {
        // when
        slot.book();

        // then
        assertThat(slot.isBooked()).isTrue();
    }

    @DisplayName("이미 예약된 슬롯을 예약할 수 없다.")
    @Test
    void bookAlreadyBookedSlot() {
        // given
        slot.book();

        // when & then
        assertThatThrownBy(() -> slot.book())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 예약된 슬롯입니다.");
    }
}
