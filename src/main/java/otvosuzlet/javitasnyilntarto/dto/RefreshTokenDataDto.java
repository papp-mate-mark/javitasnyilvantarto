package otvosuzlet.javitasnyilntarto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenDataDto {
    private String userAgent;
    private String ipAddress;
    private Integer id;
}
