package otvosuzlet.javitasnyilntarto.listeners;

import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

import otvosuzlet.javitasnyilntarto.dto.WSAction;
import otvosuzlet.javitasnyilntarto.dto.WSResponse;
import otvosuzlet.javitasnyilntarto.events.PersonModifiedEvent;
import otvosuzlet.javitasnyilntarto.projections.PersonFullInfoProjection;
import otvosuzlet.javitasnyilntarto.service.PersonService;

@Component
public class PersonModifiedWebSocketNotifier {

    private final SimpMessagingTemplate messagingTemplate;
    private final PersonService personService;
    private final SimpUserRegistry simpUserRegistry;

    public PersonModifiedWebSocketNotifier(
            SimpMessagingTemplate messagingTemplate,
            PersonService personService,
            SimpUserRegistry simpUserRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.personService = personService;
        this.simpUserRegistry = simpUserRegistry;
    }

    @EventListener
    public void onPersonModified(PersonModifiedEvent event) {
        Integer personId = event.getPersonId();
        if (personId == null) {
            return;
        }

        Optional<PersonFullInfoProjection> projection = personService.findByIdFullInfoProjection(personId);
        WSResponse<PersonFullInfoProjection> response;
        if (projection.isEmpty()) {
            response = new WSResponse<>(event.getReason(), WSAction.DELETED, null, event.getActor());
        } else {
            response = new WSResponse<>(event.getReason(), WSAction.UPDATE, projection.get(), event.getActor());
        }

        String destination = "/queue/person/" + personId;
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
