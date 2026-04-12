package otvosuzlet.javitasnyilntarto.converter;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import otvosuzlet.javitasnyilntarto.dto.FullPersonDataRequestDto;
import otvosuzlet.javitasnyilntarto.model.FullJobGroupRequestDto;
import otvosuzlet.javitasnyilntarto.model.Person;

import java.util.stream.Collectors;

@Component
public class PersonToFullPersonDataRequestDtoConverter implements Converter<Person, FullPersonDataRequestDto> {

    private final ConversionService conversionService;

    public PersonToFullPersonDataRequestDtoConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public FullPersonDataRequestDto convert(Person source) {
        FullPersonDataRequestDto dto = new FullPersonDataRequestDto();
        dto.setId(source.getId());
        dto.setName(source.getName());
        dto.setAddress(source.getAddress());
        dto.setPhone(source.getPhone());

        if (source.getJobGroups() != null) {
            dto.setJobGroups(source.getJobGroups().stream()
                    .map(group -> conversionService.convert(group, FullJobGroupRequestDto.class))
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
