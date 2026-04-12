package otvosuzlet.javitasnyilntarto.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import otvosuzlet.javitasnyilntarto.model.JobImage;
@Repository
public interface JobImageRepository extends JpaRepository<JobImage, Integer>{
    <T> Optional<T> findById(Integer id, Class<T> type);

    @Query("select img.job.jobGroup.person.id from JobImage img where img.id = :imageId")
    Integer findPersonIdByImageId(@Param("imageId") Integer imageId);
}
