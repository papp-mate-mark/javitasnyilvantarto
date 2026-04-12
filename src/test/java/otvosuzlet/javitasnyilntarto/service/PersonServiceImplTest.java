package otvosuzlet.javitasnyilntarto.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import otvosuzlet.javitasnyilntarto.dto.JobDto;
import otvosuzlet.javitasnyilntarto.dto.JobGroupUploadResponse;
import otvosuzlet.javitasnyilntarto.dto.PersonInfoDTO;
import otvosuzlet.javitasnyilntarto.dto.PersonRequest;
import otvosuzlet.javitasnyilntarto.dto.PersonSearchRequest;
import otvosuzlet.javitasnyilntarto.dto.TransferRequest;
import otvosuzlet.javitasnyilntarto.dto.TransferRequest.ImageTransferRequest;
import otvosuzlet.javitasnyilntarto.dto.TransferRequest.JobGroupTransferRequest;
import otvosuzlet.javitasnyilntarto.dto.TransferRequest.JobTransferRequest;
import otvosuzlet.javitasnyilntarto.enums.ImageType;
import otvosuzlet.javitasnyilntarto.exceptions.PersonNotFoundException;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobGroup;
import otvosuzlet.javitasnyilntarto.model.JobImage;
import otvosuzlet.javitasnyilntarto.model.Person;
import otvosuzlet.javitasnyilntarto.projections.PersonFullInfoProjection;
import otvosuzlet.javitasnyilntarto.repository.PersonRepository;

@ExtendWith(MockitoExtension.class)
public class PersonServiceImplTest {
    @Mock
    private PersonRepository personRepository;
    
    @InjectMocks
    private PersonServiceImpl personService;

    @Mock
    private JobImageService jobImageService;
    
    @Mock
    private ConversionService conversionService;

