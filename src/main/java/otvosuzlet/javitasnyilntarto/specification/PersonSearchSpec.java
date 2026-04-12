package otvosuzlet.javitasnyilntarto.specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import otvosuzlet.javitasnyilntarto.dto.PersonSearchRequest;
import otvosuzlet.javitasnyilntarto.model.Person;

public class PersonSearchSpec {
    public static Specification<Person> withFilters(PersonSearchRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String nameTerm = (filter == null || filter.getName() == null) ? "" : filter.getName();
            String addressTerm = (filter == null || filter.getAddress() == null) ? "" : filter.getAddress();
            String phoneTerm = (filter == null || filter.getPhone() == null) ? "" : filter.getPhone();

            if (!nameTerm.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + nameTerm.toLowerCase() + "%"));
            }

            if (!addressTerm.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("address")), "%" + addressTerm.toLowerCase() + "%"));
            }

            if (!phoneTerm.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("phone")), "%" + phoneTerm.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
