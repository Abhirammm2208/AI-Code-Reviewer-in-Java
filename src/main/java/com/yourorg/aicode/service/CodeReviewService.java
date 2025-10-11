// package com.yourorg.aicode.service;

// import com.yourorg.aicode.model.dto.ReviewRequest;
// import com.yourorg.aicode.model.dto.ReviewResponse;
// import com.yourorg.aicode.model.CodeSubmission;
// import com.yourorg.aicode.repository.CodeSubmissionRepository;
// import org.springframework.stereotype.Service;

// import java.time.Instant;

// @Service
// public class CodeReviewService {

//     private final CodeSubmissionRepository repository;

//     public CodeReviewService(CodeSubmissionRepository repository) {
//         this.repository = repository;
//     }

//     public ReviewResponse review(ReviewRequest request) {
//         // Basic placeholder logic: persist submission and return a simple review
//         CodeSubmission submission = new CodeSubmission();
//         submission.setAuthor(request.getAuthor());
//         submission.setCode(request.getCode());
//         submission.setCreatedAt(Instant.now());

//         CodeSubmission saved = repository.save(submission);

//         ReviewResponse resp = new ReviewResponse();
//         resp.setSubmissionId(saved.getId());
//         resp.setScore(0);
//         resp.setComments("Auto-review not implemented yet.");
//         return resp;
//     }
// }


package com.yourorg.aicode.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourorg.aicode.model.CodeSubmission;
import com.yourorg.aicode.model.dto.ReviewRequest;
import com.yourorg.aicode.model.dto.ReviewResponse;
import com.yourorg.aicode.repository.CodeSubmissionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class CodeReviewService {

    private final CodeSubmissionRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${ai.provider:gemini}")
    private String aiProvider;

    // Gemini / Generative API configuration
    @Value("${generative.api-key:}")
    private String generativeApiKey;
    
    @Value("${generative.model:gemini-2.0-flash-exp}")
    private String generativeModel;

    public CodeReviewService(CodeSubmissionRepository repository) {
        this.repository = repository;
    }

    /**
     * Review submitted code. If an AI integration is available it can be plugged later.
     * For now use a simple heuristic: score by length and presence of TODO/print stack traces.
     */
    public ReviewResponse review(ReviewRequest request) {
        CodeSubmission submission = new CodeSubmission();
        submission.setAuthor(request.getAuthor());
        submission.setCode(request.getCode());
        submission.setLanguage(request.getLanguage());
        submission.setCreatedAt(Instant.now());

        String code = request.getCode() == null ? "" : request.getCode();

        // Provider selection: gemini preferred, openai as fallback
        if (aiProvider != null && aiProvider.equalsIgnoreCase("gemini") && generativeApiKey != null && !generativeApiKey.isBlank()) {
            try {
                ReviewResponse aiResponse = callGeminiForReview(code);
                String raw = objectMapper.writeValueAsString(aiResponse);
                submission.setAiFeedback(raw);
                submission.setScore(aiResponse.getScore());
                CodeSubmission saved = repository.save(submission);

                aiResponse.setSubmissionId(saved.getId());
                return aiResponse;
            } catch (Exception e) {
                System.err.println("Gemini call failed, falling back: " + e.getMessage());
            }
        }

        // No OpenAI support: only Gemini/generative API is used. If generative key not available, fall back to heuristic.

        // --- Fallback heuristic (keeps previous behavior) ---
        int lengthScore = Math.min(50, code.length() / 10);
        int penalty = 0;
        List<String> issues = new ArrayList<>();
        if (code.contains("TODO")) {
            penalty += 10;
            issues.add("Contains TODO marks");
        }
        if (code.contains("System.out.println") || code.contains("print(")) {
            penalty += 5;
            issues.add("Contains printing statements (remove or use logger)");
        }

        int score = Math.max(0, 100 - (50 - lengthScore) - penalty);

        ReviewResponse resp = new ReviewResponse();
        String aiFeedback = "score=" + score + "; issues=" + issues.toString();
        submission.setAiFeedback(aiFeedback);
        submission.setScore(score);

        CodeSubmission saved = repository.save(submission);

        resp.setSubmissionId(saved.getId());
        resp.setScore(score);
        resp.setComments("Basic heuristic review (fallback)");
        resp.setSummary("Length-based heuristic review");
        resp.setIssues(issues.toArray(new String[0]));
        resp.setSuggestions(new String[]{"Remove TODOs before finalizing", "Replace prints with proper logging"});
        resp.setBestPractices(new String[]{"Write unit tests", "Follow single responsibility principle"});

        if (code.contains("System.out.println")) {
            resp.setFixCode(code.replace("System.out.println", "logger.info"));
        }

        return resp;
    }

    private ReviewResponse callGeminiForReview(String code) throws IOException {
        // Build a prompt instructing the model to return strict JSON
        String prompt = "You are an expert senior software engineer who reviews code. Respond strictly with JSON (no surrounding markdown) that matches the schema: {\"score\": int 0-100, \"summary\": string, \"comments\": string, \"issues\": [string], \"suggestions\": [string], \"bestPractices\": [string], \"fixCode\": string|null }.\n\nCode:\n" + code + "\n\nBe concise but thorough.";

        try {
            // Build request body for Google Generative Language API
            JsonNode requestBody = objectMapper.createObjectNode()
                .set("contents", objectMapper.createArrayNode()
                    .add(objectMapper.createObjectNode()
                        .set("parts", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode()
                                .put("text", prompt)))));

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // Call Generative Language API with API key
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + generativeModel + ":generateContent?key=" + generativeApiKey;

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                throw new IOException("Gemini API error: " + response.statusCode() + " -> " + response.body());
            }

            // Parse response
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode candidates = root.path("candidates");
            
            if (!candidates.isArray() || candidates.size() == 0) {
                throw new IOException("No candidates in Gemini response: " + response.body());
            }

            String content = candidates.get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

            // Strip markdown code fences if present
            content = content.trim();
            if (content.startsWith("```")) {
                int idx = content.indexOf('\n');
                if (idx > 0) content = content.substring(idx + 1);
                if (content.endsWith("```")) content = content.substring(0, content.length() - 3).trim();
            }

            // Parse JSON into ReviewResponse
            ReviewResponse rr = objectMapper.readerFor(ReviewResponse.class).readValue(content);
            
            // Ensure score bounds
            int s = rr.getScore();
            if (s < 0) rr.setScore(0);
            if (s > 100) rr.setScore(100);

            return rr;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("Gemini API call failed: " + e.getMessage(), e);
        }
    }

    

}
