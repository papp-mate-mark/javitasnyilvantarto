package otvosuzlet.javitasnyilntarto.events;

import java.time.Instant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class PersonModifiedEvent {
    private final Integer personId;
    private final String reason;
    private final String actor;
    private final Instant occurredAt = Instant.now();
}
