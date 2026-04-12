package otvosuzlet.javitasnyilntarto.controllers;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import otvosuzlet.javitasnyilntarto.dto.ActiveJobsRequestDTO;
import otvosuzlet.javitasnyilntarto.dto.JobGroupDto;
import otvosuzlet.javitasnyilntarto.dto.JobGroupUploadResponse;
import otvosuzlet.javitasnyilntarto.dto.PageResponse;
import otvosuzlet.javitasnyilntarto.dto.PersonInfoDTO;
import otvosuzlet.javitasnyilntarto.dto.PersonRequest;
import otvosuzlet.javitasnyilntarto.dto.PersonSearchRequest;
import otvosuzlet.javitasnyilntarto.projections.PersonFullInfoProjection;
import java.util.Optional;
import otvosuzlet.javitasnyilntarto.service.JobGroupService;
import otvosuzlet.javitasnyilntarto.service.PersonService;

@RestController
@RequestMapping("/api/person")
public class PersonController {
    @Autowired
    private PersonService personService;
    @Autowired
    private JobGroupService jobGroupService;
    @Autowired
    private ConversionService conversionService;
    
    /**
     * Uploads a new person along with their first job group.
     *
     * @param request Instantiatible configuration parameter bindings containing initial constraints perfectly formatting logic.
     * @return Execution result encapsulating ID based context returning 200 HTTP logic responses.
     */
    @PostMapping
    public ResponseEntity<JobGroupUploadResponse> createPerson(@Valid @RequestBody PersonRequest request) {
        return ResponseEntity.ok(personService.createPersonWithJobGroup(request));
    }
    
    /**
     * Uploads a new job group for an existing person.
     * 
     * @param personId Tracing relation ID matching targeted destination completely.
     * @param jobGroup Validation bound encapsulation payload executing mappings efficiently.
     * @param principal Principal bound context handling tracing and logging definitions naturally perfectly translating interactions accurately.
     * @return 200 HTTP code carrying newly initialized bounds directly perfectly aligned responses dynamically.
     */
    @PostMapping("/{personId}/job-groups")
    public ResponseEntity<JobGroupUploadResponse> addJobGroupToPerson(
        @PathVariable Integer personId,
        @Valid @RequestBody JobGroupDto jobGroup,
        Principal principal
    ) {
        JobGroupUploadResponse response = jobGroupService.addJobGroupToPerson(jobGroup, personId, principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrives a person's information their children data based on their ID.
     *
     * @param id Valid referencing structure parameter targeting relation entities distinct resolving accurately.
     * @return Execution returning potentially null encapsulations resolving HTTP boundary results.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Optional<PersonFullInfoProjection>> findPersonFromId(@PathVariable Integer id) {
        return ResponseEntity.ok(personService.findByIdFullInfoProjection(id));
    }
    /**
     * Retrives the curretnly in progress or done jobs.
     *
     * @return Interpreted representation capturing pending interactions accurately.
     */
    @GetMapping("/getActiveJobs")
    public ResponseEntity<ActiveJobsRequestDTO> getActiveJobs(){
        return ResponseEntity.ok(jobGroupService.getActiveJobsGroups());
    }
    
    /**
     * Searches among the persons.
     *
     * @param search Complex filtering model interpreting query requirements fully directly.
     * @param pageable Default layout parameter directing resolving depth successfully mapping pagination attributes logically consistently formatting structures perfectly accurately.
     * @return Formatted layout boundary mapped response correctly encapsulating entities functionally.
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponse<PersonInfoDTO>> searchPersons(
            @ModelAttribute PersonSearchRequest search,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<PersonInfoDTO> page = personService.searchForPerson(search, pageable);
        @SuppressWarnings("unchecked")
        PageResponse<PersonInfoDTO> response = (PageResponse<PersonInfoDTO>) conversionService.convert(page, PageResponse.class);
        return ResponseEntity.ok(response);
    }


    /**
     * Deletes a person by their ID.
     * Deletion cascades to children.
     *
     * @param id The reference entity primary key.
     * @return No content acknowledgement designating deletion logic outcome.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Integer id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }

}
