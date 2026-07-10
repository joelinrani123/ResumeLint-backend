package com.resumelint.service;

import com.resumelint.dto.*;
import com.resumelint.entity.Analysis;
import com.resumelint.entity.Resume;
import com.resumelint.entity.User;
import com.resumelint.exception.ApiException;
import com.resumelint.repository.AnalysisRepository;
import com.resumelint.repository.ResumeRepository;
import com.resumelint.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Equivalent of {@code routes/resumes.ts}: list, create, fetch, delete, and
 * analyze resumes, scoped to the authenticated user exactly as the Node
 * handlers scope every query by {@code req.auth.userId}.
 */
@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final AnalysisService analysisService;

    public ResumeService(ResumeRepository resumeRepository,
                          AnalysisRepository analysisRepository,
                          UserRepository userRepository,
                          AnalysisService analysisService) {
        this.resumeRepository = resumeRepository;
        this.analysisRepository = analysisRepository;
        this.userRepository = userRepository;
        this.analysisService = analysisService;
    }

    @Transactional(readOnly = true)
    public List<ResumeDto> listResumes(Long userId) {
        User user = requireUser(userId);
        return resumeRepository.findByUser(user).stream().map(this::toDto).toList();
    }

    @Transactional
    public ResumeDto createResume(Long userId, ResumeInputDto input) {
        User user = requireUser(userId);

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setFileName(input.fileName());
        resume.setContent(input.content());
        resume.setJobTitle(input.jobTitle());
        resume.setStatus("pending");
        resume = resumeRepository.save(resume);

        return toDto(resume);
    }

    @Transactional(readOnly = true)
    public ResumeDto getResume(Long userId, Long resumeId) {
        Resume resume = requireOwnedResume(userId, resumeId);
        return toDto(resume);
    }

    @Transactional
    public MessageResponseDto deleteResume(Long userId, Long resumeId) {
        Resume resume = requireOwnedResume(userId, resumeId);
        resumeRepository.delete(resume);
        return new MessageResponseDto("Resume deleted");
    }

    @Transactional
    public AnalysisDto analyzeResume(Long userId, Long resumeId, AnalyzeInputDto input) {
        Resume resume = requireOwnedResume(userId, resumeId);

        resume.setStatus("analyzing");
        resumeRepository.save(resume);

        String targetRole = (input != null && input.targetRole() != null && !input.targetRole().isBlank())
                ? input.targetRole()
                : resume.getJobTitle();

        AnalysisService.AnalysisResult result = analysisService.analyzeResume(resume.getContent(), targetRole);

        Analysis analysis = new Analysis();
        analysis.setResume(resume);
        analysis.setOverallScore(result.overallScore());
        analysis.setAtsCompatibility(result.atsCompatibility());
        analysis.setSummary(result.summary());
        analysis.setScores(result.scores());
        analysis.setSuggestions(result.suggestions());
        analysis.setKeywords(result.keywords());
        analysis = analysisRepository.save(analysis);

        resume.setStatus("completed");
        resume.setOverallScore(result.overallScore());
        resumeRepository.save(resume);

        return toDto(analysis);
    }

    @Transactional(readOnly = true)
    public AnalysisDto getAnalysis(Long userId, Long resumeId) {
        Resume resume = requireOwnedResume(userId, resumeId);

        Analysis analysis = analysisRepository.findFirstByResumeOrderByCreatedAtAsc(resume)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "Analysis not found"));

        return toDto(analysis);
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED.value(), "User not found"));
    }

    private Resume requireOwnedResume(Long userId, Long resumeId) {
        User user = requireUser(userId);
        return resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "Resume not found"));
    }

    private ResumeDto toDto(Resume resume) {
        return new ResumeDto(
                resume.getId(),
                resume.getFileName(),
                resume.getStatus(),
                resume.getJobTitle(),
                resume.getOverallScore(),
                IsoDates.toIso(resume.getCreatedAt()),
                IsoDates.toIso(resume.getUpdatedAt())
        );
    }

    private AnalysisDto toDto(Analysis analysis) {
        return new AnalysisDto(
                analysis.getId(),
                analysis.getResume().getId(),
                analysis.getOverallScore(),
                analysis.getAtsCompatibility(),
                analysis.getScores(),
                analysis.getSuggestions(),
                analysis.getKeywords(),
                analysis.getSummary(),
                IsoDates.toIso(analysis.getCreatedAt())
        );
    }
}
