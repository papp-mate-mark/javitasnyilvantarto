package otvosuzlet.javitasnyilntarto.aspects;


import otvosuzlet.javitasnyilntarto.dto.PersonRequest;
import otvosuzlet.javitasnyilntarto.model.Person;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PersonLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger("fileLogger");

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.PersonServiceImpl.createPerson(..))")
    public void logCreatePerson(JoinPoint joinPoint) {
        Person person = (Person) joinPoint.getArgs()[0];
        logger.debug("createPerson called with: " + person.getId());
    }

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.PersonServiceImpl.deletePerson(..))")
    public void logDeletePerson(JoinPoint joinPoint) {
        Integer id = (Integer) joinPoint.getArgs()[0];
        logger.debug("deletePerson called with ID: " + id);
    }

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.PersonServiceImpl.findById(..))")
    public void logFindById(JoinPoint joinPoint) {
        Integer id = (Integer) joinPoint.getArgs()[0];
        logger.debug("findById called with ID: " + id);
    }

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.PersonServiceImpl.findByIdFullInfoProjection(..))")
    public void logFindByIdFullInfoProjection(JoinPoint joinPoint) {
        Integer id = (Integer) joinPoint.getArgs()[0];
        logger.debug("findByIdFullInfoProjection called with ID: " + id);
    }

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.PersonServiceImpl.searchForPerson(..))")
    public void logSearchForPerson(JoinPoint joinPoint) {
        logger.debug("searchForPerson called");
    }

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.PersonServiceImpl.createPersonWithJobGroup(..))")
    public void logCreatePersonWithJobGroup(JoinPoint joinPoint) {
        PersonRequest request = (PersonRequest) joinPoint.getArgs()[0];
        logger.debug("createPersonWithJobGroup called with: " + request);
    }
}
