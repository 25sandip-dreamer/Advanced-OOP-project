package com.healthcare.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * PatientMedication Model Class
 * Represents a medication prescribed to a specific patient
 */
public class PatientMedication {

    private int patientMedicationId;
    private int patientId;
    private int medicationId;
    private int prescribedBy; // user_id of doctor

    // Medication details
    private String medicationName; // From join with medications_master
    private String dosage;
    private String frequency;
    private String route;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // Active, Completed, Discontinued
    private String instructions;
    private String reason;

    // Additional info
    private String patientName; // From join with patients
    private String doctorName; // From join with users

    // Enum for medication status
    public enum MedicationStatus {
        ACTIVE("Active"),
        COMPLETED("Completed"),
        DISCONTINUED("Discontinued");

        private final String displayName;

        MedicationStatus(String displayName) {
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
    public PatientMedication() {
        this.status = "Active";
    }

    // Getters and Setters
    public int getPatientMedicationId() {
        return patientMedicationId;
    }

    public void setPatientMedicationId(int patientMedicationId) {
        this.patientMedicationId = patientMedicationId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(int medicationId) {
        this.medicationId = medicationId;
    }

    public int getPrescribedBy() {
        return prescribedBy;
    }

    public void setPrescribedBy(int prescribedBy) {
        this.prescribedBy = prescribedBy;
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

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    // Utility methods

    /**
     * Get number of days since medication started
     */
    public long getDaysSinceStart() {
        if (startDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, LocalDate.now());
    }

    /**
     * Check if medication is currently active
     */
    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }

    /**
     * Check if medication has been taken for at least X days
     */
    public boolean hasBeenTakenForDays(int days) {
        return getDaysSinceStart() >= days;
    }

    /**
     * Check if it's time for 7-day assessment
     */
    public boolean isDueForSevenDayAssessment() {
        long days = getDaysSinceStart();
        return isActive() && days >= 7 && days <= 10; // Grace period of 3 days
    }

    /**
     * Get medication summary for display
     */
    public String getMedicationSummary() {
        return medicationName + " " + dosage + " - " + frequency;
    }

    /**
     * Get duration text
     */
    public String getDurationText() {
        long days = getDaysSinceStart();
        if (days == 0) {
            return "Started today";
        } else if (days == 1) {
            return "1 day ago";
        } else if (days < 7) {
            return days + " days ago";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        } else {
            long months = days / 30;
            return months + (months == 1 ? " month ago" : " months ago");
        }
    }

    @Override
    public String toString() {
        return "PatientMedication{" +
                "id=" + patientMedicationId +
                ", medication='" + medicationName + '\'' +
                ", dosage='" + dosage + '\'' +
                ", frequency='" + frequency + '\'' +
                ", status='" + status + '\'' +
                ", daysSinceStart=" + getDaysSinceStart() +
                '}';
    }
}
