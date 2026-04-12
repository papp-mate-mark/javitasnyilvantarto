package otvosuzlet.javitasnyilntarto.converter;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import otvosuzlet.javitasnyilntarto.dto.JobDto;
import otvosuzlet.javitasnyilntarto.dto.JobGroupDto;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobGroup;

public class JobGroupDtoToJobGroupConverterTest {

    @Test
    public void convertShouldMapGroupAndJobFields() {
        JobGroupDtoToJobGroupConverter converter = new JobGroupDtoToJobGroupConverter();

        JobDto jobDto = new JobDto(
            "Ring",
            "Repair",
            "Gold",
            1000,
            2000,
            3.5,
            LocalDateTime.of(2026, 1, 5, 12, 0),
            LocalDateTime.of(2026, 1, 6, 12, 0),
            1500,
            "upload",
            "finish",
            List.of(),
            List.of()
        );

        LocalDateTime bringin = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 1, 10, 10, 0);
        JobGroupDto groupDto = new JobGroupDto(bringin, deadline, List.of(jobDto));

        JobGroup result = converter.convert(groupDto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(bringin, result.getBringedin());
        Assertions.assertEquals(deadline, result.getDeadline());
        Assertions.assertEquals(1, result.getJobs().size());

        Job mappedJob = result.getJobs().iterator().next();
        Assertions.assertEquals(jobDto.getObjectname(), mappedJob.getObjectname());
        Assertions.assertEquals(jobDto.getDescription(), mappedJob.getDescription());
        Assertions.assertEquals(jobDto.getMaterial(), mappedJob.getMaterial());
        Assertions.assertEquals(jobDto.getPricemin(), mappedJob.getPricemin());
        Assertions.assertEquals(jobDto.getPricemax(), mappedJob.getPricemax());
        Assertions.assertEquals(jobDto.getWeight(), mappedJob.getWeight());
        Assertions.assertEquals(jobDto.getFinishTime(), mappedJob.getDone());
        Assertions.assertEquals(jobDto.getPickedUpTime(), mappedJob.getPickup());
        Assertions.assertEquals(jobDto.getFinalPrice(), mappedJob.getFinalprice());
        Assertions.assertEquals(jobDto.getUploadnote(), mappedJob.getUploadnote());
        Assertions.assertEquals(jobDto.getFinishnote(), mappedJob.getFinishnote());
        Assertions.assertSame(result, mappedJob.getJobGroup());
        Assertions.assertNotNull(mappedJob.getBeforeImage());
        Assertions.assertTrue(mappedJob.getBeforeImage().isEmpty());
        Assertions.assertNotNull(mappedJob.getAfterImages());
        Assertions.assertTrue(mappedJob.getAfterImages().isEmpty());
    }
}