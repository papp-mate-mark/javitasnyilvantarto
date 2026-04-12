package otvosuzlet.javitasnyilntarto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import otvosuzlet.javitasnyilntarto.model.SystemSetting;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, String>, JpaSpecificationExecutor<SystemSetting> {
}
