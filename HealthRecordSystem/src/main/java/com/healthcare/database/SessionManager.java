package com.healthcare.database;

import com.healthcare.model.User;
import java.time.LocalDateTime;

/**
 * SessionManager - Manages current user session
 * Singleton pattern - only one instance exists
 * Keeps track of who is logged in
 */
public class SessionManager {

    // Singleton instance
    private static SessionManager instance = null;

    // Current logged-in user
    private User currentUser = null;

    // Session start time
    private LocalDateTime sessionStartTime = null;

    // Private constructor (Singleton pattern)
    private SessionManager() {
        // Private to prevent direct instantiation
    }

    /**
     * Get the singleton instance
     * @return SessionManager instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Login - Set current user and start session
     * @param user The logged-in user
     */
    public void login(User user) {
        this.currentUser = user;
        this.sessionStartTime = LocalDateTime.now();

        System.out.println("✅ Session started for: " + user.getFullName());
        System.out.println("   Role: " + user.getRole());
        System.out.println("   Time: " + sessionStartTime);
    }

    /**
     * Logout - Clear current user and end session
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("👋 User logged out: " + currentUser.getFullName());
            System.out.println("   Session duration: " + getSessionDuration() + " minutes");
        }

        this.currentUser = null;
        this.sessionStartTime = null;
    }

    /**
     * Check if user is logged in
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Get current logged-in user
     * @return Current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Get session start time
     * @return Session start time or null if not logged in
     */
    public LocalDateTime getSessionStartTime() {
        return sessionStartTime;
    }

    /**
     * Get session duration in minutes
     * @return Duration in minutes or 0 if not logged in
     */
    public long getSessionDuration() {
        if (sessionStartTime == null) {
            return 0;
        }

        return java.time.Duration.between(sessionStartTime, LocalDateTime.now()).toMinutes();
    }

    /**
     * Check if current user has specific role
     * @param role Role to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(User.UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return hasRole(User.UserRole.ADMIN);
    }

    /**
     * Check if current user is doctor
     */
    public boolean isDoctor() {
        return hasRole(User.UserRole.DOCTOR);
    }

    /**
     * Check if current user is patient
     */
    public boolean isPatient() {
        return hasRole(User.UserRole.PATIENT);
    }

    /**
     * Get welcome message for current user
     */
    public String getWelcomeMessage() {
        if (currentUser == null) {
            return "Welcome, Guest!";
        }

        String roleIcon = "";
        switch (currentUser.getRole()) {
            case ADMIN:
                roleIcon = "👨‍💼";
                break;
            case DOCTOR:
                roleIcon = "👨‍⚕️";
                break;
            case PATIENT:
                roleIcon = "🧑‍🦱";
                break;
        }

        return roleIcon + " Welcome, " + currentUser.getFullName() + "!";
    }

    /**
     * Get current user info as string
     */
    @Override
    public String toString() {
        if (currentUser == null) {
            return "No active session";
        }

        return "Session{" +
                "user=" + currentUser.getFullName() +
                ", role=" + currentUser.getRole() +
                ", duration=" + getSessionDuration() + " min" +
                '}';
    }
}
