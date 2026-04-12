package otvosuzlet.javitasnyilntarto.testutil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import otvosuzlet.javitasnyilntarto.dto.JobDto;
import otvosuzlet.javitasnyilntarto.dto.JobGroupDto;
import otvosuzlet.javitasnyilntarto.enums.ImageType;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobGroup;
import otvosuzlet.javitasnyilntarto.model.JobImage;
import otvosuzlet.javitasnyilntarto.model.Person;

public class TestObjectGenerator {
    public static Person createPerson(Integer id){
        Person person = new Person();
        int random = ThreadLocalRandom.current().nextInt(1, 11);
        if(id == null){
            id = random;
        }
        person.setName("Person " + id);
        person.setAddress("Address " + id);
        person.setPhone("Phone " + id);
        person.setId(id);
        return person;
    }
    public static JobGroup createJobGroup(Person person, Integer id){
        JobGroup jobGroup = new JobGroup();
        int random = ThreadLocalRandom.current().nextInt(1, 11);
        if(id == null){
            id = random;
        }
        jobGroup.setBringedin(generateRandomLocalDateTime(null, null));
        jobGroup.setDeadline(generateRandomLocalDateTime(jobGroup.getBringedin(), null));
        jobGroup.setPerson(person);
        jobGroup.setId(id);
        return jobGroup;
    }

    public static Job createJob(JobGroup jobGroup, Integer id){
        Job job = new Job();
        int random = ThreadLocalRandom.current().nextInt(1, 11);
        if(id == null){
            id = random;
        }
        job.setJobGroup(jobGroup);
        job.setDone(generateRandomLocalDateTime(jobGroup.getBringedin(), jobGroup.getDeadline()));
        job.setPickup(generateRandomLocalDateTime(job.getDone(), jobGroup.getDeadline()));
        job.setDescription("Description " + id);
        job.setObjectname("Object " + id);
        job.setMaterial("Material " + id);
        job.setWeight(10.0 + random);
        job.setPricemin(100 * random);
        job.setPricemax(200 * random);
        job.setFinalprice(150 * random);
        job.setUploadnote("Upload note " + id);
        job.setFinishnote("Finish note " + id);
        job.setId(id);
        return job;
    }
    public static JobImage createJobImage(Job job, ImageType type, Integer id){
        JobImage jobImage = new JobImage();
        int random = ThreadLocalRandom.current().nextInt(1, 11);
        if(id == null){
            id = random;
        }
        jobImage.setJob(job);
        jobImage.setThumbnailFilename("filename" + id + ".jpg");
        jobImage.setImageFilename("image" + id + ".jpg");
        jobImage.setCreateTime(generateRandomLocalDateTime(null, null));
        jobImage.setType(type);
        jobImage.setId(id);
        return jobImage;
    }

    public static LocalDateTime generateRandomLocalDateTime(LocalDateTime start, LocalDateTime end) {
        LocalDateTime effectiveStart = start != null ? start : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime effectiveEnd = end != null ? end : LocalDateTime.of(2100, 1, 1, 0, 0);

        long startEpochSecond = effectiveStart.toEpochSecond(java.time.ZoneOffset.UTC);
        long endEpochSecond = effectiveEnd.toEpochSecond(java.time.ZoneOffset.UTC);

        long randomEpochSecond = ThreadLocalRandom.current().nextLong(startEpochSecond, endEpochSecond + 1);

        return LocalDateTime.ofEpochSecond(randomEpochSecond, 0, java.time.ZoneOffset.UTC);
    }

    public static JobGroupDto createFullyInicilizedJobGroupDto(){
        JobGroupDto jobGroupDto = new JobGroupDto();

        jobGroupDto.setBringin(generateRandomLocalDateTime(null, null));
        jobGroupDto.setDeadline(generateRandomLocalDateTime(jobGroupDto.getBringin(), null));
        return jobGroupDto;
    }
    public static JobDto createFullyInicilizedJobDto(JobGroupDto jobGroup){
        JobDto jobDto = new JobDto();
        int random = ThreadLocalRandom.current().nextInt(1, 11);
        
        jobDto.setFinishTime(generateRandomLocalDateTime(jobGroup.getBringin(), jobGroup.getDeadline()));
        jobDto.setPickedUpTime(generateRandomLocalDateTime(jobDto.getFinishTime(), jobGroup.getDeadline()));
        jobDto.setDescription("Description " + random);
        jobDto.setObjectname("Object " + random);
        jobDto.setMaterial("Material " + random);
        jobDto.setWeight(10.0 + random);
        jobDto.setPricemin(100 * random);
        jobDto.setPricemax(200 * random);
        jobDto.setFinalPrice(150 * random);
        jobDto.setUploadnote("Upload note " + random);
        jobDto.setFinishnote("Finish note " + random);
        jobDto.setImagesBefore(List.of(random, random + 1));
        jobDto.setImagesAfter(List.of(random + 2, random + 3));
        return jobDto;
    }

}