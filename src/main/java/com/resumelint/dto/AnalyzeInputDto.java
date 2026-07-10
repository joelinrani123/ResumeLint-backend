package com.resumelint.dto;

/** Equivalent of the Node AnalyzeResumeBody zod schema (AnalyzeInput). Both fields optional. */
public record AnalyzeInputDto(String jobDescription, String targetRole) {
}
