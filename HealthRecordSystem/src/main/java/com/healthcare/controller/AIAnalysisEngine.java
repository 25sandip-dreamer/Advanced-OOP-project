package com.healthcare.controller;

import com.healthcare.model.AssessmentResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AIAnalysisEngine - Advanced AI Analysis for Medication Assessments
 * Analyzes patient responses and generates clinical insights
 */
public class AIAnalysisEngine {

    /**
     * Complete Analysis Result
     */
    public static class AnalysisResult {
        private double adherenceScore;        // 0-100
        private double sideEffectScore;       // 0-100 (higher = more severe)
        private double efficacyScore;         // 0-100
        private double overallRiskScore;      // 0-100
        private String riskLevel;             // Low, Moderate, High, Critical
        private String overallSentiment;      // Positive, Neutral, Negative
        private String recommendation;
        private Map<String, String> keyFindings;
        private boolean requiresImmediateAttention;

        public AnalysisResult() {
            this.keyFindings = new HashMap<>();
        }

        // Getters and Setters
        public double getAdherenceScore() { return adherenceScore; }
        public void setAdherenceScore(double adherenceScore) { this.adherenceScore = adherenceScore; }

        public double getSideEffectScore() { return sideEffectScore; }
        public void setSideEffectScore(double sideEffectScore) { this.sideEffectScore = sideEffectScore; }

        public double getEfficacyScore() { return efficacyScore; }
        public void setEfficacyScore(double efficacyScore) { this.efficacyScore = efficacyScore; }

        public double getOverallRiskScore() { return overallRiskScore; }
        public void setOverallRiskScore(double overallRiskScore) { this.overallRiskScore = overallRiskScore; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public String getOverallSentiment() { return overallSentiment; }
        public void setOverallSentiment(String overallSentiment) { this.overallSentiment = overallSentiment; }

        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

        public Map<String, String> getKeyFindings() { return keyFindings; }
        public void addKeyFinding(String category, String finding) {
            this.keyFindings.put(category, finding);
        }

        public boolean isRequiresImmediateAttention() { return requiresImmediateAttention; }
        public void setRequiresImmediateAttention(boolean requiresImmediateAttention) {
            this.requiresImmediateAttention = requiresImmediateAttention;
        }
    }

    /**
     * Perform complete AI analysis on patient responses
     */
    public static AnalysisResult analyzeResponses(List<AssessmentResponse> responses) {
        AnalysisResult result = new AnalysisResult();

        if (responses == null || responses.isEmpty()) {
            return result;
        }

        // Analyze different aspects
        result.setAdherenceScore(calculateAdherenceScore(responses));
        result.setSideEffectScore(calculateSideEffectScore(responses));
        result.setEfficacyScore(calculateEfficacyScore(responses));

        // Calculate overall risk
        double overallRisk = calculateOverallRisk(
                result.getAdherenceScore(),
                result.getSideEffectScore(),
                result.getEfficacyScore()
        );
        result.setOverallRiskScore(overallRisk);
        result.setRiskLevel(determineRiskLevel(overallRisk));

        // Determine sentiment
        result.setOverallSentiment(determineSentiment(responses));

        // Generate findings
        generateKeyFindings(responses, result);

        // Generate recommendation
        result.setRecommendation(generateRecommendation(result));

        // Check if immediate attention needed
        result.setRequiresImmediateAttention(
                result.getSideEffectScore() > 70 ||
                        result.getAdherenceScore() < 30 ||
                        result.getRiskLevel().equals("Critical")
        );

        return result;
    }

