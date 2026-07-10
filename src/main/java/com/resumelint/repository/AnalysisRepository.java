package com.resumelint.repository;

import com.resumelint.entity.Analysis;
import com.resumelint.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    /**
     * Mirrors the Node query exactly:
     * {@code .orderBy(analysesTable.createdAt).limit(1)} — ascending order
     * with no explicit direction returns the OLDEST analysis for the resume,
     * not the newest. This is reproduced faithfully for parity with the
     * original backend's observed behavior.
     */
    Optional<Analysis> findFirstByResumeOrderByCreatedAtAsc(Resume resume);
}
