package otvosuzlet.javitasnyilntarto.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import otvosuzlet.javitasnyilntarto.model.User;
import otvosuzlet.javitasnyilntarto.model.UserDto;

@Component
public class UserToUserDtoConverter implements Converter<User, UserDto> {

    @Override
    public UserDto convert(User source) {
        return new UserDto(source.getUsername(), source.getName(), source.getId(), source.getAuthorities());
    }
}