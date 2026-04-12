package otvosuzlet.javitasnyilntarto.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonRequest extends JobGroupDto {
    @NotNull(message = "validation.required")
    private String name;

    @NotNull(message = "validation.required")
    private String address;
    
    @Nullable
    private String phone;
}