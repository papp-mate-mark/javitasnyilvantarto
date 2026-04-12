package otvosuzlet.javitasnyilntarto.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobGroup;
import otvosuzlet.javitasnyilntarto.model.Person;
import otvosuzlet.javitasnyilntarto.projections.PersonFullInfoProjection;
import otvosuzlet.javitasnyilntarto.testutil.TestObjectGenerator;

@SpringBootTest
class PersonRepositoryIntegrationTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    @Transactional
    void testFindPersonsWithActiveJobsProjection() {
        Person person1 = TestObjectGenerator.createPerson(null);
        person1.setId(null); //reset ID to allow DB generation
        JobGroup jobGroup1 = TestObjectGenerator.createJobGroup(person1, null);
        jobGroup1.setId(null);
        jobGroup1.setBringedin(LocalDateTime.now().minusDays(3));
        jobGroup1.setDeadline(LocalDateTime.now().plusDays(1));
        Job job1 = TestObjectGenerator.createJob(jobGroup1, null);
        job1.setId(null);
        job1.setDone(LocalDateTime.now().minusDays(2));
        job1.setPickup(LocalDateTime.now().minusDays(1));
        jobGroup1.setJobs(Set.of(job1));
        person1.setJobGroups(Set.of(jobGroup1));    
        Person savedPerson1 = personRepository.save(person1);
        personRepository.flush();

        Person person2 = TestObjectGenerator.createPerson(null);
        person2.setId(null); 

        JobGroup jobGroup2 = TestObjectGenerator.createJobGroup(person2, null);
        jobGroup2.setId(null);
        jobGroup2.setBringedin(LocalDateTime.now().minusDays(3));
        jobGroup2.setDeadline(LocalDateTime.now().plusDays(1));
        Job job2 = TestObjectGenerator.createJob(jobGroup2, null);
        job2.setId(null);
        job2.setDone(null);
        job2.setPickup(null);
        jobGroup2.setJobs(Set.of(job2));
        person2.setJobGroups(Set.of(jobGroup2));
        Person savedPerson2 = personRepository.save(person2);
        personRepository.flush();
        Set<PersonFullInfoProjection> activeJobs = personRepository.findPersonsWithActiveJobsProjection();
        assertFalse(activeJobs.stream().anyMatch(g-> g.getId()==savedPerson1.getId()));
        assertTrue(activeJobs.stream().anyMatch(g-> g.getId()==savedPerson2.getId()));
        
    }
}
