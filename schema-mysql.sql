-- ResumeLint — reference MySQL schema
--
-- This file is for documentation / manual setup only. By default the
-- Spring Boot app creates and updates these tables automatically via
-- `spring.jpa.hibernate.ddl-auto=update` (see application.properties).
-- Run this manually only if you prefer to manage the schema yourself
-- (and then switch ddl-auto to `validate` or `none`).

CREATE DATABASE IF NOT EXISTS resumelint
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE resumelint;

CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS resumes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    content         LONGTEXT NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'pending',
    job_title       VARCHAR(255) NULL,
    overall_score   INT NULL,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_resumes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS analyses (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id           BIGINT NOT NULL,
    overall_score       INT NOT NULL,
    ats_compatibility   INT NOT NULL,
    summary             TEXT NOT NULL,
    scores              JSON NOT NULL,
    suggestions         JSON NOT NULL,
    keywords            JSON NOT NULL,
    created_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_analyses_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_resumes_user_id ON resumes(user_id);
CREATE INDEX idx_analyses_resume_id ON analyses(resume_id);