    /**
     * Calculate adherence score (0-100)
     * Higher score = better adherence
     */
    private static double calculateAdherenceScore(List<AssessmentResponse> responses) {
        double score = 100.0;

        for (AssessmentResponse response : responses) {
            String question = response.getQuestionText().toLowerCase();
            String answer = response.getResponseText() != null ?
                    response.getResponseText().toLowerCase() : "";
            Integer numericScore = response.getResponseScore();

            // Check for missed doses
            if (question.contains("missed") || question.contains("doses")) {
                if (numericScore != null) {
                    // If they report missed doses (1-10 scale where higher = more missed)
                    score -= numericScore * 5; // Reduce score by 5 points per missed dose
                } else if (answer.contains("forgot") || answer.matches(".*\\d+.*")) {
                    // Try to extract number from text
                    score -= 15;
                }
            }

            // Check for adherence patterns
            if (question.contains("same time") || question.contains("regularly")) {
                if (answer.contains("no") || answer.contains("not")) {
                    score -= 20;
                } else if (answer.contains("yes") || answer.contains("always")) {
                    score += 5; // Bonus for consistency
                }
            }
        }

        return Math.max(0, Math.min(100, score)); // Keep between 0-100
    }

    /**
     * Calculate side effect severity score (0-100)
     * Higher score = more severe side effects
     */
    private static double calculateSideEffectScore(List<AssessmentResponse> responses) {
        double maxSeverity = 0;
        int sideEffectCount = 0;

        for (AssessmentResponse response : responses) {
            String question = response.getQuestionText().toLowerCase();
            String answer = response.getResponseText() != null ?
                    response.getResponseText().toLowerCase() : "";
            Integer numericScore = response.getResponseScore();

            if (question.contains("side effect")) {
                if (numericScore != null) {
                    // Convert 1-10 scale to 0-100
                    double severity = (numericScore / 10.0) * 100;
                    maxSeverity = Math.max(maxSeverity, severity);
                }

                // Check for severe keywords
                if (answer.contains("severe") || answer.contains("terrible") ||
                        answer.contains("unbearable")) {
                    maxSeverity = Math.max(maxSeverity, 80);
                    sideEffectCount++;
                }

                // Check for concerning symptoms
                String[] concerningSymptoms = {"chest pain", "difficulty breathing",
                        "swelling", "rash", "bleeding", "seizure"};
                for (String symptom : concerningSymptoms) {
                    if (answer.contains(symptom)) {
                        maxSeverity = Math.max(maxSeverity, 90);
                        sideEffectCount++;
                    }
                }
            }
        }

        // If multiple side effects, increase score
        if (sideEffectCount > 2) {
            maxSeverity = Math.min(100, maxSeverity + 10);
        }

        return maxSeverity;
    }

    /**
     * Calculate medication efficacy score (0-100)
     * Higher score = medication is working well
     */
    private static double calculateEfficacyScore(List<AssessmentResponse> responses) {
        double totalScore = 0;
        int efficacyQuestions = 0;

        for (AssessmentResponse response : responses) {
            String question = response.getQuestionText().toLowerCase();
            String answer = response.getResponseText() != null ?
                    response.getResponseText().toLowerCase() : "";
            Integer numericScore = response.getResponseScore();

            if (question.contains("improved") || question.contains("working") ||
                    question.contains("symptoms") || question.contains("satisfied")) {

                efficacyQuestions++;

                if (numericScore != null) {
                    // Convert to 0-100 scale
                    totalScore += (numericScore / 10.0) * 100;
                } else {
                    // Text-based analysis
                    if (answer.contains("much better") || answer.contains("completely resolved")) {
                        totalScore += 90;
                    } else if (answer.contains("better") || answer.contains("improved")) {
                        totalScore += 70;
                    } else if (answer.contains("same") || answer.contains("no change")) {
                        totalScore += 40;
                    } else if (answer.contains("worse")) {
                        totalScore += 10;
                    } else {
                        totalScore += 50; // Neutral
                    }
                }
            }
        }

        return efficacyQuestions > 0 ? totalScore / efficacyQuestions : 50;
    }

    /**
     * Calculate overall risk score
     */
    private static double calculateOverallRisk(double adherence, double sideEffects, double efficacy) {
        // Weighted risk calculation
        double adherenceRisk = (100 - adherence) * 0.4;  // 40% weight
        double sideEffectRisk = sideEffects * 0.4;        // 40% weight
        double efficacyRisk = (100 - efficacy) * 0.2;     // 20% weight

        return adherenceRisk + sideEffectRisk + efficacyRisk;
    }

