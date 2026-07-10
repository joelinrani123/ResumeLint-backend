package com.resumelint.entity;

import com.resumelint.dto.KeywordDto;
import com.resumelint.dto.ScoreBreakdownDto;
import com.resumelint.dto.SuggestionDto;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "analyses")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_analyses_resume"))
    private Resume resume;

    @Column(name = "overall_score", nullable = false)
    private Integer overallScore;

    @Column(name = "ats_compatibility", nullable = false)
    private Integer atsCompatibility;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Convert(converter = ScoresConverter.class)
    @Column(nullable = false, columnDefinition = "JSON")
    private List<ScoreBreakdownDto> scores;

    @Convert(converter = SuggestionsConverter.class)
    @Column(nullable = false, columnDefinition = "JSON")
    private List<SuggestionDto> suggestions;

    @Convert(converter = KeywordsConverter.class)
    @Column(nullable = false, columnDefinition = "JSON")
    private List<KeywordDto> keywords;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public Integer getAtsCompatibility() {
        return atsCompatibility;
    }

    public void setAtsCompatibility(Integer atsCompatibility) {
        this.atsCompatibility = atsCompatibility;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<ScoreBreakdownDto> getScores() {
        return scores;
    }

    public void setScores(List<ScoreBreakdownDto> scores) {
        this.scores = scores;
    }

    public List<SuggestionDto> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<SuggestionDto> suggestions) {
        this.suggestions = suggestions;
    }

    public List<KeywordDto> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<KeywordDto> keywords) {
        this.keywords = keywords;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
