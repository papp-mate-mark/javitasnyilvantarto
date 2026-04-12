package otvosuzlet.javitasnyilntarto.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import otvosuzlet.javitasnyilntarto.enums.UserAuthorities;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {
    @NotBlank(message = "validation.required")
    private String username;
    private String name;
    private Set<UserAuthorities> authorities;
}
