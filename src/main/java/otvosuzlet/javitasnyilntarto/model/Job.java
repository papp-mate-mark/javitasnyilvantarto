package otvosuzlet.javitasnyilntarto.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "jobs")
@EntityListeners(AuditingEntityListener.class)
@Data
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "objectname", nullable = false, columnDefinition = "TEXT")
    private String objectname;

    @Column(name = "material", nullable = false, columnDefinition = "TEXT")
    private String material;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "pricemin", nullable = false)
    private Integer pricemin;

    @Column(name = "pricemax", nullable = true)
    private Integer pricemax;
    @Column(name = "finalprice", nullable = true)
    private Integer finalprice;
    @Column(name = "done", nullable = true)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime done;

    @Column(name = "pickup", nullable = true)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime pickup;

    @Column(name = "uploadnote", nullable = true)
    private String uploadnote;
    @Column(name = "finishnote", nullable = true)
    private String finishnote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_group_id")
    @JsonBackReference
    private JobGroup jobGroup;

    @SQLRestriction("type = 'BEFORE'") // Modern Hibernate annotation
    @JsonManagedReference
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<JobImage> beforeImage = new HashSet<>();

    @SQLRestriction("type = 'AFTER'") // Modern Hibernate annotation
    @JsonManagedReference
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<JobImage> afterImages = new HashSet<>();


}
