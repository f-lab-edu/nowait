package com.nowait.application.event;

import com.nowait.domain.model.booking.Booking;
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
