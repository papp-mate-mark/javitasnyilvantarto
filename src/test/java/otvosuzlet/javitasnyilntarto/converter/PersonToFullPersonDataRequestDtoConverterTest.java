package otvosuzlet.javitasnyilntarto.converter;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.ConversionService;

import otvosuzlet.javitasnyilntarto.dto.FullPersonDataRequestDto;
import otvosuzlet.javitasnyilntarto.model.FullJobGroupRequestDto;
import otvosuzlet.javitasnyilntarto.model.JobGroup;
import otvosuzlet.javitasnyilntarto.model.Person;
import otvosuzlet.javitasnyilntarto.testutil.TestObjectGenerator;

public class PersonToFullPersonDataRequestDtoConverterTest {

    @Test
    public void convertShouldMapPersonFieldsAndConvertJobGroups() {
        ConversionService conversionService = Mockito.mock(ConversionService.class);
        PersonToFullPersonDataRequestDtoConverter converter = new PersonToFullPersonDataRequestDtoConverter(conversionService);

        Person person = TestObjectGenerator.createPerson(11);
        person.setName("John");
        person.setAddress("Street 1");
        person.setPhone("123456");

        JobGroup group1 = TestObjectGenerator.createJobGroup(person, 201);
        JobGroup group2 = TestObjectGenerator.createJobGroup(person, 202);
        person.setJobGroups(Set.of(group1, group2));

        FullJobGroupRequestDto mapped1 = new FullJobGroupRequestDto();
        mapped1.setId(201);
        FullJobGroupRequestDto mapped2 = new FullJobGroupRequestDto();
        mapped2.setId(202);

        Mockito.when(conversionService.convert(group1, FullJobGroupRequestDto.class)).thenReturn(mapped1);
        Mockito.when(conversionService.convert(group2, FullJobGroupRequestDto.class)).thenReturn(mapped2);

        FullPersonDataRequestDto result = converter.convert(person);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(person.getId(), result.getId());
        Assertions.assertEquals(person.getName(), result.getName());
        Assertions.assertEquals(person.getAddress(), result.getAddress());
        Assertions.assertEquals(person.getPhone(), result.getPhone());
        Assertions.assertNotNull(result.getJobGroups());
        Assertions.assertEquals(2, result.getJobGroups().size());
        Assertions.assertTrue(result.getJobGroups().stream().anyMatch(group -> group.getId().equals(201)));
        Assertions.assertTrue(result.getJobGroups().stream().anyMatch(group -> group.getId().equals(202)));

        Mockito.verify(conversionService, Mockito.times(1)).convert(group1, FullJobGroupRequestDto.class);
        Mockito.verify(conversionService, Mockito.times(1)).convert(group2, FullJobGroupRequestDto.class);
    }
}