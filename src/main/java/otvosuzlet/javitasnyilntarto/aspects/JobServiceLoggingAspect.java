package otvosuzlet.javitasnyilntarto.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import otvosuzlet.javitasnyilntarto.Utilities.StringUtils;
import otvosuzlet.javitasnyilntarto.dto.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@Aspect
@Component
public class JobServiceLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger("fileLogger");

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.JobServiceImpl.completeJob(..)) && args(id, jobCompleteData)")
    public void logBeforeCompleteJob(Integer id, JobCompleteDTO jobCompleteData) {
        if (logger.isDebugEnabled()) {
            logger.debug("Requested change to done for id: " + id +
                    "\n\tDate: " + (jobCompleteData.getDate() == null ? "nincs megadva" : jobCompleteData.getDate()) +
                    " Note: " + (jobCompleteData.getNote() == null ? "nincs megadva" : jobCompleteData.getNote()) +
                    " Price: " + (jobCompleteData.getPrice() == null ? "nincs megadva" : jobCompleteData.getPrice()) +
                    " Images After: " + (jobCompleteData.getImagesAfter() == null ? 0 : jobCompleteData.getImagesAfter().size()) + " image");
        }
    }

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.JobServiceImpl.pickedUpJob(..)) && args(id, jobPickedUpData)")
    public void logBeforePickedUpJob(Integer id, JobPickedUpDTO jobPickedUpData) {
        if (logger.isDebugEnabled()) {
            logger.debug("Requested change to pickedup for id: " + id +
                    " Date: " + (jobPickedUpData.getDate() == null ? "nincs megadva" : jobPickedUpData.getDate()));
        }
    }

    

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.JobServiceImpl.searchForJob(..)) && args(filter, pageable)")
    public void logBeforeSearchForJob(JobSearchDto filter, Pageable pageable) {
        if (logger.isDebugEnabled()) {
            String logMessage = String.format(
                    "Keresési paraméterek: " +
                            "Név: \"%s\", Cím: \"%s\", Telefonszám: \"%s\", Tárgy neve: \"%s\", Leírás: \"%s\", Anyag: \"%s\", " +
                            "Végső ár (min-max): %s - %s, Súly (min-max): %s - %s, " +
                            "Feltöltés időszak: %s - %s, Elkészülés időszak: %s - %s, Átvétel időszak: %s - %s, Határidő időszak: %s - %s, " +
                            "Kész jegyzet: \"%s\", Feltöltés jegyzet: \"%s\", Oldal: %s, Limit: %s",
                    StringUtils.nullable(filter.getName()),
                    StringUtils.nullable(filter.getAddress()),
                    StringUtils.nullable(filter.getPhone()),
                    StringUtils.nullable(filter.getObjectname()),
                    StringUtils.nullable(filter.getDescription()),
                    StringUtils.nullable(filter.getMaterial()),
                    StringUtils.nullable(filter.getFinalpricemin()),
                    StringUtils.nullable(filter.getFinalpricemax()),
                    StringUtils.nullable(filter.getWeightmin()),
                    StringUtils.nullable(filter.getWeightmax()),
                    StringUtils.nullable(filter.getUploadstart()),
                    StringUtils.nullable(filter.getUploadend()),
                    StringUtils.nullable(filter.getDonestart()),
                    StringUtils.nullable(filter.getDoneend()),
                    StringUtils.nullable(filter.getPickupstart()),
                    StringUtils.nullable(filter.getPickupend()),
                    StringUtils.nullable(filter.getDeadlinestart()),
                    StringUtils.nullable(filter.getDeadlineend()),
                    StringUtils.nullable(filter.getDonenote()),
                        StringUtils.nullable(filter.getUploadnote()),
                    pageable == null ? null : pageable.getPageNumber(),
                    pageable == null ? null : pageable.getPageSize()
            );
            logger.debug("Full search requested. {}", logMessage);
        }
    }

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.JobServiceImpl.setDoneDate(..)) && args(id, doneDate)")
    public void logBeforeSetDoneDate(Integer id, LocalDateTime doneDate) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting done date for job id: {}, date: {}", id, doneDate);
        }
    }

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.JobServiceImpl.setPickupDate(..)) && args(id, pickupDate)")
    public void logBeforeSetPickupDate(Integer id, LocalDateTime pickupDate) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting pickup date for job id: {}, date: {}", id, pickupDate);
        }
    }

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.JobServiceImpl.getJobCount(..))")
    public void logBeforeGetJobCount() {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting total job count.");
        }
    }

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.JobServiceImpl.deleteJob(..)) && args(id)")
    public void logBeforeDeleteJob(Integer id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Request to delete job with id: {}", id);
        }
    }

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.JobServiceImpl.getSummary(..)) && args(id)")
    public void logBeforeGetSummary(Integer id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Request to get job summary for id: {}", id);
        }
    }

    @Before("execution(* otvosuzlet.javitasnyilntarto.service.JobServiceImpl.getJobById(..)) && args(id)")
    public void logBeforeGetJobById(Integer id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Request to fetch job by id: {}", id);
        }
    }
}
