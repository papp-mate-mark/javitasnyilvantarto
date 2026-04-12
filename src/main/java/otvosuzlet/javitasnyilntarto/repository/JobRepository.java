package otvosuzlet.javitasnyilntarto.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import otvosuzlet.javitasnyilntarto.model.Job;
@Repository
public interface JobRepository extends JpaRepository<Job, Integer>, JpaSpecificationExecutor<Job> {

	@Query("select j.jobGroup.person.id from Job j where j.id = :jobId")
	Integer findPersonIdByJobId(@Param("jobId") Integer jobId);
}
