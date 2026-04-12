package otvosuzlet.javitasnyilntarto.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import otvosuzlet.javitasnyilntarto.dto.JobCompleteDTO;
import otvosuzlet.javitasnyilntarto.dto.JobPickedUpDTO;
import otvosuzlet.javitasnyilntarto.dto.JobSearchDto;
import otvosuzlet.javitasnyilntarto.dto.JobSearchJobDataDTO;
import otvosuzlet.javitasnyilntarto.enums.ImageType;
import otvosuzlet.javitasnyilntarto.exceptions.ValidationException;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobGroup;
import otvosuzlet.javitasnyilntarto.model.JobImage;
import otvosuzlet.javitasnyilntarto.model.Person;
import otvosuzlet.javitasnyilntarto.repository.JobRepository;
import otvosuzlet.javitasnyilntarto.testutil.TestObjectGenerator;

@ExtendWith(MockitoExtension.class)
public class JobServiceImplTest {
    @InjectMocks
    private JobServiceImpl jobService;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobImageService imageService;
    
    @Mock
    private ConversionService conversionService;

    @Mock 
    private Validator validator;

    @Test
    public void deleteJobTest() {
        Integer jobId = 1;
        Person person = TestObjectGenerator.createPerson(null);
        JobGroup jobGroup = TestObjectGenerator.createJobGroup(person, null);
        person.setJobGroups(Set.of(jobGroup));
        Job job1 = TestObjectGenerator.createJob(jobGroup, null);
        Job job2 = TestObjectGenerator.createJob(jobGroup, null);
        job1.setBeforeImage(Set.of(TestObjectGenerator.createJobImage(job1, ImageType.BEFORE, null)));
        job1.setAfterImages(Set.of(TestObjectGenerator.createJobImage(job1, ImageType.AFTER, null),TestObjectGenerator.createJobImage(job1, ImageType.AFTER, null)));
        job2.setBeforeImage(Set.of(TestObjectGenerator.createJobImage(job2, ImageType.BEFORE, null)));
        jobGroup.setJobs(Set.of(job1, job2));

        Mockito.when(jobRepository.findById(jobId)).thenReturn(Optional.of(job1));
        jobService.deleteJob(jobId);
        Mockito.verify(jobRepository).delete(job1);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void validateAndCompleteJobThrowsValidationErrorsTest() {
        Integer jobId = 1;
        Person person = TestObjectGenerator.createPerson(1);
        JobGroup jobGroup = TestObjectGenerator.createJobGroup(person, 2);
        person.setJobGroups(Set.of(jobGroup));

        Job job = TestObjectGenerator.createJob(jobGroup, 3);
        job.setDone(LocalDateTime.of(2026, 2, 10, 10, 0));
        jobGroup.setBringedin(LocalDateTime.of(2026, 2, 11, 10, 0));
        jobGroup.setJobs(Set.of(job));

        JobCompleteDTO request = new JobCompleteDTO(
            -1,
            "finish",
            LocalDateTime.of(2026, 2, 10, 9, 0),
            List.of(101, 102)
        );
        @SuppressWarnings("rawtypes")
        ConstraintViolation errorMock = Mockito.mock(ConstraintViolation.class);
        Path propertyPathMock = Mockito.mock(Path.class);
        Mockito.when(propertyPathMock.toString()).thenReturn("price");
        Mockito.when(errorMock.getPropertyPath()).thenReturn(propertyPathMock);
        Mockito.when(errorMock.getMessage()).thenReturn("validation.non.negative");
        Mockito.when(validator.validate(Mockito.any())).thenReturn(Set.of(errorMock));
        Mockito.when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        ValidationException exception = Assertions.assertThrows(
            ValidationException.class,
            () -> jobService.validateAndCompleteJob(jobId, request)
        );

        Assertions.assertEquals(3, exception.getErrors().size());
        Assertions.assertTrue(exception.getErrors().stream().anyMatch(error ->
            "date".equals(error.getField()) && "validation.job.alreadyDone".equals(error.getDefaultMessage())
        ));
        Assertions.assertTrue(exception.getErrors().stream().anyMatch(error ->
            "date".equals(error.getField()) && "validation.job.beforeBringin".equals(error.getDefaultMessage())
        ));
        Assertions.assertTrue(exception.getErrors().stream().anyMatch(error ->
            "price".equals(error.getField()) && "validation.non.negative".equals(error.getDefaultMessage())
        ));

        Mockito.verify(imageService, Mockito.never())
            .attachImagesByIds(Mockito.anyCollection(), Mockito.any(Job.class), Mockito.any(ImageType.class));
        Mockito.verify(jobRepository, Mockito.never()).save(Mockito.any(Job.class));
    }


    @Test
    public void validateAndCompleteJobTest() {
        Integer jobId = 20;
        Person person = TestObjectGenerator.createPerson(20);
        JobGroup jobGroup = TestObjectGenerator.createJobGroup(person, 21);
        person.setJobGroups(Set.of(jobGroup));

        Job job = TestObjectGenerator.createJob(jobGroup, 22);
        job.setDone(null);
        job.setPickup(null);
        job.setFinalprice(null);
        job.setFinishnote(null);
        job.setPricemin(777);
        jobGroup.setBringedin(LocalDateTime.of(2026, 2, 1, 10, 0));
        jobGroup.setJobs(Set.of(job));

        JobImage image = TestObjectGenerator.createJobImage(null, null, 301);
        image.setJob(job);
        image.setType(ImageType.AFTER);
        Set<JobImage> attachedImages = Set.of(image);

        Mockito.when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        Mockito.when(imageService.attachImagesByIds(Mockito.eq(List.of(301)), Mockito.same(job), Mockito.eq(ImageType.AFTER)))
            .thenReturn(attachedImages);

        LocalDateTime doneDate = LocalDateTime.of(2026, 2, 3, 12, 0);
        JobCompleteDTO request = new JobCompleteDTO(
            null,
            "done note",
            doneDate,
            List.of(301)
        );

        jobService.validateAndCompleteJob(jobId, request);

        ArgumentCaptor<Job> savedJobCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository, Mockito.times(1)).save(savedJobCaptor.capture());
        Mockito.verify(imageService, Mockito.times(1))
            .attachImagesByIds(Mockito.eq(List.of(301)), Mockito.same(job), Mockito.eq(ImageType.AFTER));

        Job savedJob = savedJobCaptor.getValue();
        Assertions.assertSame(job, savedJob);
        Assertions.assertEquals(doneDate, savedJob.getDone());
        Assertions.assertEquals(777, savedJob.getFinalprice());
        Assertions.assertEquals("done note", savedJob.getFinishnote());
        Assertions.assertTrue(savedJob.getAfterImages().containsAll(attachedImages));
        Assertions.assertTrue(savedJob.getAfterImages().stream().anyMatch(savedImage -> savedImage.getId().equals(301)));
        Assertions.assertEquals(savedJob, image.getJob());
        Assertions.assertEquals(ImageType.AFTER, image.getType());
    }

