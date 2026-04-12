package otvosuzlet.javitasnyilntarto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import otvosuzlet.javitasnyilntarto.model.JobGroup;
@Repository
public interface JobGroupRepository extends JpaRepository<JobGroup, Integer> {

	@Query("select jg.person.id from JobGroup jg where jg.id = :jobGroupId")
	Integer findPersonIdByJobGroupId(@Param("jobGroupId") Integer jobGroupId);
}
