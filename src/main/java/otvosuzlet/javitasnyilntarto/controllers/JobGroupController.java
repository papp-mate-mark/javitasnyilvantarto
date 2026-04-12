package otvosuzlet.javitasnyilntarto.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import otvosuzlet.javitasnyilntarto.dto.JobPickedUpDTO;
import otvosuzlet.javitasnyilntarto.exceptions.ValidationException;
import otvosuzlet.javitasnyilntarto.service.JobGroupService;

@RestController
@RequestMapping("/api/jobgroup")
public class JobGroupController {
    @Autowired
    private JobGroupService jobGroupService;    
    
    /**
     * Changes the status of the jobs associated with the given job group ID to "picked up",
     * if the provided input data (including date consistency checks) is valid. 
     *
     * @param id Tracking key pointing towards a job group.
     * @param input Data resolving timing details associated with the jobs pickups.
     * @param bindingResult Component containing any identified discrepancies and invalid references evaluated by the interceptors.
     * @return 200 HTTP code acknowledging update successes gracefully.
     * @throws BindException Exception denoting parameter logic parsing inconsistencies.
     */
    @PatchMapping("/{id}/change-donejobs-to-pickedup")
    public ResponseEntity<Void> changeToPickedUp(@PathVariable Integer id, @RequestBody JobPickedUpDTO input, BindingResult bindingResult) throws BindException
    {
        try{
            jobGroupService.setDoneJobsToPickedUp(id, input);
        } catch (ValidationException e) {
            for (ValidationException.ValidationError error : e.getErrors()) {
                bindingResult.rejectValue(error.getField(), "", error.getDefaultMessage());
            }
            throw new BindException(bindingResult);
        }

        return ResponseEntity.ok().build();
    }
    
    /**
     * Downloads the summary PDF.
     *
     * @param id Origin document reference ID.
     * @return Transmitable and displayable raw bytes interpreted natively within PDF bounds.
     */
    @GetMapping("/{id}/get-receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Integer id)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=sample.pdf");
        headers.add("Content-Type", "application/pdf");
        return new ResponseEntity<>(jobGroupService.getReceipt(id), headers, HttpStatus.OK);
    }
    
}
