package otvosuzlet.javitasnyilntarto.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import otvosuzlet.javitasnyilntarto.events.PersonModifiedEvent;

@Component
public class PersonModificationPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public PersonModificationPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishAfterCommit(Integer personId, String reason) {
        publishAfterCommit(personId, reason, null);
    }

    public void publishAfterCommit(Integer personId, String reason, String actor) {
        if (personId == null) {
            return;
        }

        Runnable publishAction = () -> {
            eventPublisher.publishEvent(new PersonModifiedEvent(personId, reason, actor));
        };

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishAction.run();
                }
            });
        } else {
            publishAction.run();
        }
    }
}
