package otvosuzlet.javitasnyilntarto.converter;

import java.util.HashSet;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import otvosuzlet.javitasnyilntarto.dto.JobDto;
import otvosuzlet.javitasnyilntarto.dto.JobGroupDto;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobGroup;

@Component
public class JobGroupDtoToJobGroupConverter implements Converter<JobGroupDto, JobGroup> {

    @Override
    public JobGroup convert(JobGroupDto dto) {
        JobGroup group = new JobGroup();

        group.setBringedin(dto.getBringin());
        group.setDeadline(dto.getDeadline());

        Set<Job> jobs = new HashSet<>();
        for (JobDto jobDto : dto.getJobs()) {
            Job job = new Job();
            job.setObjectname(jobDto.getObjectname());
            job.setDescription(jobDto.getDescription());
            job.setMaterial(jobDto.getMaterial());
            job.setPricemin(jobDto.getPricemin());
            job.setPricemax(jobDto.getPricemax());
            job.setWeight(jobDto.getWeight());
            job.setJobGroup(group);
            job.setDone(jobDto.getFinishTime());
            job.setPickup(jobDto.getPickedUpTime());
            job.setFinalprice(jobDto.getFinalPrice());
            job.setUploadnote(jobDto.getUploadnote());
            job.setFinishnote(jobDto.getFinishnote());
            job.setBeforeImage(new HashSet<>());
            job.setAfterImages(new HashSet<>());
            jobs.add(job);
        }

        group.setJobs(jobs);

        return group;
    }
}
