package com.resumelint.dto;

import java.util.List;

public record AnalysisDto(
        Long id,
        Long resumeId,
        Integer overallScore,
        Integer atsCompatibility,
        List<ScoreBreakdownDto> scores,
        List<SuggestionDto> suggestions,
        List<KeywordDto> keywords,
        String summary,
        String createdAt
) {
}
