package com.resumelint.dto;

public record ResumeDto(
        Long id,
        String fileName,
        String status,
        String jobTitle,
        Integer overallScore,
        String createdAt,
        String updatedAt
) {
}
