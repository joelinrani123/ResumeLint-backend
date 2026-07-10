package com.resumelint.service;

import com.resumelint.dto.KeywordDto;
import com.resumelint.dto.ScoreBreakdownDto;
import com.resumelint.dto.SuggestionDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Faithful Java port of {@code artifacts/api-server/src/lib/analyze.ts}.
 * Produces a simulated AI resume analysis using content heuristics — no
 * external AI call is involved, exactly as in the original implementation.
 *
 * <p>Every regex, weight, threshold and piece of copy below matches the
 * original source line for line so that the frontend receives byte-for-byte
 * equivalent results.
 */
@Service
public class AnalysisService {

    private static final String[] COMMON_KEYWORDS = {
            "leadership", "collaboration", "communication", "results-driven",
            "data-driven", "cross-functional", "stakeholder", "KPIs", "agile",
            "problem-solving", "strategic", "innovation", "scalable", "impact",
            "metrics", "optimization", "mentorship", "execution"
    };

    private static final Pattern[] IMPACT_PATTERNS = {
            Pattern.compile("\\d+%"),
            Pattern.compile("increased|improved|reduced|grew|saved|generated"),
            Pattern.compile("\\$\\d+"),
            Pattern.compile("\\d+[km]?\\+?\\s*(users|customers|revenue|sales)"),
            Pattern.compile("led|managed|owned|drove|delivered")
    };

    private static final Pattern[] FORMATTING_PATTERNS = {
            Pattern.compile("\\b(experience|education|skills|summary|objective)\\b"),
            Pattern.compile("\u2022|\u2013|-\\s"),
            Pattern.compile("^\\s*[A-Z]"),
            Pattern.compile("\\d{4}"),
            Pattern.compile("linkedin|github|portfolio")
    };

    private static final Pattern[] CONTENT_PATTERNS = {
            Pattern.compile("\\b(built|developed|designed|implemented|architected)\\b"),
            Pattern.compile("\\b(team|project|product|system|platform)\\b"),
            Pattern.compile("\\b(year|month|quarter)\\b"),
            Pattern.compile("role|position|title"),
            Pattern.compile("responsible|achieve")
    };

    private static final Pattern[] KEYWORDS_PATTERNS = {
            Pattern.compile("agile|scrum|kanban"),
            Pattern.compile("cross-functional|cross functional"),
            Pattern.compile("data-driven|metrics|kpis"),
            Pattern.compile("leadership|mentored|coached"),
            Pattern.compile("strategic|strategy"),
            Pattern.compile("stakeholder"),
            Pattern.compile("collaboration|collaborated")
    };

    private static final Pattern[] LENGTH_PATTERNS = {
            Pattern.compile("\\w{100,}"),
            Pattern.compile("(?s).{500,}"),
            Pattern.compile("\\n.*\\n.*\\n")
    };

    private static final Pattern LINKEDIN_PATTERN = Pattern.compile("linkedin\\.com");
    private static final Pattern SUMMARY_PATTERN = Pattern.compile("summary|objective|profile");
    private static final Pattern SKILLS_PATTERN = Pattern.compile("skill|technical|proficiency|tools",
            Pattern.CASE_INSENSITIVE);

    public record AnalysisResult(
            int overallScore,
            int atsCompatibility,
            String summary,
            List<ScoreBreakdownDto> scores,
            List<SuggestionDto> suggestions,
            List<KeywordDto> keywords
    ) {
    }

    private static int scoreSection(String content, Pattern[] patterns) {
        int hits = 0;
        for (Pattern p : patterns) {
            if (p.matcher(content).find()) {
                hits++;
            }
        }
        return Math.min(100, 40 + Math.round((hits / (float) patterns.length) * 60));
    }

    /** Equivalent of JS {@code (hash * 31 + charCode) | 0} using 32-bit int wraparound. */
    private static int pseudoRandom(String seed, int min, int max) {
        int hash = 0;
        for (int i = 0; i < seed.length(); i++) {
            hash = hash * 31 + seed.charAt(i);
        }
        long absHash = Math.abs((long) hash);
        return (int) (min + absHash % (max - min + 1));
    }

