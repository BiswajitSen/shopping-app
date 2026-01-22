package com.shopapp.shared.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public void publish(DomainEvent event) {
        log.info("Publishing domain event: {} with id: {}", event.getEventType(), event.getEventId());
        applicationEventPublisher.publishEvent(event);
    }
}
