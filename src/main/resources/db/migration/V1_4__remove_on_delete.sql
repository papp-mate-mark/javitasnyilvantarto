-- V1_4__remove_on_delete.sql
-- Recreate foreign key constraints without any ON DELETE actions
-- (this makes the DB use the default NO ACTION/RESTRICT behavior)

ALTER TABLE user_authorities DROP CONSTRAINT IF EXISTS user_authorities_user_id_fkey;
ALTER TABLE user_authorities ADD CONSTRAINT fk_user_authorities_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE refresh_tokens DROP CONSTRAINT IF EXISTS refresh_tokens_user_id_fkey;
ALTER TABLE refresh_tokens ADD CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE job_images DROP CONSTRAINT IF EXISTS job_images_job_id_fkey;
ALTER TABLE job_images ADD CONSTRAINT fk_job_images_job FOREIGN KEY (job_id) REFERENCES jobs(id);

ALTER TABLE jobs DROP CONSTRAINT IF EXISTS jobs_job_group_id_fkey;
ALTER TABLE jobs ADD CONSTRAINT fk_jobs_jobgroup FOREIGN KEY (job_group_id) REFERENCES jobgroups(id);

ALTER TABLE jobgroups DROP CONSTRAINT IF EXISTS jobgroups_person_id_fkey;
ALTER TABLE jobgroups ADD CONSTRAINT fk_jobgroups_person FOREIGN KEY (person_id) REFERENCES people(id);

ALTER TABLE jobgroups DROP CONSTRAINT IF EXISTS jobgroups_user_id_fkey;
ALTER TABLE jobgroups ADD CONSTRAINT fk_jobgroups_user FOREIGN KEY (user_id) REFERENCES users(id);

-- End of migration
