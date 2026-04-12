package otvosuzlet.javitasnyilntarto.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import otvosuzlet.javitasnyilntarto.dto.TransferRequest;
import otvosuzlet.javitasnyilntarto.service.PersonService;

@RestController
@RequestMapping("/api/migration")
@PreAuthorize("hasAuthority('MANAGE_MIGRATIONS')")
public class MigrationController {
    @Autowired
    private PersonService personService;

    /**
     * Executes bulk transitional injection.
     * Used for migrating data.
     *
     * @param request Data chunk lists resolving migration interpretations iteratively.
     * @return Execution end boundary defining standard REST void returns.
     */
    @PostMapping()
    public Object transfer(@RequestBody List<TransferRequest> request) {
        for (TransferRequest entity : request) {
            personService.transfer(entity);
        }
        return null;
    }
    /**
     * Deletes all data from the system, excluding user data.
     */
    @DeleteMapping()
    public void deleteAll(){
        personService.deleteAll();
    }

    /**
     * Downloads all person data as TransferRequest objects with Base64-encoded images.
     * This can be used to export the current system data for migration to another instance.
     *
     * @return A list of TransferRequest objects ready for transfer/migration
     */
    @GetMapping()
    public List<TransferRequest> download() {
        return personService.downloadMigrationData();
    }
}
