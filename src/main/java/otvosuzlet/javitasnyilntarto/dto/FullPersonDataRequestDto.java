package otvosuzlet.javitasnyilntarto.dto;

import java.util.List;

import lombok.Data;
import otvosuzlet.javitasnyilntarto.model.FullJobGroupRequestDto;

@Data
public class FullPersonDataRequestDto {
    Integer id;
    String name;
    String phone;
    String address;
    List<FullJobGroupRequestDto> jobGroups; 
}
