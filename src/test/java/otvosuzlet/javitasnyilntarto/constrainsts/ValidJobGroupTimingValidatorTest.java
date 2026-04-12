package otvosuzlet.javitasnyilntarto.constrainsts;

import jakarta.validation.Validator;
import otvosuzlet.javitasnyilntarto.dto.JobDto;
import otvosuzlet.javitasnyilntarto.dto.JobGroupDto;
import otvosuzlet.javitasnyilntarto.testutil.TestObjectGenerator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Set;
import jakarta.validation.ConstraintViolation;

class ValidJobGroupTimingValidatorTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


    @Test
    void shouldFailWhenFinalPriceSetButFinishTimeMissing() {
        JobGroupDto group = TestObjectGenerator.createFullyInicilizedJobGroupDto();
        JobDto job = TestObjectGenerator.createFullyInicilizedJobDto(group);
        job.setFinalPrice(1000);
        job.setFinishTime(null);

        group.setJobs(List.of(job));

        Set<ConstraintViolation<JobGroupDto>> violations = validator.validate(group);

        assertTrue(violations.stream()
            .anyMatch(v -> "validation.finishTime.requiredIfFinalPriceIsSet".equals(v.getMessageTemplate())));
    }

    @Test
    void shouldFailWhenFinishDateIsMissingButEverythingElseIsSet(){
        JobGroupDto group = TestObjectGenerator.createFullyInicilizedJobGroupDto();
        JobDto job = TestObjectGenerator.createFullyInicilizedJobDto(group);
        job.setFinishTime(null);
        job.setPickedUpTime(TestObjectGenerator.generateRandomLocalDateTime(group.getBringin(), group.getDeadline()));

        group.setJobs(List.of(job));

        Set<ConstraintViolation<JobGroupDto>> violations = validator.validate(group);

        Set<String> expectedCodes = Set.of(
            "validation.finishTime.requiredIfFinalPriceIsSet",
            "validation.finishTime.requiredIfFinishNoteIsSet",
            "validation.finishTime.requiredIfImagesAfterIsSet"
        );

        assertTrue(violations.stream()
            .filter(v -> expectedCodes.contains(v.getMessageTemplate()))
            .count() == 3);
    }

}