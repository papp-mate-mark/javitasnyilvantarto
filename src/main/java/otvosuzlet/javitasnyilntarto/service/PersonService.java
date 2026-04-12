package otvosuzlet.javitasnyilntarto.service;

import otvosuzlet.javitasnyilntarto.dto.JobGroupUploadResponse;
import otvosuzlet.javitasnyilntarto.dto.PersonInfoDTO;
import otvosuzlet.javitasnyilntarto.dto.PersonRequest;
import otvosuzlet.javitasnyilntarto.dto.PersonSearchRequest;
import otvosuzlet.javitasnyilntarto.dto.TransferRequest;
import otvosuzlet.javitasnyilntarto.model.Person;
import otvosuzlet.javitasnyilntarto.projections.PersonFullInfoProjection;
import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PersonService {
    /**
     * Deletes all persons from the system.
     * 
     * This method removes all person records from the database, which will also cascade delete related entities.
     * It also calls the job image service to ensure that all the image files are deleted
     */
    void deleteAll();
    
    /**
     * Saves a person and it's jobgroups with jobs and images based on the provided object.
     * Used for migration, there is no validation.
     * 
     * @param person the {@link TransferRequest} containing the transfer details
     */
    void transfer(TransferRequest person);

    /**
     * Deletes a person from the system by their unique identifier.
     *
     * @param id the unique identifier of the person to be deleted
     * @throws IllegalArgumentException if the id is null or invalid
     * @throws EntityNotFoundException if no person with the given id exists
     */
    void deletePerson(Integer id);

    /**
     * Finds a person by their unique identifier.
     *
     * @param id the unique identifier of the person to find
     * @return the Person object matching the given id, or null if not found
     * @throws IllegalArgumentException if id is null or invalid
     */
    Person findPersonById(Integer id);

    /**
     * Finds a person by their ID and returns their information.
     *
     * @param id the unique identifier of the person to retrieve
     * @return an {@link Optional} containing the {@link PersonFullInfoProjection} with full person details
     *         if a person with the given ID exists, or an empty {@link Optional} if not found
     */
    Optional<PersonFullInfoProjection> findByIdFullInfoProjection(Integer id);
    
    /**
     * Searches for persons based on the provided search criteria and returns a paginated list of person information.
     *
     * @param search the search criteria to apply
     * @param pageable the pagination information
     * @return a page of {@link PersonInfoDTO} objects matching the search criteria
     */
    Page<PersonInfoDTO> searchForPerson(PersonSearchRequest search, Pageable pageable);

    /**
     * Creates a new person with a job group based on the provided request.
     *
     * @param request the {@link PersonRequest} containing the person and job group details
     * @return a {@link JobGroupUploadResponse} containing the result of the person creation operation
     */
    JobGroupUploadResponse createPersonWithJobGroup(PersonRequest request);

    /**
     * Downloads all person data as TransferRequest objects with Base64-encoded images.
     *
     * @return A list of TransferRequest objects ready for transfer/migration
     */
    List<TransferRequest> downloadMigrationData();
}
