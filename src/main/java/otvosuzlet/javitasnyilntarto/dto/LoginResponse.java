package otvosuzlet.javitasnyilntarto.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private TokensResponseDTO tokens;
    private String name;
}
