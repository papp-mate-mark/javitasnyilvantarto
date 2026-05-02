package otvosuzlet.javitasnyilntarto.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import otvosuzlet.javitasnyilntarto.dto.ActiveJobsRequestDTO;
import otvosuzlet.javitasnyilntarto.dto.WSAction;
import otvosuzlet.javitasnyilntarto.dto.WSResponse;
import otvosuzlet.javitasnyilntarto.events.PersonModifiedEvent;
import otvosuzlet.javitasnyilntarto.service.JobGroupService;

class ActiveJobsWebSocketNotifierTest {

    private SimpMessagingTemplate messagingTemplate;
    private JobGroupService jobGroupService;
    private SimpUserRegistry simpUserRegistry;
    private ActiveJobsWebSocketNotifier notifier;

    @BeforeEach
    void setUp() {
        messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
        jobGroupService = Mockito.mock(JobGroupService.class);
        simpUserRegistry = Mockito.mock(SimpUserRegistry.class);
        notifier = new ActiveJobsWebSocketNotifier(messagingTemplate, jobGroupService, simpUserRegistry);
    }

    @Test
    void onPersonModifiedShouldBroadcastActiveJobsToOtherUsers() {
        PersonModifiedEvent event = new PersonModifiedEvent(7, "UPDATED", "testuser1");
        ActiveJobsRequestDTO activeJobs = new ActiveJobsRequestDTO();
        Mockito.when(jobGroupService.getActiveJobsGroups()).thenReturn(activeJobs);

        SimpUser testuser1 = Mockito.mock(SimpUser.class);
        SimpUser testuser2 = Mockito.mock(SimpUser.class);
        Mockito.when(testuser1.getName()).thenReturn("testuser1");
        Mockito.when(testuser2.getName()).thenReturn("testuser2");
        Mockito.when(simpUserRegistry.getUsers()).thenReturn(Set.of(testuser1, testuser2));

        notifier.onPersonModified(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<WSResponse<ActiveJobsRequestDTO>> responseCaptor = ArgumentCaptor.forClass(WSResponse.class);
        Mockito.verify(messagingTemplate).convertAndSendToUser(Mockito.eq("testuser2"), Mockito.eq("/queue/active-jobs"), responseCaptor.capture());
        assertEquals(WSAction.UPDATE, responseCaptor.getValue().getAction());
        assertEquals("UPDATED", responseCaptor.getValue().getReason());
        assertEquals("testuser1", responseCaptor.getValue().getActor());
        assertEquals(activeJobs, responseCaptor.getValue().getPayload());

        Mockito.verify(messagingTemplate, Mockito.never()).convertAndSendToUser(Mockito.eq("testuser1"), Mockito.anyString(), Mockito.any());
        Mockito.verify(jobGroupService).getActiveJobsGroups();
    }
}