package otvosuzlet.javitasnyilntarto.service;
import otvosuzlet.javitasnyilntarto.dto.ActiveJobsRequestDTO;
import otvosuzlet.javitasnyilntarto.dto.JobGroupDto;
import otvosuzlet.javitasnyilntarto.dto.JobGroupUploadResponse;
import otvosuzlet.javitasnyilntarto.dto.JobPickedUpDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;

@Service
public interface JobGroupService {

    /**
     * Adds a new job group to a specific person.
     * 
     * @param jobGroup The data transfer object containing the job group details.
     * @param personId The ID of the person to whom the job group will be added.
     * @param uploaderUsername The username of the user who is uploading the job group.
     * @return A response containing information about the created job group.
     */
    JobGroupUploadResponse addJobGroupToPerson(JobGroupDto jobGroup, Integer personId, String uploaderUsername);

    /**
     * Retrieves all active job groups.
     * 
     * @return A data transfer object containing the list of active job groups.
     */
    ActiveJobsRequestDTO getActiveJobsGroups();

    /**
     * Sets completed jobs within a job group to picked up status, also validates the time consistency.
     * 
     * @param id The ID of the job group.
     * @param jobPickedUpData The data containing pick-up details.
     * @throws BindException If validation fails.
     */
    void setDoneJobsToPickedUp(Integer id, JobPickedUpDTO jobPickedUpData) throws BindException;

    /**
     * Generates a receipt for the specified job group.
     * 
     * @param id The ID of the job group for which to generate the receipt.
     * @return A byte array representing the PDF receipt document.
     */
    byte[] getReceipt(Integer id);
}
