package otvosuzlet.javitasnyilntarto.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import otvosuzlet.javitasnyilntarto.dto.PageResponse;

@Component
public class PageToPageResponseConverter implements Converter<Page<?>, PageResponse<?>> {

    @Override
    public PageResponse<?> convert(Page<?> source) {
        return new PageResponse<>(
                source.getContent(),
                source.getTotalElements(),
                source.getTotalPages(),
                source.getNumber(),
                source.getSize(),
                source.isFirst(),
                source.isLast());
    }
}