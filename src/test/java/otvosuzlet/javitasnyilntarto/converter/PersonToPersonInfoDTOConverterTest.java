package otvosuzlet.javitasnyilntarto.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import otvosuzlet.javitasnyilntarto.dto.PersonInfoDTO;
import otvosuzlet.javitasnyilntarto.model.Person;
import otvosuzlet.javitasnyilntarto.testutil.TestObjectGenerator;

public class PersonToPersonInfoDTOConverterTest {

    @Test
    public void convertShouldReturnNullForNullSource() {
        PersonToPersonInfoDTOConverter converter = new PersonToPersonInfoDTOConverter();

        PersonInfoDTO result = converter.convert(null);

        Assertions.assertNull(result);
    }

    @Test
    public void convertShouldMapAllFields() {
        PersonToPersonInfoDTOConverter converter = new PersonToPersonInfoDTOConverter();
        Person person = TestObjectGenerator.createPerson(55);

        PersonInfoDTO result = converter.convert(person);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(person.getId(), result.getId());
        Assertions.assertEquals(person.getName(), result.getName());
        Assertions.assertEquals(person.getAddress(), result.getAddress());
        Assertions.assertEquals(person.getPhone(), result.getPhone());
    }
}