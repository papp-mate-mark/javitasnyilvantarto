package otvosuzlet.javitasnyilntarto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetOwnPasswordRequest {

    @NotBlank(message = "validation.required")
    @Size(min = 8, message = "validation.password.too.short")
    private String newPassword;

    private String refreshToken;

}
