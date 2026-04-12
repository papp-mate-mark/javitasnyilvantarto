package otvosuzlet.javitasnyilntarto.service;

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
import otvosuzlet.javitasnyilntarto.dto.JobDto;
import otvosuzlet.javitasnyilntarto.dto.JobGroupDto;
import otvosuzlet.javitasnyilntarto.dto.JobGroupUploadResponse;
import otvosuzlet.javitasnyilntarto.dto.ActiveJobsRequestDTO;
import otvosuzlet.javitasnyilntarto.dto.JobPickedUpDTO;
import otvosuzlet.javitasnyilntarto.enums.ImageType;
import otvosuzlet.javitasnyilntarto.exceptions.ValidationException;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobGroup;
import otvosuzlet.javitasnyilntarto.model.JobImage;
import otvosuzlet.javitasnyilntarto.model.Person;
import otvosuzlet.javitasnyilntarto.model.User;
import otvosuzlet.javitasnyilntarto.projections.JobFullInfoProjection;
import otvosuzlet.javitasnyilntarto.projections.JobGroupFullInfoProjection;
import otvosuzlet.javitasnyilntarto.projections.PersonFullInfoProjection;
import otvosuzlet.javitasnyilntarto.repository.JobGroupRepository;
import otvosuzlet.javitasnyilntarto.repository.JobRepository;
import otvosuzlet.javitasnyilntarto.repository.PersonRepository;

@ExtendWith(MockitoExtension.class)
public class JobGroupServiceImplTest {
    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobGroupRepository jobGroupRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock 
    private UserService userService;

    @Mock
    private JobImageService imageService;

    @Mock
    private SystemSettingServiceImpl systemSettingService;

    @InjectMocks
    private JobGroupServiceImpl jobGroupService;

