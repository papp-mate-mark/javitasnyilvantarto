package otvosuzlet.javitasnyilntarto.converter;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import otvosuzlet.javitasnyilntarto.model.FullJobGroupRequestDto;
import otvosuzlet.javitasnyilntarto.model.FullJobRequestDto;
import otvosuzlet.javitasnyilntarto.model.JobGroup;

import java.util.stream.Collectors;

@Component
public class JobGroupToFullJobGroupRequestDtoConverter implements Converter<JobGroup, FullJobGroupRequestDto> {

    private final ConversionService conversionService;

    public JobGroupToFullJobGroupRequestDtoConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public FullJobGroupRequestDto convert(JobGroup source) {
        FullJobGroupRequestDto dto = new FullJobGroupRequestDto();
        dto.setId(source.getId());
        dto.setBringedin(source.getBringedin());
        dto.setDeadline(source.getDeadline());

        if (source.getJobs() != null) {
            dto.setJobs(source.getJobs().stream()
                    .map(job -> conversionService.convert(job, FullJobRequestDto.class))
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
