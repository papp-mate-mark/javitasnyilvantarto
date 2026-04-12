package otvosuzlet.javitasnyilntarto.service;

import otvosuzlet.javitasnyilntarto.dto.JobCompleteDTO;
import otvosuzlet.javitasnyilntarto.dto.JobPickedUpDTO;
import otvosuzlet.javitasnyilntarto.dto.JobSearchDto;
import otvosuzlet.javitasnyilntarto.dto.JobSearchJobDataDTO;
import otvosuzlet.javitasnyilntarto.model.Job;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface JobService {
    /**
     * Searches for jobs based on the provided filter and returns paginated results.
     *
     * @param filter the job search filter containing search criteria
     * @param pageable the pagination information (page number, size, sorting)
     * @return a Page containing JobSearchJobDataDTO objects that match the search criteria
     */
    Page<JobSearchJobDataDTO> searchForJob(JobSearchDto filter, Pageable pageable);


    /**
     * Validates a the job complition data based on the already existing job data, 
     * and if invalid, throws an exception with the validation errors. 
     * 
     * @param id The ID of the job to validate and update.
     * @param jobCompleteData The data to validate and update the job with.
     */
    void validateAndCompleteJob(Integer id, JobCompleteDTO jobCompleteData);

    /**
     * Validates the job pick-up data based on the already existing job data, 
     * and if invalid, throws an exception with the validation errors. 
     * 
     * @param id The ID of the job to validate and update.
     * @param jobPickedUpData The data to validate and update the job with.
     */
    void pickedUpJob(Integer id, JobPickedUpDTO jobPickedUpData);

    /**
     * Generates a summary of the specified job.
     * 
     * @param id The ID of the job for which to retrieve the summary.
     * @return A byte array representing the summary PDF document.
     */
    byte[] getSummary(Integer id);

    /**
     * Deletes a job from the system by its ID.
     * Deletion will cascade to all children.
     * 
     * @param id The ID of the job to delete.
     */
    void deleteJob(Integer id);

    /**
     * Retrieves a job by its ID.
     * 
     * @param id The ID of the job to retrieve.
     * @return The Job object with the specified ID.
     */
    Job getJobById(Integer id);
}
