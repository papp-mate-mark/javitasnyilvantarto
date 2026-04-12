package otvosuzlet.javitasnyilntarto.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import otvosuzlet.javitasnyilntarto.dto.SystemSettingSearchRequest;
import otvosuzlet.javitasnyilntarto.model.SystemSetting;

public class SystemSettingSearchSpec {
    public static Specification<SystemSetting> withFilters(SystemSettingSearchRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String keyTerm = (filter == null || filter.getKey() == null) ? "" : filter.getKey();

            if (!keyTerm.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("key")), "%" + keyTerm.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
