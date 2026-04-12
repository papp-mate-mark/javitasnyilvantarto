package otvosuzlet.javitasnyilntarto.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobGroup;
import otvosuzlet.javitasnyilntarto.model.JobImage;
import otvosuzlet.javitasnyilntarto.model.Person;
import otvosuzlet.javitasnyilntarto.repository.JobGroupRepository;
import otvosuzlet.javitasnyilntarto.repository.JobImageRepository;
import otvosuzlet.javitasnyilntarto.repository.JobRepository;
import otvosuzlet.javitasnyilntarto.service.PersonModificationPublisher;

@Aspect
@Component
public class PersonModificationAspect {

    private final PersonModificationPublisher publisher;
    private final JobRepository jobRepository;
    private final JobGroupRepository jobGroupRepository;
    private final JobImageRepository jobImageRepository;

    public PersonModificationAspect(
            PersonModificationPublisher publisher,
            JobRepository jobRepository,
            JobGroupRepository jobGroupRepository,
            JobImageRepository jobImageRepository) {
        this.publisher = publisher;
        this.jobRepository = jobRepository;
        this.jobGroupRepository = jobGroupRepository;
        this.jobImageRepository = jobImageRepository;
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String name = authentication.getName();
        if (name == null || name.equalsIgnoreCase("anonymousUser")) {
            return null;
        }
        return name;
    }

    // --- Person ---

    @AfterReturning(pointcut = "execution(* otvosuzlet.javitasnyilntarto.repository.PersonRepository.save(..))", returning = "saved")
    public void afterPersonSave(Person saved) {
        publisher.publishAfterCommit(saved == null ? null : saved.getId(), "person.save", currentActor());
    }

    @Around("execution(* otvosuzlet.javitasnyilntarto.repository.PersonRepository.delete(..)) && args(person)")
    public Object aroundPersonDelete(ProceedingJoinPoint pjp, Person person) throws Throwable {
        Integer personId = person == null ? null : person.getId();
        Object result = pjp.proceed();
        publisher.publishAfterCommit(personId, "person.delete", currentActor());
        return result;
    }

    @Around("execution(* otvosuzlet.javitasnyilntarto.repository.PersonRepository.deleteById(..)) && args(personId)")
    public Object aroundPersonDeleteById(ProceedingJoinPoint pjp, Integer personId) throws Throwable {
        Object result = pjp.proceed();
        publisher.publishAfterCommit(personId, "person.deleteById", currentActor());
        return result;
    }

    // --- JobGroup ---

    @AfterReturning(pointcut = "execution(* otvosuzlet.javitasnyilntarto.repository.JobGroupRepository.save(..))", returning = "saved")
    public void afterJobGroupSave(JobGroup saved) {
        Integer personId = null;
        if (saved != null && saved.getPerson() != null) {
            personId = saved.getPerson().getId();
        }
        if (personId == null && saved != null && saved.getId() != null) {
            personId = jobGroupRepository.findPersonIdByJobGroupId(saved.getId());
        }
        publisher.publishAfterCommit(personId, "jobGroup.save", currentActor());
    }

    @Around("execution(* otvosuzlet.javitasnyilntarto.repository.JobGroupRepository.delete(..)) && args(jobGroup)")
    public Object aroundJobGroupDelete(ProceedingJoinPoint pjp, JobGroup jobGroup) throws Throwable {
        Integer jobGroupId = jobGroup == null ? null : jobGroup.getId();
        Integer personId = jobGroupId == null ? null : jobGroupRepository.findPersonIdByJobGroupId(jobGroupId);
        Object result = pjp.proceed();
        publisher.publishAfterCommit(personId, "jobGroup.delete", currentActor());
        return result;
    }

    @Around("execution(* otvosuzlet.javitasnyilntarto.repository.JobGroupRepository.deleteById(..)) && args(jobGroupId)")
    public Object aroundJobGroupDeleteById(ProceedingJoinPoint pjp, Integer jobGroupId) throws Throwable {
        Integer personId = jobGroupId == null ? null : jobGroupRepository.findPersonIdByJobGroupId(jobGroupId);
        Object result = pjp.proceed();
        publisher.publishAfterCommit(personId, "jobGroup.deleteById", currentActor());
        return result;
    }

    // --- Job ---

    @AfterReturning(pointcut = "execution(* otvosuzlet.javitasnyilntarto.repository.JobRepository.save(..))", returning = "saved")
    public void afterJobSave(Job saved) {
        Integer jobId = saved == null ? null : saved.getId();
        Integer personId = jobId == null ? null : jobRepository.findPersonIdByJobId(jobId);
        publisher.publishAfterCommit(personId, "job.save", currentActor());
    }

    @Around("execution(* otvosuzlet.javitasnyilntarto.repository.JobRepository.delete(..)) && args(job)")
    public Object aroundJobDelete(ProceedingJoinPoint pjp, Job job) throws Throwable {
        Integer jobId = job == null ? null : job.getId();
        Integer personId = jobId == null ? null : jobRepository.findPersonIdByJobId(jobId);
        Object result = pjp.proceed();
        publisher.publishAfterCommit(personId, "job.delete", currentActor());
        return result;
    }

    @Around("execution(* otvosuzlet.javitasnyilntarto.repository.JobRepository.deleteById(..)) && args(jobId)")
    public Object aroundJobDeleteById(ProceedingJoinPoint pjp, Integer jobId) throws Throwable {
        Integer personId = jobId == null ? null : jobRepository.findPersonIdByJobId(jobId);
        Object result = pjp.proceed();
        publisher.publishAfterCommit(personId, "job.deleteById", currentActor());
        return result;
    }

    // --- JobImage ---

    @AfterReturning(pointcut = "execution(* otvosuzlet.javitasnyilntarto.repository.JobImageRepository.save(..))", returning = "saved")
    public void afterJobImageSave(JobImage saved) {
        if (saved == null || saved.getId() == null) {
            return;
        }

        // Most images are created unattached (job == null) and later linked via cascade when the Job is saved.
        // Only treat it as a "person modification" once it is attached to a job.
        if (saved.getJob() == null) {
            return;
        }

        Integer personId = jobImageRepository.findPersonIdByImageId(saved.getId());
        publisher.publishAfterCommit(personId, "jobImage.save", currentActor());
    }

    @Around("execution(* otvosuzlet.javitasnyilntarto.repository.JobImageRepository.delete(..)) && args(image)")
    public Object aroundJobImageDelete(ProceedingJoinPoint pjp, JobImage image) throws Throwable {
        Integer imageId = image == null ? null : image.getId();
        Integer personId = imageId == null ? null : jobImageRepository.findPersonIdByImageId(imageId);
        Object result = pjp.proceed();
        publisher.publishAfterCommit(personId, "jobImage.delete", currentActor());
        return result;
    }

    @Around("execution(* otvosuzlet.javitasnyilntarto.repository.JobImageRepository.deleteById(..)) && args(imageId)")
    public Object aroundJobImageDeleteById(ProceedingJoinPoint pjp, Integer imageId) throws Throwable {
        Integer personId = imageId == null ? null : jobImageRepository.findPersonIdByImageId(imageId);
        Object result = pjp.proceed();
        publisher.publishAfterCommit(personId, "jobImage.deleteById", currentActor());
        return result;
    }
}
