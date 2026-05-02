package otvosuzlet.javitasnyilntarto.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import otvosuzlet.javitasnyilntarto.dto.WSAction;
import otvosuzlet.javitasnyilntarto.dto.WSResponse;
import otvosuzlet.javitasnyilntarto.events.PersonModifiedEvent;
import otvosuzlet.javitasnyilntarto.projections.JobGroupFullInfoProjection;
import otvosuzlet.javitasnyilntarto.projections.PersonFullInfoProjection;
import otvosuzlet.javitasnyilntarto.service.PersonService;

class PersonModifiedWebSocketNotifierTest {

    private SimpMessagingTemplate messagingTemplate;
    private PersonService personService;
    private SimpUserRegistry simpUserRegistry;
    private PersonModifiedWebSocketNotifier notifier;

    @BeforeEach
    void setUp() {
        messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
        personService = Mockito.mock(PersonService.class);
        simpUserRegistry = Mockito.mock(SimpUserRegistry.class);
        notifier = new PersonModifiedWebSocketNotifier(messagingTemplate, personService, simpUserRegistry);
    }

    @Test
    void onPersonModifiedShouldBroadcastUpdatedPersonToOtherUsers() {
        PersonModifiedEvent event = new PersonModifiedEvent(10, "UPDATED", "testuser1");
        PersonFullInfoProjection projection = Mockito.mock(PersonFullInfoProjection.class);
        Mockito.when(projection.getId()).thenReturn(10);
        Mockito.when(projection.getName()).thenReturn("John Doe");
        Mockito.when(projection.getPhone()).thenReturn("123");
        Mockito.when(projection.getAddress()).thenReturn("Main street");
        Mockito.when(projection.getJobGroups()).thenReturn(Set.<JobGroupFullInfoProjection>of());
        Mockito.when(personService.findByIdFullInfoProjection(10)).thenReturn(Optional.of(projection));

        SimpUser testuser1 = Mockito.mock(SimpUser.class);
        SimpUser testuser2 = Mockito.mock(SimpUser.class);
        Mockito.when(testuser1.getName()).thenReturn("testuser1");
        Mockito.when(testuser2.getName()).thenReturn("testuser2");
        Mockito.when(simpUserRegistry.getUsers()).thenReturn(Set.of(testuser1, testuser2));

        notifier.onPersonModified(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<WSResponse<PersonFullInfoProjection>> responseCaptor = ArgumentCaptor.forClass(WSResponse.class);
        Mockito.verify(messagingTemplate).convertAndSendToUser(Mockito.eq("testuser2"), Mockito.eq("/queue/person/10"), responseCaptor.capture());
        assertEquals(WSAction.UPDATE, responseCaptor.getValue().getAction());
        assertEquals("UPDATED", responseCaptor.getValue().getReason());
        assertEquals("testuser1", responseCaptor.getValue().getActor());
        assertEquals(projection, responseCaptor.getValue().getPayload());

        Mockito.verify(messagingTemplate, Mockito.never()).convertAndSendToUser(Mockito.eq("testuser1"), Mockito.anyString(), Mockito.any());
        Mockito.verify(personService).findByIdFullInfoProjection(10);
    }

    @Test
    void onPersonModifiedShouldBroadcastDeletedMessage() {
        PersonModifiedEvent event = new PersonModifiedEvent(10, "DELETED", "testuser1");
        Mockito.when(personService.findByIdFullInfoProjection(10)).thenReturn(Optional.empty());

        SimpUser testuser1 = Mockito.mock(SimpUser.class);
        SimpUser testuser2 = Mockito.mock(SimpUser.class);
        Mockito.when(testuser1.getName()).thenReturn("testuser1");
        Mockito.when(testuser2.getName()).thenReturn("testuser2");
        Mockito.when(simpUserRegistry.getUsers()).thenReturn(Set.of(testuser1, testuser2));

        notifier.onPersonModified(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<WSResponse<PersonFullInfoProjection>> responseCaptor = ArgumentCaptor.forClass(WSResponse.class);
        Mockito.verify(messagingTemplate).convertAndSendToUser(Mockito.eq("testuser2"), Mockito.eq("/queue/person/10"), responseCaptor.capture());
        assertEquals(WSAction.DELETED, responseCaptor.getValue().getAction());
        assertEquals("DELETED", responseCaptor.getValue().getReason());
        assertEquals(null, responseCaptor.getValue().getPayload());
    }

}