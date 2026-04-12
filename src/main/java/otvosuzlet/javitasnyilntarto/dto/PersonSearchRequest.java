package otvosuzlet.javitasnyilntarto.dto;

import lombok.Data;

@Data
public class PersonSearchRequest {
    private String name;
    private String address;
    private String phone;
}
