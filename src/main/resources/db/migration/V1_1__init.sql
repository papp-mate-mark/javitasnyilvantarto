CREATE TABLE people (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(255)
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255)
);

CREATE TYPE user_authority AS ENUM ('LIST_USERS', 'MODIFY_USERS', 'CREATE_JOBS', 'MODIFY_JOBS', 'MODIFY_SYSTEM_SETTINGS');

CREATE TABLE user_authorities (
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    authority user_authority NOT NULL,
    CONSTRAINT pk_user_authorities PRIMARY KEY (user_id, authority)
);

CREATE TYPE image_type AS ENUM ('BEFORE', 'AFTER');

CREATE TABLE jobgroups (
    id SERIAL PRIMARY KEY,
    person_id INTEGER REFERENCES people(id) ON DELETE SET NULL,
    user_id INTEGER NULL REFERENCES users(id) ON DELETE SET NULL,
    bringedin TIMESTAMP NOT NULL,
    deadline TIMESTAMP NOT NULL
);

CREATE TABLE jobs (
    id SERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    objectname TEXT NOT NULL,
    material TEXT NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    pricemin INTEGER NOT NULL,
    pricemax INTEGER,
    finalprice INTEGER,
    done TIMESTAMP,
    pickup TIMESTAMP,
    uploadnote TEXT,
    finishnote TEXT,
    job_group_id INTEGER REFERENCES jobgroups(id) ON DELETE SET NULL
);

CREATE TABLE job_images (
    id SERIAL PRIMARY KEY,
    thumbnail_filename VARCHAR(255),
    image_filename VARCHAR(255) NOT NULL,
    created_date TIMESTAMP,
    type image_type,
    job_id INTEGER REFERENCES jobs(id) ON DELETE CASCADE
);

CREATE INDEX idx_jobimages_jobid ON job_images(job_id);
CREATE INDEX idx_jobs_jobgroup ON jobs(job_group_id);
CREATE INDEX idx_jobgroups_person ON jobgroups(person_id);
CREATE INDEX idx_jobgroups_user ON jobgroups(user_id);

CREATE TABLE refresh_tokens (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
