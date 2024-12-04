package com.nowait.booking.application;

import com.nowait.booking.domain.model.Booking;
import com.nowait.common.event.BookedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Async
    public void publishBookedEvent(Booking booking, Long placeId) {
        eventPublisher.publishEvent(
            new BookedEvent(booking.getId(), placeId, booking.getUserId()));
    }
}
