package otvosuzlet.javitasnyilntarto.listeners;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

import otvosuzlet.javitasnyilntarto.dto.ActiveJobsRequestDTO;
import otvosuzlet.javitasnyilntarto.dto.WSAction;
import otvosuzlet.javitasnyilntarto.dto.WSResponse;
import otvosuzlet.javitasnyilntarto.events.PersonModifiedEvent;
import otvosuzlet.javitasnyilntarto.service.JobGroupService;

@Component
public class ActiveJobsWebSocketNotifier {

    private final SimpMessagingTemplate messagingTemplate;
    private final JobGroupService jobGroupService;
    private final SimpUserRegistry simpUserRegistry;

    public ActiveJobsWebSocketNotifier(
            SimpMessagingTemplate messagingTemplate,
            JobGroupService jobGroupService,
            SimpUserRegistry simpUserRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.jobGroupService = jobGroupService;
        this.simpUserRegistry = simpUserRegistry;
    }

    @EventListener
    public void onPersonModified(PersonModifiedEvent event) {
        ActiveJobsRequestDTO activeJobs = jobGroupService.getActiveJobsGroups();
        WSResponse<ActiveJobsRequestDTO> response = new WSResponse<>(event.getReason(), WSAction.UPDATE, activeJobs, event.getActor());

        String destination = "/queue/active-jobs";
        String actor = event.getActor();
        for (SimpUser user : simpUserRegistry.getUsers()) {
            String username = user.getName();
            if (actor != null && actor.equals(username)) {
                continue;
            }
            messagingTemplate.convertAndSendToUser(username, destination, response);
        }
    }
}
