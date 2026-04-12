package otvosuzlet.javitasnyilntarto.dto;

import java.time.LocalDateTime;
import java.util.List;

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
public class JobDto {
    @NotNull(message = "validation.required")
    private String objectname;
    @NotNull(message = "validation.required")
    private String description;
    @NotNull(message = "validation.required")
    private String material;
    @NotNull(message = "validation.required")
    private Integer pricemin;
    @Nullable
    private Integer pricemax;
    @NotNull(message = "validation.required")
    private Double weight;
    private LocalDateTime finishTime;
    private LocalDateTime pickedUpTime;
    private Integer finalPrice;
    private String uploadnote;
    private String finishnote;
    private List<Integer> imagesBefore;
    private List<Integer> imagesAfter;


}
