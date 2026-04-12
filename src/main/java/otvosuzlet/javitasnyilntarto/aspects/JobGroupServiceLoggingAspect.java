package otvosuzlet.javitasnyilntarto.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class JobGroupServiceLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger("fileLogger");

    @Pointcut("execution(* otvosuzlet.javitasnyilntarto.service.JobGroupServiceImpl.*(..))")
    public void jobGroupServiceMethods() {
        return;
    }

    @Before("jobGroupServiceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        if (logger.isDebugEnabled()) {
            String methodName = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();
            logger.debug("Entering method: {} with arguments: {}", methodName, Arrays.toString(args));
        }
    }

    @AfterReturning(pointcut = "jobGroupServiceMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        if (logger.isDebugEnabled()) {
            String methodName = joinPoint.getSignature().getName();
            // You can adjust this to avoid logging full binary or PDF content.
            if (result instanceof byte[]) {
                logger.debug("Method {} returned byte[] of length {}", methodName, ((byte[]) result).length);
            } else {
                logger.debug("Method {} returned: {}", methodName, result);
            }
        }
    }

    
}