    @Test
    public void deletePersonTest(){
        Person testPerson = new Person();
        testPerson.setId(1);
        testPerson.setName("Test Person");
        
        JobImage beforeImage = new JobImage();
        beforeImage.setId(1);
        beforeImage.setType(ImageType.BEFORE);
        JobImage afterImage = new JobImage();
        afterImage.setId(2);
        afterImage.setType(ImageType.AFTER);

        Job job = new Job();
        job.setId(1);
        job.setAfterImages(Set.of(afterImage));
        job.setBeforeImage(Set.of(beforeImage));

        JobGroup jobGroup1 = new JobGroup();
        jobGroup1.setId(1);
        jobGroup1.setPerson(testPerson);
        jobGroup1.setJobs(Set.of(job));

        testPerson.setJobGroups(Set.of(jobGroup1));
        Mockito.when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));
     
        personService.deletePerson(1);

        Mockito.verify(personRepository, Mockito.times(1)).delete(testPerson);
    }

    @Test
    public void deletePersonNotFoundTest(){
        Mockito.when(personRepository.findById(1)).thenReturn(Optional.empty());
        Assertions.assertThrows(PersonNotFoundException.class, () -> personService.deletePerson(1));

        Mockito.verify(personRepository, Mockito.never()).delete(Mockito.any(Person.class));
    }

    @Test
    public void findByIdFullInfoProjectionTest(){
        Optional<PersonFullInfoProjection> expected = Optional.of(Mockito.mock(PersonFullInfoProjection.class));
        Mockito.when(personRepository.findById(1, PersonFullInfoProjection.class)).thenReturn(expected);
        Optional<PersonFullInfoProjection> result = personService.findByIdFullInfoProjection(1);
        Assertions.assertEquals(expected, result);
        Mockito.verify(personRepository, Mockito.times(1)).findById(Mockito.anyInt(), Mockito.eq(PersonFullInfoProjection.class));
    }

    @Test
    public void searchForPersonTest(){
        PersonSearchRequest searchRequest = Mockito.mock(PersonSearchRequest.class);
        Pageable pageable = PageRequest.of(0, 10);
        Person mockPerson = Mockito.mock(Person.class);
        PersonInfoDTO mockDto = Mockito.mock(PersonInfoDTO.class);
        Page<Person> repoData = new PageImpl<>(Set.of(mockPerson).stream().toList(), pageable, 1);
        Mockito.when(personRepository.findAll(Mockito.<Specification<Person>>any(), Mockito.eq(pageable))).thenReturn(repoData);
        Mockito.when(conversionService.convert(mockPerson, PersonInfoDTO.class)).thenReturn(mockDto);
        Page<PersonInfoDTO> expected = new PageImpl<>(Set.of(mockDto).stream().toList(), pageable, 1);

        Page<PersonInfoDTO> result = personService.searchForPerson(searchRequest, pageable);
        Assertions.assertEquals(expected, result);
    }

    @Test 
    public void findPersonByIdTest(){
        Person testPerson = new Person();
        testPerson.setId(1);
        testPerson.setName("Test Person");

        Mockito.when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));
        Person result = personService.findPersonById(1);

        Assertions.assertEquals(testPerson, result);
        Mockito.verify(personRepository, Mockito.times(1)).findById(1);
    }

    @Test
    public void findPersonByIdNotFoundTest(){
        Mockito.when(personRepository.findById(1)).thenReturn(Optional.empty());
        Assertions.assertThrows(PersonNotFoundException.class, () -> personService.findPersonById(1));
        Mockito.verify(personRepository, Mockito.times(1)).findById(1);
    }

    @Test
    public void createPersonWithJobGroupTest() {
        LocalDateTime bringin = LocalDateTime.of(2026, 2, 5, 10, 30);
        LocalDateTime deadline = LocalDateTime.of(2026, 2, 20, 16, 0);

        JobDto firstJobRequest = new JobDto();
        firstJobRequest.setObjectname("Ring");
        firstJobRequest.setDescription("Resize ring");
        firstJobRequest.setMaterial("Gold");
        firstJobRequest.setPricemin(100);
        firstJobRequest.setPricemax(180);
        firstJobRequest.setWeight(5.5);
        firstJobRequest.setFinishTime(LocalDateTime.of(2026, 2, 10, 11, 0));
        firstJobRequest.setPickedUpTime(LocalDateTime.of(2026, 2, 11, 9, 0));
        firstJobRequest.setFinalPrice(150);
        firstJobRequest.setUploadnote("urgent");
        firstJobRequest.setFinishnote("completed");
        firstJobRequest.setImagesBefore(List.of(101));
        firstJobRequest.setImagesAfter(List.of());

        JobDto secondJobRequest = new JobDto();
        secondJobRequest.setObjectname("Bracelet");
        secondJobRequest.setDescription("Polish bracelet");
        secondJobRequest.setMaterial("Silver");
        secondJobRequest.setPricemin(80);
        secondJobRequest.setPricemax(140);
        secondJobRequest.setWeight(8.0);
        secondJobRequest.setFinishTime(LocalDateTime.of(2026, 2, 12, 15, 0));
        secondJobRequest.setPickedUpTime(LocalDateTime.of(2026, 2, 13, 13, 0));
        secondJobRequest.setFinalPrice(120);
        secondJobRequest.setUploadnote("normal");
        secondJobRequest.setFinishnote("ready");
        secondJobRequest.setImagesBefore(List.of());
        secondJobRequest.setImagesAfter(List.of(201, 202));

        PersonRequest request = new PersonRequest();
        request.setName("John Doe");
        request.setAddress("Main street 1");
        request.setPhone("123456789");
        request.setBringin(bringin);
        request.setDeadline(deadline);
        request.setJobs(List.of(firstJobRequest, secondJobRequest));

        JobImage firstJobBeforeImage = new JobImage();
        firstJobBeforeImage.setId(101);
        firstJobBeforeImage.setType(ImageType.BEFORE);

        JobImage secondJobAfterImage1 = new JobImage();
        secondJobAfterImage1.setId(201);
        secondJobAfterImage1.setType(ImageType.AFTER);

        JobImage secondJobAfterImage2 = new JobImage();
        secondJobAfterImage2.setId(202);
        secondJobAfterImage2.setType(ImageType.AFTER);

        Mockito.when(jobImageService.attachImagesByIds(Mockito.eq(List.of(101)), Mockito.any(Job.class), Mockito.eq(ImageType.BEFORE)))
            .thenReturn(Set.of(firstJobBeforeImage));
        Mockito.when(jobImageService.attachImagesByIds(Mockito.eq(List.of()), Mockito.any(Job.class), Mockito.eq(ImageType.AFTER)))
            .thenReturn(Set.of());
        Mockito.when(jobImageService.attachImagesByIds(Mockito.eq(List.of()), Mockito.any(Job.class), Mockito.eq(ImageType.BEFORE)))
            .thenReturn(Set.of());
        Mockito.when(jobImageService.attachImagesByIds(Mockito.eq(List.of(201, 202)), Mockito.any(Job.class), Mockito.eq(ImageType.AFTER)))
            .thenReturn(Set.of(secondJobAfterImage1, secondJobAfterImage2));

        Person persistedPerson = new Person();
        persistedPerson.setId(10);
        persistedPerson.setName("John Doe");
        persistedPerson.setAddress("Main street 1");
        persistedPerson.setPhone("123456789");

        JobGroup persistedGroup = new JobGroup();
        persistedGroup.setId(20);
        persistedGroup.setBringedin(bringin);
        persistedGroup.setDeadline(deadline);
        persistedGroup.setPerson(persistedPerson);

        Job persistedFirstJob = new Job();
        persistedFirstJob.setId(30);
        persistedFirstJob.setObjectname("Ring");
        persistedFirstJob.setDescription("Resize ring");
        persistedFirstJob.setMaterial("Gold");
        persistedFirstJob.setPricemin(100);
        persistedFirstJob.setPricemax(180);
        persistedFirstJob.setWeight(5.5);
        persistedFirstJob.setDone(LocalDateTime.of(2026, 2, 10, 11, 0));
        persistedFirstJob.setPickup(LocalDateTime.of(2026, 2, 11, 9, 0));
        persistedFirstJob.setFinalprice(150);
        persistedFirstJob.setUploadnote("urgent");
        persistedFirstJob.setFinishnote("completed");
        persistedFirstJob.setBeforeImage(Set.of(firstJobBeforeImage));
        persistedFirstJob.setAfterImages(Set.of());
        persistedFirstJob.setJobGroup(persistedGroup);

        Job persistedSecondJob = new Job();
        persistedSecondJob.setId(31);
        persistedSecondJob.setObjectname("Bracelet");
        persistedSecondJob.setDescription("Polish bracelet");
        persistedSecondJob.setMaterial("Silver");
        persistedSecondJob.setPricemin(80);
        persistedSecondJob.setPricemax(140);
        persistedSecondJob.setWeight(8.0);
        persistedSecondJob.setDone(LocalDateTime.of(2026, 2, 12, 15, 0));
        persistedSecondJob.setPickup(LocalDateTime.of(2026, 2, 13, 13, 0));
        persistedSecondJob.setFinalprice(120);
        persistedSecondJob.setUploadnote("normal");
        persistedSecondJob.setFinishnote("ready");
        persistedSecondJob.setBeforeImage(Set.of());
        persistedSecondJob.setAfterImages(Set.of(secondJobAfterImage1, secondJobAfterImage2));
        persistedSecondJob.setJobGroup(persistedGroup);

        persistedGroup.setJobs(Set.of(persistedFirstJob, persistedSecondJob));
        persistedPerson.setJobGroups(Set.of(persistedGroup));

        Mockito.when(personRepository.save(Mockito.any(Person.class))).thenAnswer(invocation -> {
            Person personToSave = invocation.getArgument(0);
            personToSave.setId(10);
            JobGroup savedGroup = personToSave.getJobGroups().iterator().next();
            savedGroup.setId(20);
            return persistedPerson;
        });

        JobGroupUploadResponse response = personService.createPersonWithJobGroup(request);

        ArgumentCaptor<Person> savedPersonCaptor = ArgumentCaptor.forClass(Person.class);
        Mockito.verify(personRepository, Mockito.times(1)).save(savedPersonCaptor.capture());

        Person captured = savedPersonCaptor.getValue();
        Assertions.assertEquals("John Doe", captured.getName());
        Assertions.assertEquals("Main street 1", captured.getAddress());
        Assertions.assertEquals("123456789", captured.getPhone());
        Assertions.assertEquals(1, captured.getJobGroups().size());

        JobGroup capturedGroup = captured.getJobGroups().iterator().next();
        Assertions.assertEquals(bringin, capturedGroup.getBringedin());
        Assertions.assertEquals(deadline, capturedGroup.getDeadline());
        Assertions.assertEquals(2, capturedGroup.getJobs().size());

        Job capturedRingJob = null;
        Job capturedBraceletJob = null;
        for (Job job : capturedGroup.getJobs()) {
            if ("Ring".equals(job.getObjectname())) {
                capturedRingJob = job;
            }
            if ("Bracelet".equals(job.getObjectname())) {
                capturedBraceletJob = job;
            }
        }

        Assertions.assertNotNull(capturedRingJob);
        Assertions.assertEquals("Resize ring", capturedRingJob.getDescription());
        Assertions.assertEquals("Gold", capturedRingJob.getMaterial());
        Assertions.assertEquals(100, capturedRingJob.getPricemin());
        Assertions.assertEquals(180, capturedRingJob.getPricemax());
        Assertions.assertEquals(5.5, capturedRingJob.getWeight());
        Assertions.assertEquals(LocalDateTime.of(2026, 2, 10, 11, 0), capturedRingJob.getDone());
        Assertions.assertEquals(LocalDateTime.of(2026, 2, 11, 9, 0), capturedRingJob.getPickup());
        Assertions.assertEquals(150, capturedRingJob.getFinalprice());
        Assertions.assertEquals("urgent", capturedRingJob.getUploadnote());
        Assertions.assertEquals("completed", capturedRingJob.getFinishnote());
        Assertions.assertEquals(capturedGroup, capturedRingJob.getJobGroup());
        Assertions.assertEquals(Set.of(firstJobBeforeImage), capturedRingJob.getBeforeImage());
        Assertions.assertEquals(Set.of(), capturedRingJob.getAfterImages());

        Assertions.assertNotNull(capturedBraceletJob);
        Assertions.assertEquals("Polish bracelet", capturedBraceletJob.getDescription());
        Assertions.assertEquals("Silver", capturedBraceletJob.getMaterial());
        Assertions.assertEquals(80, capturedBraceletJob.getPricemin());
        Assertions.assertEquals(140, capturedBraceletJob.getPricemax());
        Assertions.assertEquals(8.0, capturedBraceletJob.getWeight());
        Assertions.assertEquals(LocalDateTime.of(2026, 2, 12, 15, 0), capturedBraceletJob.getDone());
        Assertions.assertEquals(LocalDateTime.of(2026, 2, 13, 13, 0), capturedBraceletJob.getPickup());
        Assertions.assertEquals(120, capturedBraceletJob.getFinalprice());
        Assertions.assertEquals("normal", capturedBraceletJob.getUploadnote());
        Assertions.assertEquals("ready", capturedBraceletJob.getFinishnote());
        Assertions.assertEquals(capturedGroup, capturedBraceletJob.getJobGroup());
        Assertions.assertEquals(Set.of(), capturedBraceletJob.getBeforeImage());
        Assertions.assertEquals(Set.of(secondJobAfterImage1, secondJobAfterImage2), capturedBraceletJob.getAfterImages());

        Mockito.verify(jobImageService, Mockito.times(1))
            .attachImagesByIds(Mockito.eq(List.of(101)), Mockito.any(Job.class), Mockito.eq(ImageType.BEFORE));
        Mockito.verify(jobImageService, Mockito.times(1))
            .attachImagesByIds(Mockito.eq(List.of()), Mockito.any(Job.class), Mockito.eq(ImageType.AFTER));
        Mockito.verify(jobImageService, Mockito.times(1))
            .attachImagesByIds(Mockito.eq(List.of()), Mockito.any(Job.class), Mockito.eq(ImageType.BEFORE));
        Mockito.verify(jobImageService, Mockito.times(1))
            .attachImagesByIds(Mockito.eq(List.of(201, 202)), Mockito.any(Job.class), Mockito.eq(ImageType.AFTER));
        Mockito.verify(jobImageService, Mockito.times(4))
            .attachImagesByIds(Mockito.anyCollection(), Mockito.any(Job.class), Mockito.any(ImageType.class));

        Assertions.assertEquals(20, response.getGroupId());
    }


    @Test
    public void transferTest() throws IOException {
        // Build image transfer data (Base64 encoded "test")
        String base64Data = java.util.Base64.getEncoder().encodeToString("test".getBytes());

        ImageTransferRequest beforeImg = new ImageTransferRequest();
        beforeImg.setImage(base64Data);
        beforeImg.setFullContentType("image/png");

        ImageTransferRequest afterImg = new ImageTransferRequest();
        afterImg.setImage(base64Data);
        afterImg.setFullContentType("image/jpeg");

        JobTransferRequest jobTransfer = new JobTransferRequest();
        jobTransfer.setObjectname("Necklace");
        jobTransfer.setDescription("Fix necklace");
        jobTransfer.setMaterial("Silver");
        jobTransfer.setWeight(10.0);
        jobTransfer.setPricemin(50);
        jobTransfer.setPricemax(150);
        jobTransfer.setFinalprice(120);
        jobTransfer.setDone(LocalDateTime.of(2026, 2, 10, 14, 0));
        jobTransfer.setPickup(LocalDateTime.of(2026, 2, 11, 10, 0));
        jobTransfer.setUploadnote("upload");
        jobTransfer.setFinishnote("finish");
        jobTransfer.setBeforeImage(List.of(beforeImg));
        jobTransfer.setAfterImages(List.of(afterImg));

        JobGroupTransferRequest groupTransfer = new JobGroupTransferRequest();
        groupTransfer.setBringedin(LocalDateTime.of(2026, 2, 1, 9, 0));
        groupTransfer.setDeadline(LocalDateTime.of(2026, 3, 1, 9, 0));
        groupTransfer.setJobs(List.of(jobTransfer));

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setName("Transfer Person");
        transferRequest.setAddress("Transfer Address");
        transferRequest.setPhone("999888");
        transferRequest.setJobGroups(List.of(groupTransfer));

        JobImage uploadedBefore = new JobImage();
        uploadedBefore.setId(100);
        JobImage uploadedAfter = new JobImage();
        uploadedAfter.setId(200);

        Mockito.when(jobImageService.uploadImage(Mockito.any(MultipartFile.class)))
                .thenReturn(uploadedBefore)
                .thenReturn(uploadedAfter);

        Set<JobImage> attachedBefore = Set.of(uploadedBefore);
        Set<JobImage> attachedAfter = Set.of(uploadedAfter);
        Mockito.when(jobImageService.attachImagesByIds(Mockito.eq(List.of(100)), Mockito.any(Job.class), Mockito.eq(ImageType.BEFORE)))
                .thenReturn(attachedBefore);
        Mockito.when(jobImageService.attachImagesByIds(Mockito.eq(List.of(200)), Mockito.any(Job.class), Mockito.eq(ImageType.AFTER)))
                .thenReturn(attachedAfter);

        personService.transfer(transferRequest);

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        Mockito.verify(personRepository, Mockito.times(1)).save(personCaptor.capture());

        Person savedPerson = personCaptor.getValue();
        Assertions.assertEquals("Transfer Person", savedPerson.getName());
        Assertions.assertEquals("Transfer Address", savedPerson.getAddress());
        Assertions.assertEquals("999888", savedPerson.getPhone());
        Assertions.assertEquals(1, savedPerson.getJobGroups().size());

        JobGroup savedGroup = savedPerson.getJobGroups().iterator().next();
        Assertions.assertEquals(LocalDateTime.of(2026, 2, 1, 9, 0), savedGroup.getBringedin());
        Assertions.assertEquals(LocalDateTime.of(2026, 3, 1, 9, 0), savedGroup.getDeadline());
        Assertions.assertEquals(1, savedGroup.getJobs().size());

        Job savedJob = savedGroup.getJobs().iterator().next();
        Assertions.assertEquals("Necklace", savedJob.getObjectname());
        Assertions.assertEquals("Fix necklace", savedJob.getDescription());
        Assertions.assertEquals("Silver", savedJob.getMaterial());
        Assertions.assertEquals(10.0, savedJob.getWeight());
        Assertions.assertEquals(50, savedJob.getPricemin());
        Assertions.assertEquals(150, savedJob.getPricemax());
        Assertions.assertEquals(120, savedJob.getFinalprice());
        Assertions.assertEquals(attachedBefore, savedJob.getBeforeImage());
        Assertions.assertEquals(attachedAfter, savedJob.getAfterImages());

        Mockito.verify(jobImageService, Mockito.times(2)).uploadImage(Mockito.any(MultipartFile.class));
    }


    @Test
    public void deleteAllTest() {
        personService.deleteAll();

        Mockito.verify(personRepository, Mockito.times(1)).deleteAll();
    }

    
}
