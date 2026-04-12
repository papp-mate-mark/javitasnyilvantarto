package otvosuzlet.javitasnyilntarto.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class JobSearchDto {
    private String name;
    private String address;
    private String phone;
    private String objectname;
    private String description;
    private String material;
    private Integer finalpricemin;
    private Integer finalpricemax;
    private Float weightmin;
    private Float weightmax;
    private LocalDateTime uploadstart;
    private LocalDateTime uploadend;
    private LocalDateTime donestart;
    private LocalDateTime doneend;
    private LocalDateTime pickupstart;
    private LocalDateTime pickupend;
    private LocalDateTime deadlinestart;
    private LocalDateTime deadlineend;
    private String donenote;
    private String uploadnote;
    private Boolean onlywithphotos;

}
