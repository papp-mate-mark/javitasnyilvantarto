package otvosuzlet.javitasnyilntarto.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private String name;
    private String address;
    private String phone;
    private List<JobGroupTransferRequest> jobGroups;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobGroupTransferRequest {
        private List<JobTransferRequest> jobs;
        private LocalDateTime bringedin;
        private LocalDateTime deadline;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobTransferRequest {
        private String description;
        private String objectname;
        private String material;
        private Double weight;
        private Integer pricemin;
        private Integer pricemax;
        private Integer finalprice;
        private LocalDateTime done;
        private LocalDateTime pickup;
        private String uploadnote;
        private String finishnote;
        private List<ImageTransferRequest> beforeImage;
        private List<ImageTransferRequest> afterImages;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageTransferRequest {
        private String image;
        private String fullContentType;
        private String createTime;
    }
}
