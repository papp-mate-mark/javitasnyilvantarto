package otvosuzlet.javitasnyilntarto.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import otvosuzlet.javitasnyilntarto.model.FullJobRequestDto;
import otvosuzlet.javitasnyilntarto.model.Job;

import java.util.stream.Collectors;

@Component
public class JobToFullJobRequestDtoConverter implements Converter<Job, FullJobRequestDto> {

    @Override
    public FullJobRequestDto convert(Job source) {
        FullJobRequestDto dto = new FullJobRequestDto();
        dto.setId(source.getId());
        dto.setDescription(source.getDescription());
        dto.setObjectname(source.getObjectname());
        dto.setMaterial(source.getMaterial());
        dto.setWeight(source.getWeight());
        dto.setPricemin(source.getPricemin());
        dto.setPricemax(source.getPricemax());
        dto.setFinalprice(source.getFinalprice());
        dto.setDone(source.getDone());
        dto.setPickup(source.getPickup());
        dto.setUploadnote(source.getUploadnote());
        dto.setFinishnote(source.getFinishnote());

        if (source.getBeforeImage() != null) {
            dto.setBeforeImages(source.getBeforeImage().stream()
                    .map(image -> image.getId())
                    .collect(Collectors.toList()));
        }

        if (source.getAfterImages() != null) {
            dto.setAfterImages(source.getAfterImages().stream()
                    .map(image -> image.getId())
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
