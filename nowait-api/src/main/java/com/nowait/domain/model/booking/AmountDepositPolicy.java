package com.nowait.domain.model.booking;

public class AmountDepositPolicy implements DepositPolicy {

    private final int amount;

    public AmountDepositPolicy(int amount) {
        this.amount = amount;
    }

    @Override
    public int getDepositAmount(Booking booking) {
        return amount * booking.getPartySize();
    }
}
