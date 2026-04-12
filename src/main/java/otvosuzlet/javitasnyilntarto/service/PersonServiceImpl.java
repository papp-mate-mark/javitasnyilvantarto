package otvosuzlet.javitasnyilntarto.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import otvosuzlet.javitasnyilntarto.dto.JobDto;
import otvosuzlet.javitasnyilntarto.dto.JobGroupUploadResponse;
import otvosuzlet.javitasnyilntarto.dto.PersonInfoDTO;
import otvosuzlet.javitasnyilntarto.dto.PersonRequest;
import otvosuzlet.javitasnyilntarto.dto.PersonSearchRequest;
import otvosuzlet.javitasnyilntarto.dto.TransferRequest;
import otvosuzlet.javitasnyilntarto.dto.TransferRequest.JobGroupTransferRequest;
import otvosuzlet.javitasnyilntarto.dto.TransferRequest.JobTransferRequest;
import otvosuzlet.javitasnyilntarto.dto.TransferRequest.ImageTransferRequest;

import otvosuzlet.javitasnyilntarto.enums.ImageType;
import otvosuzlet.javitasnyilntarto.exceptions.PersonNotFoundException;
import otvosuzlet.javitasnyilntarto.model.Base64MultipartFile;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobGroup;
import otvosuzlet.javitasnyilntarto.model.JobImage;
import otvosuzlet.javitasnyilntarto.model.Person;
import otvosuzlet.javitasnyilntarto.projections.PersonFullInfoProjection;
import otvosuzlet.javitasnyilntarto.repository.PersonRepository;
import otvosuzlet.javitasnyilntarto.specification.PersonSearchSpec;
import java.io.IOException;
import java.util.*;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PersonServiceImpl implements PersonService {
    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private JobImageService jobImageService;

    @Autowired
    private ConversionService conversionService;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deletePerson(Integer id) {
        Person person = findPersonById(id);

        personRepository.delete(person);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Person findPersonById(Integer id) {
        return personRepository.findById(id).orElseThrow(() -> new PersonNotFoundException("Person with ID:" + id + " not found", "error.person.not.found"));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Optional<PersonFullInfoProjection> findByIdFullInfoProjection(Integer id) {
        return personRepository.findById(id, PersonFullInfoProjection.class);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Page<PersonInfoDTO> searchForPerson(PersonSearchRequest search, Pageable pageable) {
        return personRepository.findAll(PersonSearchSpec.withFilters(search), pageable)
                .map(person -> conversionService.convert(person, PersonInfoDTO.class));
    }


    /** {@inheritDoc} */
    @Override
    @Transactional
    public JobGroupUploadResponse createPersonWithJobGroup(PersonRequest request) {
        // Create and save person
        Person person = new Person();
        person.setName(request.getName());
        person.setAddress(request.getAddress());
        person.setPhone(request.getPhone());
        JobGroup jobGroup = new JobGroup();
        jobGroup.setPerson(person);
        jobGroup.setBringedin(request.getBringin() == null ? LocalDateTime.now() : request.getBringin());
        jobGroup.setDeadline(request.getDeadline());

        Set<Job> jobs = new HashSet<>();
        if (request.getJobs() != null) {
            for (JobDto jobDto : request.getJobs()) {
                Job job = new Job();
                job.setObjectname(jobDto.getObjectname());
                job.setDescription(jobDto.getDescription());
                job.setMaterial(jobDto.getMaterial());
                job.setPricemin(jobDto.getPricemin());
                job.setPricemax(jobDto.getPricemax());
                job.setWeight(jobDto.getWeight());
                job.setJobGroup(jobGroup);
                job.setDone(jobDto.getFinishTime());
                job.setPickup(jobDto.getPickedUpTime());
                job.setFinalprice(jobDto.getFinalPrice());
                job.setUploadnote(jobDto.getUploadnote());
                job.setFinishnote(jobDto.getFinishnote());

                job.setBeforeImage(jobImageService.attachImagesByIds(jobDto.getImagesBefore(), job, ImageType.BEFORE));
                job.setAfterImages(jobImageService.attachImagesByIds(jobDto.getImagesAfter(), job, ImageType.AFTER));

                jobs.add(job);
            }
        }
        jobGroup.setJobs(jobs);
        person.setJobGroups(Collections.singleton(jobGroup));
        personRepository.save(person);
        return new JobGroupUploadResponse(jobGroup.getId());
    }



    /** {@inheritDoc} */
    @Override
    @Transactional
    public void transfer(TransferRequest person) {
        Person newPerson = new Person();
        newPerson.setName(person.getName());
        newPerson.setAddress(person.getAddress());
        newPerson.setPhone(person.getPhone());
        
        Set<JobGroup> jobGroups = new HashSet<>();
        for (JobGroupTransferRequest jobGroup : person.getJobGroups()){
            JobGroup newJobGroup = new JobGroup();
            newJobGroup.setBringedin(jobGroup.getBringedin());
            newJobGroup.setDeadline(jobGroup.getDeadline());
            newJobGroup.setPerson(newPerson);
            Set<Job> jobs = new HashSet<>();
            for(JobTransferRequest job: jobGroup.getJobs()){

                Job newJob = new Job();
                newJob.setObjectname(job.getObjectname());
                newJob.setDescription(job.getDescription());
                newJob.setMaterial(job.getMaterial());
                newJob.setWeight(job.getWeight());
                newJob.setPricemin(job.getPricemin());
                newJob.setPricemax(job.getPricemax());
                newJob.setFinalprice(job.getFinalprice());
                newJob.setDone(job.getDone());
                newJob.setPickup(job.getPickup());
                newJob.setUploadnote(job.getUploadnote());
                newJob.setFinishnote(job.getFinishnote());
                
                Collection<Integer> beforeImages = job.getBeforeImage().stream().map(image -> {
                    try {
                        return jobImageService.uploadImage(new Base64MultipartFile(image.getImage(), "123", image.getFullContentType())).getId();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to upload before image", e);
                    }
                }).toList();
                newJob.setBeforeImage(jobImageService.attachImagesByIds(beforeImages, newJob, ImageType.BEFORE));
                Collection<Integer> afterImages = job.getAfterImages().stream().map(image -> {
                    try {
                        return jobImageService.uploadImage(new Base64MultipartFile(image.getImage(), "123", image.getFullContentType())).getId();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to upload before image", e);
                    }
                }).toList();
                newJob.setAfterImages(jobImageService.attachImagesByIds(afterImages, newJob, ImageType.AFTER));
                newJob.setJobGroup(newJobGroup);
                jobs.add(newJob);
            }
            newJobGroup.setJobs(jobs);
            newJobGroup.setPerson(newPerson);
            jobGroups.add(newJobGroup);
        }
        newPerson.setJobGroups(jobGroups);
        personRepository.save(newPerson);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteAll() {
        personRepository.deleteAll();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<TransferRequest> downloadMigrationData() {
        return personRepository.findAll().stream()
                .map(this::convertPersonToTransferRequest)
                .toList();
    }

    private TransferRequest convertPersonToTransferRequest(Person person) {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setName(person.getName());
        transferRequest.setAddress(person.getAddress());
        transferRequest.setPhone(person.getPhone());

        List<JobGroupTransferRequest> jobGroups = person.getJobGroups().stream()
                .map(this::convertJobGroupToTransferRequest)
                .toList();
        transferRequest.setJobGroups(jobGroups);
        return transferRequest;
    }

    private JobGroupTransferRequest convertJobGroupToTransferRequest(JobGroup jobGroup) {
        JobGroupTransferRequest transferRequest = new JobGroupTransferRequest();
        transferRequest.setBringedin(jobGroup.getBringedin());
        transferRequest.setDeadline(jobGroup.getDeadline());

        List<JobTransferRequest> jobs = jobGroup.getJobs().stream()
                .map(this::convertJobToTransferRequest)
                .toList();
        transferRequest.setJobs(jobs);
        return transferRequest;
    }

    private JobTransferRequest convertJobToTransferRequest(Job job) {
        JobTransferRequest transferRequest = new JobTransferRequest();
        transferRequest.setDescription(job.getDescription());
        transferRequest.setObjectname(job.getObjectname());
        transferRequest.setMaterial(job.getMaterial());
        transferRequest.setWeight(job.getWeight());
        transferRequest.setPricemin(job.getPricemin());
        transferRequest.setPricemax(job.getPricemax());
        transferRequest.setFinalprice(job.getFinalprice());
        transferRequest.setDone(job.getDone());
        transferRequest.setPickup(job.getPickup());
        transferRequest.setUploadnote(job.getUploadnote());
        transferRequest.setFinishnote(job.getFinishnote());

        List<ImageTransferRequest> beforeImages = job.getBeforeImage().stream()
                .map(this::convertJobImageToTransferRequest)
                .toList();
        transferRequest.setBeforeImage(beforeImages);

        List<ImageTransferRequest> afterImages = job.getAfterImages().stream()
                .map(this::convertJobImageToTransferRequest)
                .toList();
        transferRequest.setAfterImages(afterImages);

        return transferRequest;
    }

    private ImageTransferRequest convertJobImageToTransferRequest(JobImage image) {
        ImageTransferRequest transferRequest = new ImageTransferRequest();
        try {
            byte[] imageBytes = jobImageService.getFullImageById(image.getId());
            if (imageBytes != null) {
                transferRequest.setImage(Base64.getEncoder().encodeToString(imageBytes));
                transferRequest.setFullContentType("image/jpeg");
                if (image.getCreateTime() != null) {
                    transferRequest.setCreateTime(image.getCreateTime().toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load image with ID: " + image.getId(), e);
        }
        return transferRequest;
    }

}
