package com.yourorg.aicode.model.dto;

public class ReviewResponse {
    private Long submissionId;
    private int score;
    private String comments;
    private String summary;
    private String[] issues;
    private String[] suggestions;
    private String[] bestPractices;
    private String fixCode;

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String[] getIssues() {
        return issues;
    }

    public void setIssues(String[] issues) {
        this.issues = issues;
    }

    public String[] getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String[] suggestions) {
        this.suggestions = suggestions;
    }

    public String[] getBestPractices() {
        return bestPractices;
    }

    public void setBestPractices(String[] bestPractices) {
        this.bestPractices = bestPractices;
    }

    public String getFixCode() {
        return fixCode;
    }

    public void setFixCode(String fixCode) {
        this.fixCode = fixCode;
    }
}
