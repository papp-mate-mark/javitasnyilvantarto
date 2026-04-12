package otvosuzlet.javitasnyilntarto.specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import otvosuzlet.javitasnyilntarto.dto.JobSearchDto;
import otvosuzlet.javitasnyilntarto.model.Job;

public class JobSearchSpec {
    public static Specification<Job> withFilters(JobSearchDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Ügyfél adatok
            if (filter.getName() != null) {
                predicates.add(cb.like(root.get("jobGroup").get("person").get("name"), "%" + filter.getName() + "%"));
            }
            if (filter.getAddress() != null) {
                predicates.add(
                        cb.like(root.get("jobGroup").get("person").get("address"), "%" + filter.getAddress() + "%"));
            }
            if (filter.getPhone() != null) {
                predicates.add(cb.like(root.get("jobGroup").get("person").get("phone"), "%" + filter.getPhone() + "%"));
            }

            if (filter.getObjectname() != null) {
                predicates.add(cb.like(root.get("objectname"), "%" + filter.getObjectname() + "%"));
            }
            if (filter.getMaterial() != null) {
                predicates.add(cb.like(root.get("material"), "%" + filter.getMaterial() + "%"));
            }
            if (filter.getDescription() != null) {
                predicates.add(cb.like(root.get("description"), "%" + filter.getDescription() + "%"));
            }
            if(filter.getOnlywithphotos() != null && filter.getOnlywithphotos()) {
                predicates.add(cb.or(
                        cb.isNotEmpty(root.get("beforeImage")),
                        cb.isNotEmpty(root.get("afterImages"))));
            }
            // Numeric ranges (e.g., finalprice between min and max)
            if (filter.getFinalpricemin() != null || filter.getFinalpricemax() != null) {
                if (filter.getFinalpricemin() != null && filter.getFinalpricemax() != null) {
                    predicates.add(cb.between(root.get("finalprice"),
                            filter.getFinalpricemin(),
                            filter.getFinalpricemax()));
                } else if (filter.getFinalpricemin() != null) {
                    predicates.add(cb.ge(root.get("finalprice"), filter.getFinalpricemin()));
                } else {
                    predicates.add(cb.le(root.get("finalprice"), filter.getFinalpricemax()));
                }
            }

            // Weight range
            if (filter.getWeightmin() != null || filter.getWeightmax() != null) {
                if (filter.getWeightmin() != null && filter.getWeightmax() != null) {
                    predicates.add(cb.between(root.get("weight"),
                            filter.getWeightmin(),
                            filter.getWeightmax()));
                } else if (filter.getWeightmin() != null) {
                    predicates.add(cb.ge(root.get("weight"), filter.getWeightmin()));
                } else {
                    predicates.add(cb.le(root.get("weight"), filter.getWeightmax()));
                }
            }

            // Date ranges (e.g., uploadTime between uploadstart and uploadend)
            if (filter.getUploadstart() != null && filter.getUploadend() != null) {
                predicates.add(cb.between(root.get("jobGroup").get("bringedin"),
                        filter.getUploadstart(),
                        filter.getUploadend()));
            } else if (filter.getUploadstart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("jobGroup").get("bringedin"),
                        filter.getUploadstart()));
            } else if (filter.getUploadend() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("jobGroup").get("bringedin"),
                        filter.getUploadend()));
            }

            if (filter.getDeadlinestart() != null && filter.getDeadlineend() != null) {
                predicates.add(cb.between(root.get("jobGroup").get("deadline"),
                        filter.getDeadlinestart(),
                        filter.getDeadlineend()));
            } else if (filter.getDeadlinestart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("jobGroup").get("deadline"),
                        filter.getDeadlinestart()));
            } else if (filter.getDeadlineend() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("jobGroup").get("deadline"),
                        filter.getDeadlineend()));
            }

            if (filter.getDonestart() != null && filter.getDoneend() != null) {
                predicates.add(cb.between(root.get("done"),
                        filter.getDonestart(),
                        filter.getDoneend()));
            } else if (filter.getDonestart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("done"),
                        filter.getDonestart()));
            } else if (filter.getDoneend() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("done"),
                        filter.getDoneend()));
            }

            if (filter.getPickupstart() != null && filter.getPickupend() != null) {
                predicates.add(cb.between(root.get("pickup"),
                        filter.getPickupstart(),
                        filter.getPickupend()));
            } else if (filter.getPickupstart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pickup"),
                        filter.getPickupstart()));
            } else if (filter.getPickupend() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pickup"),
                        filter.getPickupend()));
            }
            if (filter.getDonenote() != null) {
                predicates.add(cb.like(root.get("finishnote"), "%" + filter.getDonenote() + "%"));
            }
            if (filter.getUploadnote() != null) {
                predicates.add(cb.like(root.get("uploadnote"), "%" + filter.getUploadnote() + "%"));
            }
            // Combine all predicates with AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
