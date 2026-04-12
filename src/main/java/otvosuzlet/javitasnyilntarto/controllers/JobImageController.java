package otvosuzlet.javitasnyilntarto.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import otvosuzlet.javitasnyilntarto.service.JobImageService;

@RestController
@RequestMapping("/api/jobimage")
public class JobImageController {
    @Autowired
    private JobImageService jobImageService;


    /**
     * Returns the full resolution image file.
     *
     * @param id Entity defining locating logic boundaries.
     * @return Raw downloadable bytes matching specific file.
     */
    @GetMapping("/{id}")
    public Object getFullImageById(@PathVariable Integer id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=image.jpg");
        headers.add("Content-Type", "image/jpg");
        return new ResponseEntity<>(jobImageService.getFullImageById(id), headers, HttpStatus.OK);
    }
    /**
     * Returns the low resolution thumbnail image file.
     *
     * @param id Database mapped target relation key.
     * @return Interpretable graphical outputs aligned towards image consumption streams.
     */
    @GetMapping("/{id}/thumbnail")
    public Object getThumbnailById(@PathVariable Integer id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=image.jpg");
        headers.add("Content-Type", "image/jpg");
        return new ResponseEntity<>(jobImageService.getThumbnailById(id), headers, HttpStatus.OK);
    }

    /**
     * Uploads a new image, and returns it's ID.
     *
     * @param file Streamed raw component interpreting graphic definitions.
     * @return 201 Created acknowledgment encapsulating newly defined persistence mappings logic.
     * @throws Exception Propagated IO conflicts.
     */
    @PostMapping
    public ResponseEntity<Integer> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        Integer id = jobImageService.uploadImage(file).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    /**
     * Deletes the image by ID.
     *
     * @param id Tracking key linked with intended elimination candidate file.
     * @return Terminating acknowledgment interpreting standard execution closure results matching REST patterns.
     * @throws Exception Possible generic unlinked database handling errors processing queries.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Integer id) throws Exception {
        jobImageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}
