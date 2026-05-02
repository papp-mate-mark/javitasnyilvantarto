package otvosuzlet.javitasnyilntarto.service;

import static org.mockito.Mockito.verify;

import org.mockito.Mockito;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import otvosuzlet.javitasnyilntarto.events.PersonModifiedEvent;

class PersonModificationPublisherTest {

    private ApplicationEventPublisher eventPublisher;
    private PersonModificationPublisher publisher;

    @BeforeEach
    void setUp() {
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        publisher = new PersonModificationPublisher(eventPublisher);
    }

    @Test
    void publishAfterCommitShouldPublishEvent() {
        publisher.publishAfterCommit(42, "CREATED", "testuser2");

        ArgumentCaptor<PersonModifiedEvent> eventCaptor = ArgumentCaptor.forClass(PersonModifiedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        PersonModifiedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(42, capturedEvent.getPersonId());
        assertEquals("CREATED", capturedEvent.getReason());
        assertEquals("testuser2", capturedEvent.getActor());
    }
}
