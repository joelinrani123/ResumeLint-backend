package com.resumelint.controller;

import com.resumelint.dto.*;
import com.resumelint.security.CurrentUser;
import com.resumelint.service.ResumeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Equivalent of {@code routes/resumes.ts}. Every method here is protected by
 * {@link com.resumelint.filter.AuthInterceptor} (registered in WebConfig for
 * {@code /api/resumes/**}), matching the Node router's
 * {@code router.use(requireAuth)}.
 */
@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;
    private final CurrentUser currentUser;

    public ResumeController(ResumeService resumeService, CurrentUser currentUser) {
        this.resumeService = resumeService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<ResumeDto> listResumes() {
        return resumeService.listResumes(currentUser.get().userId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeDto uploadResume(@Valid @RequestBody ResumeInputDto input) {
        return resumeService.createResume(currentUser.get().userId(), input);
    }

    @GetMapping("/{id}")
    public ResumeDto getResume(@PathVariable Long id) {
        return resumeService.getResume(currentUser.get().userId(), id);
    }

    @DeleteMapping("/{id}")
    public MessageResponseDto deleteResume(@PathVariable Long id) {
        return resumeService.deleteResume(currentUser.get().userId(), id);
    }

    @PostMapping("/{id}/analyze")
    public AnalysisDto analyzeResume(@PathVariable Long id,
                                      @RequestBody(required = false) AnalyzeInputDto input) {
        return resumeService.analyzeResume(currentUser.get().userId(), id, input);
    }

    @GetMapping("/{id}/analysis")
    public AnalysisDto getAnalysis(@PathVariable Long id) {
        return resumeService.getAnalysis(currentUser.get().userId(), id);
    }
}
