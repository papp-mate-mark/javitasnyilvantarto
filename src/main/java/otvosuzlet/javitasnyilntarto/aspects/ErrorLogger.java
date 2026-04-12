package otvosuzlet.javitasnyilntarto.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import otvosuzlet.javitasnyilntarto.exceptions.OrderImpossibleException;
import otvosuzlet.javitasnyilntarto.exceptions.RuntimeExceptionWithCode;

@Aspect
@Component
public class ErrorLogger {

    private static final Logger logger = LoggerFactory.getLogger(ErrorLogger.class);

    @AfterThrowing(
        pointcut = "(execution(* otvosuzlet.javitasnyilntarto.service..*.*(..)) || execution(* otvosuzlet.javitasnyilntarto.controllers..*.*(..))) && !within(otvosuzlet.javitasnyilntarto.aspects..*)",
        throwing = "ex"
    )
    public void logCustomExceptions(JoinPoint joinPoint, Throwable ex) {
        String method = joinPoint.getSignature().toShortString();

        if (ex instanceof RuntimeExceptionWithCode runtimeWithCode) {
            logger.error(
                "RuntimeExceptionWithCode in {}: key={}, status={}, message={}",
                method,
                runtimeWithCode.getErrorCode(),
                runtimeWithCode.getStatus(),
                runtimeWithCode.getMessage(),
                runtimeWithCode
            );
        } else if (ex instanceof OrderImpossibleException oie) {
            logger.error("OrderImpossibleException in {}: {} | Source: {}", method, oie.getMessage(), oie.getSource(),
                    oie);
        } else {
            logger.error("Unexpected exception in {}: {}", method, ex.getMessage(), ex);
        }
    }
}
