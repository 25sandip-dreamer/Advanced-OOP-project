package com.healthcare.model;

import java.time.LocalDateTime;

/**
 * AssessmentResponse Model
 * Stores patient answers to assessment questions
 */
public class AssessmentResponse {

    private int responseId;
    private int monitoringId;
    private String questionText;
    private String responseText;
    private Integer responseScore; // For numeric ratings (1-10)
    private String sentiment; // Positive, Neutral, Negative (AI-determined)
    private LocalDateTime responseTimestamp;

    // Question types
    public enum QuestionType {
        ADHERENCE,      // Did you take medication as prescribed?
        SIDE_EFFECTS,   // Any side effects?
        EFFICACY,       // Is it working?
        LIFESTYLE,      // Impact on daily life
        OPEN_ENDED      // Free text
    }

    private QuestionType questionType;

    // Constructors
    public AssessmentResponse() {
    }

    public AssessmentResponse(int monitoringId, String questionText) {
        this.monitoringId = monitoringId;
        this.questionText = questionText;
    }

    // Getters and Setters
    public int getResponseId() {
        return responseId;
    }

    public void setResponseId(int responseId) {
        this.responseId = responseId;
    }

    public int getMonitoringId() {
        return monitoringId;
    }

    public void setMonitoringId(int monitoringId) {
        this.monitoringId = monitoringId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public Integer getResponseScore() {
        return responseScore;
    }

    public void setResponseScore(Integer responseScore) {
        this.responseScore = responseScore;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public LocalDateTime getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(LocalDateTime responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    // Utility methods

    /**
     * Check if response indicates a problem
     */
    public boolean indicatesProblem() {
        // Low scores (1-4) or negative sentiment indicate problems
        if (responseScore != null && responseScore <= 4) {
            return true;
        }
        return "Negative".equalsIgnoreCase(sentiment);
    }

    /**
     * Get response summary for display
     */
    public String getResponseSummary() {
        if (responseScore != null) {
            return String.format("%s (Score: %d/10)",
                    responseText != null ? responseText : "No text response",
                    responseScore);
        }
        return responseText != null ? responseText : "No response";
    }

    @Override
    public String toString() {
        return "AssessmentResponse{" +
                "id=" + responseId +
                ", question='" + questionText + '\'' +
                ", response='" + responseText + '\'' +
                ", score=" + responseScore +
                ", sentiment='" + sentiment + '\'' +
                '}';
    }
}
