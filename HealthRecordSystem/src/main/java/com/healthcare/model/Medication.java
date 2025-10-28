package com.healthcare.model;

/**
 * Medication Model Class
 * Represents a medication in the master medications list
 */
public class Medication {

    private int medicationId;
    private String medicationName;
    private String genericName;
    private String category;
    private String description;
    private String commonSideEffects;
    private String contraindications;

    // Constructors
    public Medication() {
    }

    public Medication(String medicationName, String genericName, String category) {
        this.medicationName = medicationName;
        this.genericName = genericName;
        this.category = category;
    }

    // Getters and Setters
    public int getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(int medicationId) {
        this.medicationId = medicationId;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommonSideEffects() {
        return commonSideEffects;
    }

    public void setCommonSideEffects(String commonSideEffects) {
        this.commonSideEffects = commonSideEffects;
    }

    public String getContraindications() {
        return contraindications;
    }

    public void setContraindications(String contraindications) {
        this.contraindications = contraindications;
    }

    // Utility methods

    /**
     * Get display name (shows both brand and generic name)
     */
    public String getDisplayName() {
        if (genericName != null && !genericName.isEmpty() && !genericName.equals(medicationName)) {
            return medicationName + " (" + genericName + ")";
        }
        return medicationName;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
