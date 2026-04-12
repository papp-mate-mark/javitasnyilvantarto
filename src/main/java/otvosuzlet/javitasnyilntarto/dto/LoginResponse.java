package otvosuzlet.javitasnyilntarto.dto;

import java.util.Set;

import lombok.Data;
import otvosuzlet.javitasnyilntarto.enums.UserAuthorities;

@Data
public class LoginResponse {
    private Set<UserAuthorities> authorities;
    private TokensResponseDTO tokens;
    private String name;
}
