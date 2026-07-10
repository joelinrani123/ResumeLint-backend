package com.resumelint.dto;

import java.util.List;

public record DashboardStatsDto(
        int totalResumes,
        int averageScore,
        int bestScore,
        int recentAnalyses,
        List<ScoreHistoryDto> scoreHistory
) {
}
