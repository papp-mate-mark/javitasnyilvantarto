package otvosuzlet.javitasnyilntarto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokensResponseDTO {
    private String accessToken;
    private String refreshToken;
}
