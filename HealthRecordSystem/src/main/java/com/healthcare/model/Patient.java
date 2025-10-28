package com.healthcare.model;

import java.time.LocalDate;

public class Patient {

    // Fields - matching what PatientDAO expects
    private int patientId;
    private String firstName;      // ⭐ Changed from fullName
    private String lastName;       // ⭐ NEW - was missing
    private String email;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;          // ⭐ Changed from phoneNumber
    private String address;
    private String bloodGroup;     // ⭐ Changed from bloodType
    private String photoPath;
    private LocalDate createdAt;

    // Constructors
    public Patient() {
    }

    // Getters and Setters

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Helper method for full name
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
    /**
     * Calculate patient's age from date of birth
     */
    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return java.time.Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    @Override
    public String toString() {
        return getFullName();
    }
}





