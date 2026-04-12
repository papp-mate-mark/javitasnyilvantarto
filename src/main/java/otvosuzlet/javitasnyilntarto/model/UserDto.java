package otvosuzlet.javitasnyilntarto.model;

import java.util.Set;

import lombok.Data;
import otvosuzlet.javitasnyilntarto.enums.UserAuthorities;

@Data
public class UserDto {
    private final String username;
    private final String name;
    private final Integer id;
    private final Set<UserAuthorities> authorities;
}
