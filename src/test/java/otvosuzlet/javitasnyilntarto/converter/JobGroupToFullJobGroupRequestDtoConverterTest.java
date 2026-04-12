package otvosuzlet.javitasnyilntarto.converter;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.ConversionService;

import otvosuzlet.javitasnyilntarto.model.FullJobGroupRequestDto;
import otvosuzlet.javitasnyilntarto.model.FullJobRequestDto;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobGroup;
import otvosuzlet.javitasnyilntarto.testutil.TestObjectGenerator;

public class JobGroupToFullJobGroupRequestDtoConverterTest {

    @Test
    public void convertShouldMapGroupFieldsAndConvertJobs() {
        ConversionService conversionService = Mockito.mock(ConversionService.class);
        JobGroupToFullJobGroupRequestDtoConverter converter = new JobGroupToFullJobGroupRequestDtoConverter(conversionService);

        JobGroup group = TestObjectGenerator.createJobGroup(TestObjectGenerator.createPerson(10), 20);
        group.setBringedin(LocalDateTime.of(2026, 2, 1, 9, 0));
        group.setDeadline(LocalDateTime.of(2026, 2, 10, 17, 0));

        Job job1 = TestObjectGenerator.createJob(group, 101);
        Job job2 = TestObjectGenerator.createJob(group, 102);
        group.setJobs(Set.of(job1, job2));

        FullJobRequestDto mapped1 = new FullJobRequestDto();
        mapped1.setId(101);
        FullJobRequestDto mapped2 = new FullJobRequestDto();
        mapped2.setId(102);

        Mockito.when(conversionService.convert(job1, FullJobRequestDto.class)).thenReturn(mapped1);
        Mockito.when(conversionService.convert(job2, FullJobRequestDto.class)).thenReturn(mapped2);

        FullJobGroupRequestDto result = converter.convert(group);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(group.getId(), result.getId());
        Assertions.assertEquals(group.getBringedin(), result.getBringedin());
        Assertions.assertEquals(group.getDeadline(), result.getDeadline());
        Assertions.assertNotNull(result.getJobs());
        Assertions.assertEquals(2, result.getJobs().size());
        Assertions.assertTrue(result.getJobs().stream().anyMatch(job -> job.getId().equals(101)));
        Assertions.assertTrue(result.getJobs().stream().anyMatch(job -> job.getId().equals(102)));

        Mockito.verify(conversionService, Mockito.times(1)).convert(job1, FullJobRequestDto.class);
        Mockito.verify(conversionService, Mockito.times(1)).convert(job2, FullJobRequestDto.class);
    }
}