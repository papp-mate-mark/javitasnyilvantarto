package otvosuzlet.javitasnyilntarto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "system_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemSetting {

    @Id
    @Column(name = "setting_key", nullable = false)
    private String key;

    @Column(name = "description_key", nullable = true)
    private String descriptionKey;

    @Column(name = "setting_value", nullable = true, columnDefinition = "TEXT")
    private String value;
}
