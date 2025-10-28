package com.healthcare.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MedicationAssessment Model
 * Tracks medication monitoring assessments (7-day, 14-day, etc.)
 */
public class MedicationAssessment {

    private int monitoringId;
    private int patientMedicationId;
    private int assessmentDay; // 7, 14, 30, 60, 90
    private LocalDate assessmentDate;
    private String status; // Pending, Completed, Overdue
    private boolean questionnaireSent;
    private boolean questionnaireCompleted;
    private boolean aiAnalysisCompleted;
    private LocalDateTime createdAt;

    // Additional info from joins
    private String patientName;
    private String medicationName;
    private String dosage;

    // Enum for assessment status
    public enum AssessmentStatus {
        PENDING("Pending"),
        COMPLETED("Completed"),
        OVERDUE("Overdue");

        private final String displayName;

        AssessmentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // Constructors
    public MedicationAssessment() {
        this.status = "Pending";
    }

    // Getters and Setters
    public int getMonitoringId() {
        return monitoringId;
    }

    public void setMonitoringId(int monitoringId) {
        this.monitoringId = monitoringId;
    }

    public int getPatientMedicationId() {
        return patientMedicationId;
    }

    public void setPatientMedicationId(int patientMedicationId) {
        this.patientMedicationId = patientMedicationId;
    }

    public int getAssessmentDay() {
        return assessmentDay;
    }

    public void setAssessmentDay(int assessmentDay) {
        this.assessmentDay = assessmentDay;
    }

    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDate assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isQuestionnaireSent() {
        return questionnaireSent;
    }

    public void setQuestionnaireSent(boolean questionnaireSent) {
        this.questionnaireSent = questionnaireSent;
    }

    public boolean isQuestionnaireCompleted() {
        return questionnaireCompleted;
    }

    public void setQuestionnaireCompleted(boolean questionnaireCompleted) {
        this.questionnaireCompleted = questionnaireCompleted;
    }

    public boolean isAiAnalysisCompleted() {
        return aiAnalysisCompleted;
    }

    public void setAiAnalysisCompleted(boolean aiAnalysisCompleted) {
        this.aiAnalysisCompleted = aiAnalysisCompleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    // Utility methods

    /**
     * Check if assessment is overdue
     */
    public boolean isOverdue() {
        return assessmentDate != null &&
                LocalDate.now().isAfter(assessmentDate) &&
                !questionnaireCompleted;
    }

    /**
     * Check if assessment is due today
     */
    public boolean isDueToday() {
        return assessmentDate != null &&
                assessmentDate.equals(LocalDate.now()) &&
                !questionnaireCompleted;
    }

    /**
     * Get days until assessment (negative if overdue)
     */
    public long getDaysUntilAssessment() {
        if (assessmentDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), assessmentDate);
    }

    /**
     * Get assessment summary
     */
    public String getAssessmentSummary() {
        return String.format("Day %d Assessment - %s", assessmentDay, status);
    }

    @Override
    public String toString() {
        return "MedicationAssessment{" +
                "id=" + monitoringId +
                ", day=" + assessmentDay +
                ", date=" + assessmentDate +
                ", status='" + status + '\'' +
                ", completed=" + questionnaireCompleted +
                '}';
    }
}
