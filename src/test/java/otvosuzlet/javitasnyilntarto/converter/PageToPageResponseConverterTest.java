package otvosuzlet.javitasnyilntarto.converter;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import otvosuzlet.javitasnyilntarto.dto.PageResponse;

public class PageToPageResponseConverterTest {

    @Test
    public void convertShouldMapPageMetadataAndContent() {
        PageToPageResponseConverter converter = new PageToPageResponseConverter();
        Page<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 5);

        PageResponse<?> result = converter.convert(page);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(page.getContent(), result.getContent());
        Assertions.assertEquals(page.getTotalElements(), result.getTotalElements());
        Assertions.assertEquals(page.getTotalPages(), result.getTotalPages());
        Assertions.assertEquals(page.getNumber(), result.getPage());
        Assertions.assertEquals(page.getSize(), result.getSize());
        Assertions.assertEquals(page.isFirst(), result.isFirst());
        Assertions.assertEquals(page.isLast(), result.isLast());
    }
}