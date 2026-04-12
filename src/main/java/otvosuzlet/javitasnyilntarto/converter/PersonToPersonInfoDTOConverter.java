package otvosuzlet.javitasnyilntarto.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import otvosuzlet.javitasnyilntarto.dto.PersonInfoDTO;
import otvosuzlet.javitasnyilntarto.model.Person;

@Component
public class PersonToPersonInfoDTOConverter implements Converter<Person, PersonInfoDTO> {

    @Override
    public PersonInfoDTO convert(Person source) {
        if (source == null) {
            return null;
        }

        PersonInfoDTO dto = new PersonInfoDTO();
        dto.setId(source.getId());
        dto.setName(source.getName());
        dto.setAddress(source.getAddress());
        dto.setPhone(source.getPhone());
        
        return dto;
    }
}
