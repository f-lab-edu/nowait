package com.nowait.application;

import com.nowait.domain.model.booking.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepositService {

    public void validateDepositAmount(Booking booking, Integer amount) {
        // TODO: 해당 예약의 예약금과 amount가 같은지 확인하는 로직 추가
    }
}