    public AnalysisResult analyzeResume(String content, String targetRole) {
        String lower = content.toLowerCase();

        int impactScore = scoreSection(lower, IMPACT_PATTERNS);
        int formattingScore = scoreSection(lower, FORMATTING_PATTERNS);
        int contentScore = scoreSection(lower, CONTENT_PATTERNS);
        int keywordsScore = scoreSection(lower, KEYWORDS_PATTERNS);
        int lengthScore = scoreSection(lower, LENGTH_PATTERNS);

        int overallScore = Math.round(
                impactScore * 0.35f +
                        contentScore * 0.25f +
                        formattingScore * 0.15f +
                        keywordsScore * 0.15f +
                        lengthScore * 0.10f
        );

        int atsCompatibility = Math.min(100, Math.round(
                formattingScore * 0.4f + keywordsScore * 0.35f + contentScore * 0.25f
        ));

        List<ScoreBreakdownDto> scores = new ArrayList<>();
        scores.add(new ScoreBreakdownDto(
                "Impact & Quantification",
                impactScore,
                impactScore >= 70
                        ? "Strong use of measurable achievements and outcomes."
                        : "Add more quantified results — numbers, percentages, and dollar amounts make achievements credible."
        ));
        scores.add(new ScoreBreakdownDto(
                "Content & Relevance",
                contentScore,
                contentScore >= 70
                        ? "Content demonstrates clear ownership and project scope."
                        : "Include more action verbs and specify your direct contributions vs. team contributions."
        ));
        scores.add(new ScoreBreakdownDto(
                "Formatting & Readability",
                formattingScore,
                formattingScore >= 70
                        ? "Resume structure is clean and easy to scan."
                        : "Ensure consistent formatting, clear section headers, and bullet-point structure throughout."
        ));
        scores.add(new ScoreBreakdownDto(
                "Keywords & Industry Terms",
                keywordsScore,
                keywordsScore >= 70
                        ? "Good use of relevant industry terminology."
                        : "Incorporate more industry-standard keywords, especially those from the target job description."
        ));
        scores.add(new ScoreBreakdownDto(
                "Length & Completeness",
                lengthScore,
                lengthScore >= 70
                        ? "Resume length and coverage appear appropriate."
                        : "Expand key sections with more detail — especially roles held less than 2 years."
        ));

        List<SuggestionDto> suggestions = new ArrayList<>();

        if (impactScore < 70) {
            suggestions.add(new SuggestionDto(
                    "improvement", "high",
                    "Add quantified achievements to at least 80% of your bullet points. Use 'increased X by Y%' or 'saved $Z in costs' format.",
                    "Experience"));
        } else {
            suggestions.add(new SuggestionDto(
                    "success", "low",
                    "Excellent use of metrics and quantified outcomes throughout your experience section.",
                    "Experience"));
        }

        if (!LINKEDIN_PATTERN.matcher(lower).find()) {
            suggestions.add(new SuggestionDto(
                    "warning", "high",
                    "LinkedIn profile URL is missing from your contact section. Recruiters expect to verify your profile.",
                    "Contact"));
        }

        if (!SUMMARY_PATTERN.matcher(lower).find()) {
            suggestions.add(new SuggestionDto(
                    "improvement", "high",
                    "Add a professional summary at the top — a 2-3 sentence statement tailored to your target role dramatically improves recruiter engagement.",
                    "Summary"));
        } else {
            suggestions.add(new SuggestionDto(
                    "success", "low",
                    "Professional summary is present and positions you effectively for your target role.",
                    "Summary"));
        }

        if (keywordsScore < 60) {
            suggestions.add(new SuggestionDto(
                    "improvement", "medium",
                    "Mirror the language from target job descriptions. Use exact phrases like 'cross-functional collaboration' and 'data-driven decision making'.",
                    "Keywords"));
        }

        if (atsCompatibility < 70) {
            suggestions.add(new SuggestionDto(
                    "warning", "high",
                    "ATS compatibility is below threshold. Avoid tables, graphics, and non-standard fonts. Use simple bullet points and standard section headings.",
                    "Formatting"));
        }

        suggestions.add(new SuggestionDto(
                "improvement", "medium",
                (targetRole != null && !targetRole.isBlank())
                        ? "For the '" + targetRole + "' role, emphasize your most relevant technical skills and leadership scope in the first half of the page."
                        : "Tailor your resume to each specific job by rearranging bullet points to match the job description's priorities.",
                "Targeting"));

        if (!SKILLS_PATTERN.matcher(content).find()) {
            suggestions.add(new SuggestionDto(
                    "improvement", "medium",
                    "Add a dedicated Skills section listing tools, technologies, and methodologies. This is critical for ATS keyword matching.",
                    "Skills"));
        }

        String seed = content.length() > 50 ? content.substring(0, 50) : content;
        List<KeywordDto> keywords = new ArrayList<>();
        for (String word : COMMON_KEYWORDS) {
            boolean found = lower.contains(word.toLowerCase())
                    || pseudoRandom(word + seed, 0, 10) > 5;
            keywords.add(new KeywordDto(word, found));
        }

        long foundCount = keywords.stream().filter(KeywordDto::found).count();
        String summary = "Your resume scores " + overallScore + "/100 overall with an ATS compatibility rating of "
                + atsCompatibility + "%. "
                + "You are matching " + foundCount + " of " + keywords.size() + " key industry terms. "
                + (overallScore >= 75
                ? "This is a strong resume. Focus on keyword alignment and ATS optimization to maximize recruiter response rates."
                : overallScore >= 55
                ? "Your resume has a solid foundation. Prioritize adding quantified achievements and an optimized professional summary."
                : "Significant improvements are needed before submitting. Start with quantifying your achievements and improving keyword coverage.");

        return new AnalysisResult(overallScore, atsCompatibility, summary, scores, suggestions, keywords);
    }
}