    @Test
    public void addJobGroupToPersonTest(){
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

        JobGroupDto request = new JobGroupDto();
        request.setBringin(bringin);
        request.setDeadline(deadline);
        request.setJobs(List.of(firstJobRequest, secondJobRequest));

        Person person = new Person();
        person.setId(10);
        person.setName("John Doe");
        person.setAddress("Main street 1");
        person.setPhone("123456789");

        User uploader = new User();
        uploader.setId(99);
        uploader.setUsername("uploader");

        JobImage firstJobBeforeImage = new JobImage();
        firstJobBeforeImage.setId(101);
        firstJobBeforeImage.setType(ImageType.BEFORE);

        JobImage secondJobAfterImage1 = new JobImage();
        secondJobAfterImage1.setId(201);
        secondJobAfterImage1.setType(ImageType.AFTER);

        JobImage secondJobAfterImage2 = new JobImage();
        secondJobAfterImage2.setId(202);
        secondJobAfterImage2.setType(ImageType.AFTER);

        Mockito.when(personRepository.findById(10)).thenReturn(Optional.of(person));
        Mockito.when(userService.findByUsername("uploader")).thenReturn(uploader);
        Mockito.when(imageService.attachImagesByIds(Mockito.eq(List.of(101)), Mockito.any(Job.class), Mockito.eq(ImageType.BEFORE)))
            .thenReturn(Set.of(firstJobBeforeImage));
        Mockito.when(imageService.attachImagesByIds(Mockito.eq(List.of()), Mockito.any(Job.class), Mockito.eq(ImageType.AFTER)))
            .thenReturn(Set.of());
        Mockito.when(imageService.attachImagesByIds(Mockito.eq(List.of()), Mockito.any(Job.class), Mockito.eq(ImageType.BEFORE)))
            .thenReturn(Set.of());
        Mockito.when(imageService.attachImagesByIds(Mockito.eq(List.of(201, 202)), Mockito.any(Job.class), Mockito.eq(ImageType.AFTER)))
            .thenReturn(Set.of(secondJobAfterImage1, secondJobAfterImage2));
        Mockito.when(jobGroupRepository.save(Mockito.any(JobGroup.class))).thenAnswer(invocation -> {
            JobGroup saved = invocation.getArgument(0);
            saved.setId(20);
            return saved;
        });

        JobGroupUploadResponse response = jobGroupService.addJobGroupToPerson(request, 10, "uploader");

        ArgumentCaptor<JobGroup> savedJobGroupCaptor = ArgumentCaptor.forClass(JobGroup.class);
        Mockito.verify(jobGroupRepository, Mockito.times(1)).save(savedJobGroupCaptor.capture());
        JobGroup captured = savedJobGroupCaptor.getValue();

        Assertions.assertEquals(person, captured.getPerson());
        Assertions.assertEquals(uploader, captured.getUser());
        Assertions.assertEquals(bringin, captured.getBringedin());
        Assertions.assertEquals(deadline, captured.getDeadline());
        Assertions.assertEquals(2, captured.getJobs().size());

        Job capturedRingJob = null;
        Job capturedBraceletJob = null;
        for (Job job : captured.getJobs()) {
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
        Assertions.assertEquals(captured, capturedRingJob.getJobGroup());
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
        Assertions.assertEquals(captured, capturedBraceletJob.getJobGroup());
        Assertions.assertEquals(Set.of(), capturedBraceletJob.getBeforeImage());
        Assertions.assertEquals(Set.of(secondJobAfterImage1, secondJobAfterImage2), capturedBraceletJob.getAfterImages());

        Mockito.verify(imageService, Mockito.times(1))
            .attachImagesByIds(Mockito.eq(List.of(101)), Mockito.any(Job.class), Mockito.eq(ImageType.BEFORE));
        Mockito.verify(imageService, Mockito.times(1))
            .attachImagesByIds(Mockito.eq(List.of()), Mockito.any(Job.class), Mockito.eq(ImageType.AFTER));
        Mockito.verify(imageService, Mockito.times(1))
            .attachImagesByIds(Mockito.eq(List.of()), Mockito.any(Job.class), Mockito.eq(ImageType.BEFORE));
        Mockito.verify(imageService, Mockito.times(1))
            .attachImagesByIds(Mockito.eq(List.of(201, 202)), Mockito.any(Job.class), Mockito.eq(ImageType.AFTER));
        Mockito.verify(imageService, Mockito.times(4))
            .attachImagesByIds(Mockito.anyCollection(), Mockito.any(Job.class), Mockito.any(ImageType.class));

        Assertions.assertEquals(20, response.getGroupId());
    }

    @Test
    public void getActiveJobsGroupsTest() {
        LocalDateTime bringin = LocalDateTime.of(2026, 2, 5, 10, 30);
        LocalDateTime deadline = LocalDateTime.of(2026, 2, 20, 16, 0);
        LocalDateTime doneTime = LocalDateTime.of(2026, 2, 10, 11, 0);

        JobFullInfoProjection inProgressJobProjection = Mockito.mock(JobFullInfoProjection.class);
        Mockito.when(inProgressJobProjection.getId()).thenReturn(1001);
        Mockito.when(inProgressJobProjection.getDescription()).thenReturn("Resize ring");
        Mockito.when(inProgressJobProjection.getObjectname()).thenReturn("Ring");
        Mockito.when(inProgressJobProjection.getPricemin()).thenReturn(100);
        Mockito.when(inProgressJobProjection.getPricemax()).thenReturn(180);
        Mockito.when(inProgressJobProjection.getDone()).thenReturn(null);

        JobFullInfoProjection doneJobProjection = Mockito.mock(JobFullInfoProjection.class);
        Mockito.when(doneJobProjection.getId()).thenReturn(1002);
        Mockito.when(doneJobProjection.getDescription()).thenReturn("Polish bracelet");
        Mockito.when(doneJobProjection.getObjectname()).thenReturn("Bracelet");
        Mockito.when(doneJobProjection.getFinalprice()).thenReturn(120);
        Mockito.when(doneJobProjection.getDone()).thenReturn(doneTime);

        JobGroupFullInfoProjection jobGroupProjection = Mockito.mock(JobGroupFullInfoProjection.class);
        Mockito.when(jobGroupProjection.getId()).thenReturn(20);
        Mockito.when(jobGroupProjection.getBringedin()).thenReturn(bringin);
        Mockito.when(jobGroupProjection.getDeadline()).thenReturn(deadline);
        Mockito.when(jobGroupProjection.getJobs()).thenReturn(Set.of(inProgressJobProjection, doneJobProjection));

        PersonFullInfoProjection personProjection = Mockito.mock(PersonFullInfoProjection.class);
        Mockito.when(personProjection.getId()).thenReturn(10);
        Mockito.when(personProjection.getName()).thenReturn("John Doe");
        Mockito.when(personProjection.getJobGroups()).thenReturn(Set.of(jobGroupProjection));

        Mockito.when(personRepository.findPersonsWithActiveJobsProjection()).thenReturn(Set.of(personProjection));

        ActiveJobsRequestDTO result = jobGroupService.getActiveJobsGroups();

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getGroups());
        Assertions.assertEquals(1, result.getGroups().size());

        ActiveJobsRequestDTO.JobGroup mappedGroup = result.getGroups().iterator().next();
        Assertions.assertEquals(20, mappedGroup.getGroupId());
        Assertions.assertEquals(10, mappedGroup.getPersonId());
        Assertions.assertEquals("John Doe", mappedGroup.getPersonname());
        Assertions.assertEquals(bringin, mappedGroup.getUploadDate());
        Assertions.assertEquals(deadline, mappedGroup.getDeadline());

        Assertions.assertNotNull(mappedGroup.getInProgressJobs());
        Assertions.assertEquals(1, mappedGroup.getInProgressJobs().size());
        ActiveJobsRequestDTO.JobGroup.InProgressJob mappedInProgress = mappedGroup.getInProgressJobs().iterator().next();
        Assertions.assertEquals(1001, mappedInProgress.getId());
        Assertions.assertEquals("Resize ring", mappedInProgress.getDescription());
        Assertions.assertEquals("Ring", mappedInProgress.getObjectname());
        Assertions.assertEquals(100, mappedInProgress.getPricemin());
        Assertions.assertEquals(180, mappedInProgress.getPricemax());

        Assertions.assertNotNull(mappedGroup.getDoneJobs());
        Assertions.assertEquals(1, mappedGroup.getDoneJobs().size());
        ActiveJobsRequestDTO.JobGroup.DoneJob mappedDone = mappedGroup.getDoneJobs().iterator().next();
        Assertions.assertEquals(1002, mappedDone.getId());
        Assertions.assertEquals("Polish bracelet", mappedDone.getDescription());
        Assertions.assertEquals("Bracelet", mappedDone.getObjectname());
        Assertions.assertEquals(120, mappedDone.getFinalPrice());
        Assertions.assertEquals(doneTime, mappedDone.getFinishTime());

        Mockito.verify(personRepository, Mockito.times(1)).findPersonsWithActiveJobsProjection();
    }