    /**
     * Determine risk level from score
     */
    private static String determineRiskLevel(double riskScore) {
        if (riskScore >= 75) return "Critical";
        if (riskScore >= 50) return "High";
        if (riskScore >= 25) return "Moderate";
        return "Low";
    }

    /**
     * Determine overall sentiment
     */
    private static String determineSentiment(List<AssessmentResponse> responses) {
        int positive = 0, negative = 0, neutral = 0;

        for (AssessmentResponse response : responses) {
            String sentiment = response.getSentiment();
            if ("Positive".equals(sentiment)) positive++;
            else if ("Negative".equals(sentiment)) negative++;
            else neutral++;
        }

        if (positive > negative && positive > neutral) return "Positive";
        if (negative > positive) return "Negative";
        return "Neutral";
    }

    /**
     * Generate key findings summary
     */
    private static void generateKeyFindings(List<AssessmentResponse> responses, AnalysisResult result) {
        // Adherence findings
        if (result.getAdherenceScore() >= 90) {
            result.addKeyFinding("Adherence", "✅ Excellent adherence - patient taking medication as prescribed");
        } else if (result.getAdherenceScore() >= 70) {
            result.addKeyFinding("Adherence", "⚠️ Good adherence with occasional missed doses");
        } else {
            result.addKeyFinding("Adherence", "🚨 Poor adherence - patient frequently missing doses");
        }

        // Side effects findings
        if (result.getSideEffectScore() < 20) {
            result.addKeyFinding("Side Effects", "✅ Minimal to no side effects reported");
        } else if (result.getSideEffectScore() < 50) {
            result.addKeyFinding("Side Effects", "⚠️ Mild side effects present but tolerable");
        } else {
            result.addKeyFinding("Side Effects", "🚨 Significant side effects affecting patient");
        }

        // Efficacy findings
        if (result.getEfficacyScore() >= 70) {
            result.addKeyFinding("Efficacy", "✅ Medication showing good therapeutic effect");
        } else if (result.getEfficacyScore() >= 40) {
            result.addKeyFinding("Efficacy", "⚠️ Moderate improvement - may need adjustment");
        } else {
            result.addKeyFinding("Efficacy", "🚨 Limited therapeutic benefit observed");
        }
    }

    /**
     * Generate clinical recommendation
     */
    private static String generateRecommendation(AnalysisResult result) {
        StringBuilder rec = new StringBuilder();

        if (result.getRiskLevel().equals("Critical")) {
            rec.append("🚨 IMMEDIATE ATTENTION REQUIRED:\n");
        } else if (result.getRiskLevel().equals("High")) {
            rec.append("⚠️ PROMPT FOLLOW-UP RECOMMENDED:\n");
        } else {
            rec.append("✅ CONTINUE CURRENT TREATMENT:\n");
        }

        // Adherence recommendations
        if (result.getAdherenceScore() < 70) {
            rec.append("• Counsel patient on importance of medication adherence\n");
            rec.append("• Consider reminder systems or simplified dosing schedule\n");
        }

        // Side effect recommendations
        if (result.getSideEffectScore() > 50) {
            rec.append("• Review side effects with patient\n");
            rec.append("• Consider dose adjustment or alternative medication\n");
        }

        // Efficacy recommendations
        if (result.getEfficacyScore() < 50) {
            rec.append("• Medication may not be providing adequate benefit\n");
            rec.append("• Consider dose increase or alternative therapy\n");
        }

        if (result.getRiskLevel().equals("Low")) {
            rec.append("• Continue current regimen\n");
            rec.append("• Schedule routine follow-up in 30 days\n");
        }

        return rec.toString();
    }

    /**
     * Get risk level color for UI
     */
    public static String getRiskColor(String riskLevel) {
        switch (riskLevel) {
            case "Critical": return "#D32F2F"; // Red
            case "High": return "#FF9800"; // Orange
            case "Moderate": return "#FFC107"; // Yellow
            case "Low": return "#4CAF50"; // Green
            default: return "#757575"; // Gray
        }
    }
}
