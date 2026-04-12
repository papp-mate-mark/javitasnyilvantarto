package otvosuzlet.javitasnyilntarto.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import otvosuzlet.javitasnyilntarto.projections.PersonInfoProjection;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonInfoProjectionSearchResponseWithCount {
    private Long count;
    private List<PersonInfoProjection> people;
}
