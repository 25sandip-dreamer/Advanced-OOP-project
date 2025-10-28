package com.healthcare.database;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtil - Secure Password Handling
 * Uses BCrypt for password hashing (industry standard)
 * NEVER stores plain text passwords!
 */
public class PasswordUtil {

    // BCrypt work factor (higher = more secure but slower)
    // 12 is a good balance between security and performance
    private static final int WORK_FACTOR = 12;

    /**
     * Hash a plain text password
     * This is what we store in the database
     *
     * @param plainPassword The password entered by user
     * @return Hashed password (safe to store)
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // BCrypt automatically generates a salt and hashes the password
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));

        System.out.println("🔒 Password hashed securely");
        return hashedPassword;
    }

    /**
     * Verify if a plain text password matches the hashed password
     * Used during login
     *
     * @param plainPassword The password user entered
     * @param hashedPassword The hashed password from database
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        try {
            // BCrypt handles the comparison securely
            boolean matches = BCrypt.checkpw(plainPassword, hashedPassword);

            if (matches) {
                System.out.println("✅ Password verified successfully");
            } else {
                System.out.println("❌ Password verification failed");
            }

            return matches;

        } catch (Exception e) {
            System.err.println("⚠️ Error verifying password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate password strength
     * Returns true if password meets requirements
     *
     * Requirements:
     * - At least 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        return hasUpper && hasLower && hasDigit;
    }

    /**
     * Get password strength message
     */
    public static String getPasswordStrengthMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }

        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasUpper) {
            return "Password must contain at least one uppercase letter";
        }

        if (!hasLower) {
            return "Password must contain at least one lowercase letter";
        }

        if (!hasDigit) {
            return "Password must contain at least one digit";
        }

        return "Password is strong ✅";
    }

    /**
     * Test the password utility
     */
    public static void main(String[] args) {
        System.out.println("🔒 Testing Password Utility");
        System.out.println("==========================\n");

        // Test password hashing
        String plainPassword = "MySecurePass123";
        System.out.println("Original password: " + plainPassword);

        String hashed = hashPassword(plainPassword);
        System.out.println("Hashed password: " + hashed);
        System.out.println("(This is what gets stored in database)\n");

        // Test password verification
        System.out.println("Testing correct password:");
        verifyPassword(plainPassword, hashed);

        System.out.println("\nTesting wrong password:");
        verifyPassword("WrongPassword", hashed);

        // Test password strength
        System.out.println("\n📊 Password Strength Tests:");
        String[] testPasswords = {
                "weak",
                "onlylowercase",
                "ONLYUPPERCASE",
                "NoDigits",
                "Good123",
                "VeryStrong123"
        };

        for (String pwd : testPasswords) {
            System.out.println(pwd + " → " + getPasswordStrengthMessage(pwd));
        }
    }
}