    @Test
    public void setDoneJobsToPickedUpThrowsValidationExceptionWhenPickedUpBeforeDoneTest() {
        JobGroup group = new JobGroup();
        group.setId(10);
        group.setBringedin(LocalDateTime.of(2026, 2, 10, 10, 0));
        Job job = new Job();
        job.setId(1001);
        job.setDone(LocalDateTime.of(2026, 2, 10, 12, 0));
        job.setPickup(null);
        group.setJobs(Set.of(job));

        JobPickedUpDTO request = new JobPickedUpDTO(LocalDateTime.of(2026, 2, 10, 11, 0));

        Mockito.when(jobGroupRepository.findById(10)).thenReturn(Optional.of(group));

        ValidationException exception = Assertions.assertThrows(
            ValidationException.class,
            () -> jobGroupService.setDoneJobsToPickedUp(10, request)
        );

        Assertions.assertEquals(1, exception.getErrors().size());
        Assertions.assertEquals("date", exception.getErrors().get(0).getField());
        Assertions.assertEquals("validation.job.pickedup.before.done", exception.getErrors().get(0).getDefaultMessage());
        Mockito.verify(jobRepository, Mockito.never()).save(Mockito.any(Job.class));
        Assertions.assertNull(job.getPickup());
    }

    @Test
    public void setDoneJobsToPickedUpShouldOnlyChangeNonPickedUp(){
        JobGroup group = new JobGroup();
        group.setId(10);
        group.setBringedin(LocalDateTime.of(2026, 2, 10, 10, 0));

        Job firstJob = new Job();
        firstJob.setId(1001);
        firstJob.setDone(LocalDateTime.of(2026, 2, 10, 10, 0));
        firstJob.setPickup(null);

        Job secondJob = new Job();
        secondJob.setId(1002);
        secondJob.setDone(LocalDateTime.of(2026, 2, 10, 10, 30));
        LocalDateTime alreadyPickedUpTime = LocalDateTime.of(2026, 2, 10, 12, 0);
        secondJob.setPickup(alreadyPickedUpTime);

        group.setJobs(Set.of(firstJob, secondJob));

        LocalDateTime pickedUpDate = LocalDateTime.of(2026, 2, 10, 13, 0);
        JobPickedUpDTO request = new JobPickedUpDTO(pickedUpDate);

        Mockito.when(jobGroupRepository.findById(10)).thenReturn(Optional.of(group));

        jobGroupService.setDoneJobsToPickedUp(10, request);

        Assertions.assertEquals(pickedUpDate, firstJob.getPickup());
        Assertions.assertEquals(alreadyPickedUpTime, secondJob.getPickup());

        ArgumentCaptor<Job> savedJobsCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository, Mockito.times(1)).save(savedJobsCaptor.capture());
        Assertions.assertEquals(Set.of(firstJob), Set.copyOf(savedJobsCaptor.getAllValues()));
    }

    @Test
    public void setDoneJobsToPickedUpSetsGivenDateAndSavesEveryJobTest() {
        JobGroup group = new JobGroup();
        group.setId(10);
        group.setBringedin(LocalDateTime.of(2026, 2, 10, 10, 0));

        Job firstJob = new Job();
        firstJob.setId(1001);
        firstJob.setDone(LocalDateTime.of(2026, 2, 10, 10, 0));
        firstJob.setPickup(null);

        Job secondJob = new Job();
        secondJob.setId(1002);
        secondJob.setDone(LocalDateTime.of(2026, 2, 10, 10, 30));
        secondJob.setPickup(null);

        group.setJobs(Set.of(firstJob, secondJob));

        LocalDateTime pickedUpDate = LocalDateTime.of(2026, 2, 10, 13, 0);
        JobPickedUpDTO request = new JobPickedUpDTO(pickedUpDate);

        Mockito.when(jobGroupRepository.findById(10)).thenReturn(Optional.of(group));

        jobGroupService.setDoneJobsToPickedUp(10, request);

        Assertions.assertEquals(pickedUpDate, firstJob.getPickup());
        Assertions.assertEquals(pickedUpDate, secondJob.getPickup());

        ArgumentCaptor<Job> savedJobsCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository, Mockito.times(2)).save(savedJobsCaptor.capture());
        Assertions.assertEquals(Set.of(firstJob, secondJob), Set.copyOf(savedJobsCaptor.getAllValues()));
    }

    @Test
    public void setDoneJobsToPickedUpSetsSystemDateWhenRequestDateIsNullTest() {
        JobGroup group = new JobGroup();
        group.setId(10);
        group.setBringedin(LocalDateTime.of(2026, 2, 10, 10, 0));

        Job firstJob = new Job();
        firstJob.setId(1001);
        firstJob.setDone(LocalDateTime.of(2026, 2, 10, 10, 0));
        firstJob.setPickup(null);

        Job secondJob = new Job();
        secondJob.setId(1002);
        secondJob.setDone(LocalDateTime.of(2026, 2, 10, 10, 30));
        secondJob.setPickup(null);

        group.setJobs(Set.of(firstJob, secondJob));

        JobPickedUpDTO request = new JobPickedUpDTO(null);

        Mockito.when(jobGroupRepository.findById(10)).thenReturn(Optional.of(group));

        LocalDateTime beforeCall = LocalDateTime.now();
        jobGroupService.setDoneJobsToPickedUp(10, request);
        LocalDateTime afterCall = LocalDateTime.now();

        Assertions.assertNotNull(firstJob.getPickup());
        Assertions.assertNotNull(secondJob.getPickup());
        Assertions.assertFalse(firstJob.getPickup().isBefore(beforeCall));
        Assertions.assertFalse(firstJob.getPickup().isAfter(afterCall));
        Assertions.assertFalse(secondJob.getPickup().isBefore(beforeCall));
        Assertions.assertFalse(secondJob.getPickup().isAfter(afterCall));

        Mockito.verify(jobRepository, Mockito.times(2)).save(Mockito.any(Job.class));
    }

    @Test
    public void getReceiptWithTwoJobsReturnsLargeEnoughPdfByteArrayTest() {
        Person person = new Person();
        person.setId(10);
        person.setName("John Doe");
        person.setAddress("Main street 1");
        person.setPhone("123456789");

        Job firstJob = new Job();
        firstJob.setId(1001);
        firstJob.setObjectname("Ring");
        firstJob.setDescription("Resize ring");
        firstJob.setMaterial("Gold");
        firstJob.setWeight(5.5);
        firstJob.setPricemin(100);
        firstJob.setPricemax(180);

        Job secondJob = new Job();
        secondJob.setId(1002);
        secondJob.setObjectname("Bracelet");
        secondJob.setDescription("Polish bracelet");
        secondJob.setMaterial("Silver");
        secondJob.setWeight(8.0);
        secondJob.setPricemin(80);
        secondJob.setPricemax(140);

        JobGroup group = new JobGroup();
        group.setId(20);
        group.setPerson(person);
        group.setBringedin(LocalDateTime.of(2026, 2, 5, 10, 30));
        group.setDeadline(LocalDateTime.of(2026, 2, 20, 16, 0));
        group.setJobs(Set.of(firstJob, secondJob));

        firstJob.setJobGroup(group);
        secondJob.setJobGroup(group);

        Mockito.when(jobGroupRepository.findById(20)).thenReturn(Optional.of(group));
        Mockito.when(systemSettingService.getValue("receipt.title")).thenReturn("Test Receipt Title");
        Mockito.when(systemSettingService.getValue("receipt.note")).thenReturn("Test receipt note text");
        Mockito.when(systemSettingService.getValue("receipt.store_data")).thenReturn("Test Store Data");
        Mockito.when(systemSettingService.getValue("receipt.store_contact")).thenReturn("Test Store Contact");

        byte[] receipt = jobGroupService.getReceipt(20);

        Assertions.assertNotNull(receipt);
        Assertions.assertTrue(receipt.length > 200, "Expected a non-trivial PDF size");
    }

    
}
