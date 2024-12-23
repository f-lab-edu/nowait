package com.nowait.domain.model.booking;

public class NoneDepositPolicy implements DepositPolicy {

    @Override
    public int getDepositAmount(Booking booking) {
        return 0;
    }
}
