package otvosuzlet.javitasnyilntarto.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class JobGroupUploadResponse {
    private final Integer groupId;
}
