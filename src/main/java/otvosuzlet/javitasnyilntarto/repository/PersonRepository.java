package otvosuzlet.javitasnyilntarto.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import otvosuzlet.javitasnyilntarto.model.Person;
import otvosuzlet.javitasnyilntarto.projections.PersonFullInfoProjection;
import otvosuzlet.javitasnyilntarto.projections.PersonInfoProjection;

import java.util.Optional;
import java.util.Set;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer>, JpaSpecificationExecutor<Person> {
    Page<PersonInfoProjection> findByNameContainingIgnoreCaseAndAddressContainingIgnoreCase(String name, String address, Pageable pageable);
    Page<PersonInfoProjection> findByNameContainingIgnoreCaseAndAddressContainingIgnoreCaseAndPhoneContainingIgnoreCase(String name, String address, String phone, Pageable pageable);
    Long countAllByNameContainingIgnoreCaseAndAddressContainingIgnoreCaseAndPhoneContainingIgnoreCase(String name, String address, String phone);
    Long countAllByNameContainingIgnoreCaseAndAddressContainingIgnoreCase(String name, String address);
    <T> Optional<T> findById(Integer id, Class<T> type);
    @Query("SELECT DISTINCT p FROM Person p " +
            "JOIN FETCH p.jobGroups j " +
            "JOIN FETCH j.jobs job " +
            "WHERE job.pickup IS NULL " +
            "ORDER BY j.deadline ASC")
    Set<PersonFullInfoProjection> findPersonsWithActiveJobsProjection();
}
