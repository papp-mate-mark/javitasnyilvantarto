package otvosuzlet.javitasnyilntarto.aspects;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class JobImageLogginAspect {
     private static final Logger logger = LoggerFactory.getLogger("fileLogger");

    // Intercept the constructor of JobImage
    @Before("execution(otvosuzlet.javitasnyilntarto.model.JobImage.new(..))")
    public void logBeforeConstructor() {
        logger.debug("JobImage constructor is being called.");
    }

    // Log if an exception is thrown by the constructor
    @AfterThrowing(pointcut = "execution(otvosuzlet.javitasnyilntarto.model.JobImage.new(..))", throwing = "exception")
    public void logConstructorException(Throwable exception) {
        logger.error("Error occurred during the construction of JobImage: " + exception.getMessage(), exception);
    }
}
