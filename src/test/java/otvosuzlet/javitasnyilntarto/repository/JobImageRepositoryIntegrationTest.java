package otvosuzlet.javitasnyilntarto.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import otvosuzlet.javitasnyilntarto.enums.ImageType;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobGroup;
import otvosuzlet.javitasnyilntarto.model.JobImage;
import otvosuzlet.javitasnyilntarto.model.Person;
import otvosuzlet.javitasnyilntarto.testutil.TestObjectGenerator;

@SpringBootTest
class JobImageRepositoryIntegrationTest {

    @Autowired
    private JobImageRepository jobImageRepository;

    @Autowired
    private PersonRepository personRepository;

    @Test
    @Transactional
    void testFindPersonIdByImageId() {
        Person person = TestObjectGenerator.createPerson(null);
        person.setId(null); //reset ID to allow DB generation
        JobGroup jobGroup = TestObjectGenerator.createJobGroup(person, null);
        jobGroup.setId(null);
        Job job = TestObjectGenerator.createJob(jobGroup, null);
        job.setId(null);
        JobImage jobImage = TestObjectGenerator.createJobImage(job, ImageType.BEFORE , null);
        jobImage.setId(null);
        job.setAfterImages(Set.of(jobImage));
        jobGroup.setJobs(Set.of(job));
        person.setJobGroups(Set.of(jobGroup));

        Person savedPerson = personRepository.save(person);
        personRepository.flush();
        
        Integer foundPersonId = jobImageRepository.findPersonIdByImageId(savedPerson.getJobGroups().iterator().next().getJobs().iterator().next().getAfterImages().iterator().next().getId());
        assertEquals(foundPersonId, savedPerson.getId());
    }
}
