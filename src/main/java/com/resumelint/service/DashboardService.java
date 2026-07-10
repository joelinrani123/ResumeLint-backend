package com.resumelint.service;

import com.resumelint.dto.DashboardStatsDto;
import com.resumelint.dto.ScoreHistoryDto;
import com.resumelint.entity.Resume;
import com.resumelint.entity.User;
import com.resumelint.exception.ApiException;
import com.resumelint.repository.ResumeRepository;
import com.resumelint.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * Equivalent of {@code routes/dashboard.ts} {@code GET /dashboard/stats}.
 */
@Service
public class DashboardService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public DashboardService(ResumeRepository resumeRepository, UserRepository userRepository) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED.value(), "User not found"));

        List<Resume> resumes = resumeRepository.findByUser(user);

        List<Resume> completedResumes = resumes.stream()
                .filter(r -> "completed".equals(r.getStatus()))
                .toList();

        List<Integer> scores = completedResumes.stream()
                .map(r -> r.getOverallScore() != null ? r.getOverallScore() : 0)
                .filter(s -> s > 0)
                .toList();

        int totalResumes = resumes.size();
        int averageScore = scores.isEmpty()
                ? 0
                : Math.round((float) scores.stream().mapToInt(Integer::intValue).sum() / scores.size());
        int bestScore = scores.isEmpty() ? 0 : scores.stream().mapToInt(Integer::intValue).max().orElse(0);

        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        int recentAnalyses = (int) completedResumes.stream()
                .filter(r -> r.getUpdatedAt().isAfter(sevenDaysAgo))
                .count();

        List<ScoreHistoryDto> scoreHistory = completedResumes.stream()
                .filter(r -> r.getOverallScore() != null)
                .sorted(Comparator.comparing(Resume::getUpdatedAt))
                .skip(Math.max(0, completedResumes.stream()
                        .filter(r -> r.getOverallScore() != null).count() - 10))
                .map(r -> new ScoreHistoryDto(
                        IsoDates.toDateOnly(r.getUpdatedAt()),
                        r.getOverallScore(),
                        r.getFileName()))
                .toList();

        return new DashboardStatsDto(totalResumes, averageScore, bestScore, recentAnalyses, scoreHistory);
    }
}