    @Test
    public void pickedUpJobShouldValidateGivenDate(){
        Person person = TestObjectGenerator.createPerson(null);
        JobGroup jobGroup = TestObjectGenerator.createJobGroup(person, null);
        jobGroup.setBringedin(LocalDateTime.of(2020, 1, 1, 10, 0));
        jobGroup.setDeadline(LocalDateTime.of(2023, 1, 1, 10, 0));
        Job job = TestObjectGenerator.createJob(jobGroup, null);
        job.setDone(LocalDateTime.of(2022, 1, 1, 10, 0));
        person.setJobGroups(Set.of(jobGroup));

        JobPickedUpDTO request = new JobPickedUpDTO(
            LocalDateTime.of(2021, 1, 1, 10, 0)
        );

        Mockito.when(jobRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(job));

        ValidationException exception = Assertions.assertThrows(
            ValidationException.class,
            () -> jobService.pickedUpJob(job.getId(), request)
        );

        Assertions.assertEquals(1, exception.getErrors().size());
        Assertions.assertEquals("date", exception.getErrors().get(0).getField());
        Assertions.assertEquals("validation.job.pickup.before.done", exception.getErrors().get(0).getDefaultMessage());
        Mockito.verify(jobRepository, Mockito.never()).save(Mockito.any(Job.class));

    }

