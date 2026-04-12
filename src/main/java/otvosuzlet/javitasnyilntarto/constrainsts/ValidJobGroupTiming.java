package otvosuzlet.javitasnyilntarto.constrainsts;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidJobGroupTimingValidator.class)
public @interface ValidJobGroupTiming {
    String message() default "validation.invalid.jobgroup.timing";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}