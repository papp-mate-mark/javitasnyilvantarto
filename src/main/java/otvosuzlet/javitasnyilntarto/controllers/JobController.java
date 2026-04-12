package otvosuzlet.javitasnyilntarto.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import jakarta.servlet.http.HttpServletRequest; 
import otvosuzlet.javitasnyilntarto.dto.JobCompleteDTO;
import otvosuzlet.javitasnyilntarto.dto.JobPickedUpDTO;
import otvosuzlet.javitasnyilntarto.dto.JobSearchDto;
import otvosuzlet.javitasnyilntarto.dto.JobSearchJobDataDTO;
import otvosuzlet.javitasnyilntarto.dto.PageResponse;
import otvosuzlet.javitasnyilntarto.exceptions.ValidationException;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.service.JobService;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    @Autowired
    private JobService jobService;
    @Autowired
    private ConversionService conversionService;

    
    /**
     * Searches for jobs based on search parameters with pagination capabilities.
     *
     * @param searchParams The model representation of job search criteria.
     * @param pageable Structured pagination configuration.
     * @return A paginated set of job search evaluation results.
     */
    @GetMapping("/fullsearch")
    @ResponseBody
    public ResponseEntity<PageResponse<JobSearchJobDataDTO>> searchJobs(
            @ModelAttribute JobSearchDto searchParams,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<JobSearchJobDataDTO> page = jobService.searchForJob(searchParams, pageable);
        @SuppressWarnings("unchecked")
        PageResponse<JobSearchJobDataDTO> response = (PageResponse<JobSearchJobDataDTO>) conversionService.convert(page, PageResponse.class);
        return ResponseEntity.ok(response);
    }

    /**
     * Marks a job as done using completion details.
     *
     * @param id The ID of the job to update.
     * @param input Data representing completion information.
     * @param bindingResult Data binder capturing validation state.
     * @param request Exposes related HTTP request attributes.
     * @return No content status indicating success, or binding error exceptions.
     * @throws BindException Expected when validation logic encounters rule deviations.
     */
    @PreAuthorize("hasAuthority('MODIFY_JOBS')")
    @PatchMapping("/{id}/change-to-done")
    public ResponseEntity<Void> changeToDone(@PathVariable Integer id, @RequestBody JobCompleteDTO input, BindingResult bindingResult, HttpServletRequest request) throws BindException
    {   
        try{
            jobService.validateAndCompleteJob(id, input);
        } catch(ValidationException e){
            for (ValidationException.ValidationError error : e.getErrors()) {
                bindingResult.rejectValue(error.getField(), "", error.getDefaultMessage());
            }
        }
        if(bindingResult.hasErrors()){
            throw new BindException(bindingResult);
        }
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Marks a job as picked up.
     *
     * @param id The ID of the targeted job.
     * @param input Data representing pick up constraints.
     * @param bindingResult Data binder capturing validation state.
     * @return No content status indicating success, or throws exceptions appropriately.
     * @throws BindException Generated when invalid bindings have transpired.
     */
    @PreAuthorize("hasAuthority('MODIFY_JOBS')")
    @PatchMapping("/{id}/change-to-pickedup")
    public ResponseEntity<Void> changeToPickedUp(@PathVariable Integer id, @RequestBody JobPickedUpDTO input, BindingResult bindingResult) throws BindException
    {
        try{
            jobService.pickedUpJob(id, input);
        } catch(ValidationException e){
            for (ValidationException.ValidationError error : e.getErrors()) {
                bindingResult.rejectValue(error.getField(), "", error.getDefaultMessage());
            }
        }

        if(bindingResult.hasErrors()){
            throw new BindException(bindingResult);
        }
        return ResponseEntity.noContent().build();
    }


    /**
     * Downloads a job summary document as a PDF.
     *
     * @param id The ID referencing the job.
     * @return Formatted response containing the raw byte streams of the PDF.
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Integer id)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=sample.pdf");
        headers.add("Content-Type", "application/pdf");

        return new ResponseEntity<>(jobService.getSummary(id), headers, HttpStatus.OK);
    }
    /**
     * Resolves job data based on a given job ID.
     *
     * @param id Job tracking identifier.
     * @return Job model structure embedded inside the HTTP response entity.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Integer id)
    {
        return ResponseEntity.ok(jobService.getJobById(id));
    }
    /**
     * Eliminates an existing job record and its cascading properties globally.
     *
     * @param id The reference entity primary key.
     * @return No content acknowledgement designating deletion logic outcome.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobById(@PathVariable Integer id)
    {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