    @Test
    public void pickedUpJobShouldValidateFallbackDate(){
        Person person = TestObjectGenerator.createPerson(null);
        JobGroup jobGroup = TestObjectGenerator.createJobGroup(person, null);
        jobGroup.setBringedin(LocalDateTime.of(2020, 1, 1, 10, 0));
        jobGroup.setDeadline(LocalDateTime.of(2023, 1, 1, 10, 0));
        Job job = TestObjectGenerator.createJob(jobGroup, null);
        job.setDone(LocalDateTime.of(2022, 1, 1, 10, 0));
        person.setJobGroups(Set.of(jobGroup));

        JobPickedUpDTO request = new JobPickedUpDTO();
        LocalDateTime fixedNow = LocalDateTime.of(2021, 1, 1, 10, 0);

        Mockito.when(jobRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(job));
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedNow);

            ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> jobService.pickedUpJob(job.getId(), request)
            );

            Assertions.assertEquals(1, exception.getErrors().size());
            Assertions.assertEquals("date", exception.getErrors().get(0).getField());
            Assertions.assertEquals("validation.job.pickup.before.done", exception.getErrors().get(0).getDefaultMessage());
        }
        Mockito.verify(jobRepository, Mockito.never()).save(Mockito.any(Job.class));

    }


    @Test
    public void pickedUpJobShouldValidate(){
        Person person = TestObjectGenerator.createPerson(null);
        JobGroup jobGroup = TestObjectGenerator.createJobGroup(person, null);
        jobGroup.setBringedin(LocalDateTime.of(2020, 1, 1, 10, 0));
        jobGroup.setDeadline(LocalDateTime.of(2023, 1, 1, 10, 0));
        Job job = TestObjectGenerator.createJob(jobGroup, null);
        job.setDone(LocalDateTime.of(2022, 1, 1, 10, 0));
        person.setJobGroups(Set.of(jobGroup));

        JobPickedUpDTO request = new JobPickedUpDTO(
            LocalDateTime.of(2021, 1, 1, 10, 0)
        );

        Mockito.when(jobRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(job));

        ValidationException exception = Assertions.assertThrows(
            ValidationException.class,
            () -> jobService.pickedUpJob(job.getId(), request)
        );

        Assertions.assertEquals(1, exception.getErrors().size());
        Assertions.assertEquals("date", exception.getErrors().get(0).getField());
        Assertions.assertEquals("validation.job.pickup.before.done", exception.getErrors().get(0).getDefaultMessage());
        Mockito.verify(jobRepository, Mockito.never()).save(Mockito.any(Job.class));
    }

    @Test
    public void searchForJobShouldMapSortAndReturnMappedDtos() {
        JobSearchDto filter = new JobSearchDto();
        filter.setName("John");
        filter.setObjectname("Ring");

        Pageable inputPageable = PageRequest.of(
            2,
            5,
            Sort.by(
                Sort.Order.asc("personName"),
                Sort.Order.desc("bringin"),
                Sort.Order.asc("jobid")
            )
        );

        Person person = TestObjectGenerator.createPerson(1001);
        person.setName("John Doe");
        JobGroup group = TestObjectGenerator.createJobGroup(person, 2002);
        group.setBringedin(LocalDateTime.of(2026, 1, 10, 9, 30));
        group.setDeadline(LocalDateTime.of(2026, 1, 20, 9, 30));

        Job firstJob = TestObjectGenerator.createJob(group, 3003);
        firstJob.setObjectname("Ring");
        firstJob.setDescription("Resize");
        firstJob.setDone(LocalDateTime.of(2026, 1, 11, 14, 0));
        firstJob.setPickup(LocalDateTime.of(2026, 1, 12, 16, 0));

        Job secondJob = TestObjectGenerator.createJob(group, 3004);
        secondJob.setObjectname("Necklace");
        secondJob.setDescription("Polish");
        secondJob.setDone(LocalDateTime.of(2026, 1, 15, 11, 0));
        secondJob.setPickup(LocalDateTime.of(2026, 1, 17, 10, 0));

        Page<Job> mockedRepoPage = new PageImpl<>(List.of(firstJob, secondJob), inputPageable, 12);
        Mockito.when(jobRepository.findAll(Mockito.<Specification<Job>>any(), Mockito.any(Pageable.class)))
            .thenReturn(mockedRepoPage);

        Page<JobSearchJobDataDTO> result = jobService.searchForJob(filter, inputPageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(jobRepository, Mockito.times(1)).findAll(Mockito.<Specification<Job>>any(), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        Assertions.assertEquals(2, capturedPageable.getPageNumber());
        Assertions.assertEquals(5, capturedPageable.getPageSize());

        List<Sort.Order> mappedOrders = capturedPageable.getSort().stream().toList();
        Assertions.assertEquals(3, mappedOrders.size());
        Assertions.assertEquals("jobGroup.person.name", mappedOrders.get(0).getProperty());
        Assertions.assertEquals(Sort.Direction.ASC, mappedOrders.get(0).getDirection());
        Assertions.assertEquals("jobGroup.bringedin", mappedOrders.get(1).getProperty());
        Assertions.assertEquals(Sort.Direction.DESC, mappedOrders.get(1).getDirection());
        Assertions.assertEquals("id", mappedOrders.get(2).getProperty());
        Assertions.assertEquals(Sort.Direction.ASC, mappedOrders.get(2).getDirection());

        Assertions.assertEquals(12, result.getTotalElements());
        Assertions.assertEquals(2, result.getContent().size());

        JobSearchJobDataDTO firstDto = result.getContent().get(0);
        Assertions.assertEquals(person.getId(), firstDto.getPersonid());
        Assertions.assertEquals(group.getId(), firstDto.getJobgroupid());
        Assertions.assertEquals(firstJob.getId(), firstDto.getJobid());
        Assertions.assertEquals(person.getName(), firstDto.getPersonName());
        Assertions.assertEquals(firstJob.getObjectname(), firstDto.getObjectName());
        Assertions.assertEquals(firstJob.getDescription(), firstDto.getDescription());
        Assertions.assertEquals(group.getBringedin(), firstDto.getBringin());
        Assertions.assertEquals(firstJob.getDone(), firstDto.getDone());
        Assertions.assertEquals(firstJob.getPickup(), firstDto.getPickup());

        JobSearchJobDataDTO secondDto = result.getContent().get(1);
        Assertions.assertEquals(secondJob.getId(), secondDto.getJobid());
        Assertions.assertEquals(secondJob.getObjectname(), secondDto.getObjectName());
        Assertions.assertEquals(secondJob.getDescription(), secondDto.getDescription());
        Assertions.assertEquals(secondJob.getDone(), secondDto.getDone());
        Assertions.assertEquals(secondJob.getPickup(), secondDto.getPickup());
    }


    @Test
    public void getSummeryTest() throws Exception
    {
        Person person = TestObjectGenerator.createPerson(100);
        JobGroup jobGroup = TestObjectGenerator.createJobGroup(person, 200);
        Job job = TestObjectGenerator.createJob(jobGroup, 1);
        Set<JobImage> beforeImages = Set.of(TestObjectGenerator.createJobImage(job, ImageType.BEFORE, 301));
        Set<JobImage> afterImages = Set.of(TestObjectGenerator.createJobImage(job, ImageType.AFTER, 302), TestObjectGenerator.createJobImage(job, ImageType.AFTER, 303));
        job.setBeforeImage(beforeImages);
        job.setAfterImages(afterImages);
        jobGroup.setJobs(Set.of(job));
        person.setJobGroups(Set.of(jobGroup));
        Mockito.when(jobRepository.findById(1)).thenReturn(Optional.of(job));
        File testImage = new ClassPathResource("static/testimage.jpg").getFile();
        Mockito.when(imageService.getFullImageFile(Mockito.anyInt())).thenReturn(testImage);
        byte[] result = jobService.getSummary(1);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.length > 50);
        List<Integer> expectedIds = Stream.concat(
                job.getBeforeImage().stream(),
                job.getAfterImages().stream()
        ).map(JobImage::getId)
        .toList();
        Mockito.verify(imageService, Mockito.times(expectedIds.size())).getFullImageFile(Mockito.argThat(value -> expectedIds.contains(value)));
    }

    
}
