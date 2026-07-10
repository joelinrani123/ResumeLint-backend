package com.resumelint.repository;

import com.resumelint.entity.Resume;
import com.resumelint.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUser(User user);
    Optional<Resume> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);
}
