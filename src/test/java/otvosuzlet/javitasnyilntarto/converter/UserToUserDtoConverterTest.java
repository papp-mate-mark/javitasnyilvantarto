package otvosuzlet.javitasnyilntarto.converter;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import otvosuzlet.javitasnyilntarto.enums.UserAuthorities;
import otvosuzlet.javitasnyilntarto.model.User;
import otvosuzlet.javitasnyilntarto.model.UserDto;

public class UserToUserDtoConverterTest {

    @Test
    public void convertShouldMapAllFields() {
        UserToUserDtoConverter converter = new UserToUserDtoConverter();
        User user = new User();
        user.setId(42);
        user.setUsername("admin");
        user.setName("Admin User");
        user.setAuthorities(Set.of(UserAuthorities.LIST_USERS, UserAuthorities.MODIFY_USERS));

        UserDto result = converter.convert(user);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.getId());
        Assertions.assertEquals(user.getUsername(), result.getUsername());
        Assertions.assertEquals(user.getName(), result.getName());
        Assertions.assertEquals(user.getAuthorities(), result.getAuthorities());
    }
}