package otvosuzlet.javitasnyilntarto.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class FullJobRequestDto {
    private Integer id;
    private String description;
    private Integer finalprice;
    private String objectname;
    private String material;
    private Double weight;
    private Integer pricemin;
    private Integer pricemax;
    private String uploadnote;
    private LocalDateTime done;
    private String finishnote;
    private LocalDateTime pickup;
    private List<Integer> beforeImages;
    private List<Integer> afterImages;
}
