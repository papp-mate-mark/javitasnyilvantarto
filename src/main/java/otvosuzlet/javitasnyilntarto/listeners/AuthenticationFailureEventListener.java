package otvosuzlet.javitasnyilntarto.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


@Component
public class AuthenticationFailureEventListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {
    private static final Logger logger = LoggerFactory.getLogger("fileLogger");


    @Override
    public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        Authentication auth = event.getAuthentication();
        logger.info("Login failed: " + auth.getName() + " | Reason: " + event.getException().getMessage());
    }
}
