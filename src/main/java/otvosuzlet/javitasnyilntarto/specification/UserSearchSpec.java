package otvosuzlet.javitasnyilntarto.specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import otvosuzlet.javitasnyilntarto.dto.UserSearchRequest;
import otvosuzlet.javitasnyilntarto.model.User;

public class UserSearchSpec {
    public static Specification<User> withFilters(UserSearchRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String usernameTerm = (filter == null || filter.getUsername() == null) ? "" : filter.getUsername();
            String nameTerm = (filter == null || filter.getName() == null) ? "" : filter.getName();

            if (!usernameTerm.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + usernameTerm.toLowerCase() + "%"));
            }

            if (!nameTerm.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + nameTerm.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
