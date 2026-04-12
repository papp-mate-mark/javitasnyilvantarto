package otvosuzlet.javitasnyilntarto.converter;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import otvosuzlet.javitasnyilntarto.enums.ImageType;
import otvosuzlet.javitasnyilntarto.model.FullJobRequestDto;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobGroup;
import otvosuzlet.javitasnyilntarto.model.JobImage;
import otvosuzlet.javitasnyilntarto.testutil.TestObjectGenerator;

public class JobToFullJobRequestDtoConverterTest {

    @Test
    public void convertShouldMapAllFieldsAndImageIds() {
        JobToFullJobRequestDtoConverter converter = new JobToFullJobRequestDtoConverter();

        JobGroup group = TestObjectGenerator.createJobGroup(TestObjectGenerator.createPerson(1), 2);
        Job job = TestObjectGenerator.createJob(group, 3);
        job.setDone(LocalDateTime.of(2026, 1, 1, 10, 0));
        job.setPickup(LocalDateTime.of(2026, 1, 2, 10, 0));

        JobImage beforeImage = TestObjectGenerator.createJobImage(job, ImageType.BEFORE, null);
        JobImage afterImage = TestObjectGenerator.createJobImage(job, ImageType.AFTER, null);
        job.setBeforeImage(Set.of(beforeImage));
        job.setAfterImages(Set.of(afterImage));

        FullJobRequestDto dto = converter.convert(job);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(job.getId(), dto.getId());
        Assertions.assertEquals(job.getDescription(), dto.getDescription());
        Assertions.assertEquals(job.getObjectname(), dto.getObjectname());
        Assertions.assertEquals(job.getMaterial(), dto.getMaterial());
        Assertions.assertEquals(job.getWeight(), dto.getWeight());
        Assertions.assertEquals(job.getPricemin(), dto.getPricemin());
        Assertions.assertEquals(job.getPricemax(), dto.getPricemax());
        Assertions.assertEquals(job.getFinalprice(), dto.getFinalprice());
        Assertions.assertEquals(job.getDone(), dto.getDone());
        Assertions.assertEquals(job.getPickup(), dto.getPickup());
        Assertions.assertEquals(job.getUploadnote(), dto.getUploadnote());
        Assertions.assertEquals(job.getFinishnote(), dto.getFinishnote());
        Assertions.assertEquals(1, dto.getBeforeImages().size());
        Assertions.assertTrue(dto.getBeforeImages().contains(beforeImage.getId()));
        Assertions.assertEquals(1, dto.getAfterImages().size());
        Assertions.assertTrue(dto.getAfterImages().contains(afterImage.getId()));
    }
}