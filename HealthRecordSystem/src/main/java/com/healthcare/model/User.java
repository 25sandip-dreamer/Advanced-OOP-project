package com.healthcare.model;

import java.time.LocalDateTime;

/**
 * User Model Class
 * Represents a system user (Admin, Doctor, or Patient)
 * Used for authentication and authorization
 */
public class User {

    // User attributes
    private int userId;
    private String username;
    private String passwordHash;  // We NEVER store plain passwords!
    private String fullName;
    private String email;
    private UserRole role;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // Enum for user roles
    public enum UserRole {
        ADMIN("Admin"),
        DOCTOR("Doctor"),
        PATIENT("Patient");

        private final String displayName;

        UserRole(String displayName) {
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
    public User() {
        this.isActive = true;
    }

    public User(String username, String passwordHash, String fullName,
                String email, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.isActive = true;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    // Utility methods

    /**
     * Check if user has admin privileges
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    /**
     * Check if user is a doctor
     */
    public boolean isDoctor() {
        return role == UserRole.DOCTOR;
    }

    /**
     * Check if user is a patient
     */
    public boolean isPatient() {
        return role == UserRole.PATIENT;
    }

    /**
     * Get role as string (for database storage)
     */
    public String getRoleAsString() {
        return role != null ? role.getDisplayName() : null;
    }

    /**
     * Set role from string (from database)
     */
    public void setRoleFromString(String roleString) {
        if (roleString != null) {
            switch (roleString.toLowerCase()) {
                case "admin":
                    this.role = UserRole.ADMIN;
                    break;
                case "doctor":
                    this.role = UserRole.DOCTOR;
                    break;
                case "patient":
                    this.role = UserRole.PATIENT;
                    break;
                default:
                    this.role = UserRole.PATIENT; // Default to patient
            }
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                '}';
    }
}