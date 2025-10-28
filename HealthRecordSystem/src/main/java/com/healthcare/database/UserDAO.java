package com.healthcare.database;

import com.healthcare.model.User;
import com.healthcare.model.User.UserRole;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO - User Database Access Object
 * Handles all database operations for users
 * Authentication, user management, etc.
 */
public class UserDAO {

    /**
     * Authenticate user (Login)
     * @param username Username or email
     * @param password Plain text password
     * @return User object if successful, null if failed
     */
    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE (username = ? OR email = ?) AND is_active = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, username);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");

                // Verify password using BCrypt
                if (PasswordUtil.verifyPassword(password, storedHash)) {
                    User user = extractUserFromResultSet(rs);

                    // Update last login time
                    updateLastLogin(user.getUserId());

                    System.out.println("✅ User authenticated: " + user.getUsername());
                    return user;
                } else {
                    System.out.println("❌ Invalid password for user: " + username);
                }
            } else {
                System.out.println("❌ User not found: " + username);
            }

        } catch (SQLException e) {
            System.err.println("❌ Authentication error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Register a new user
     * @param user User object with details
     * @param plainPassword Plain text password (will be hashed)
     * @return true if successful, false otherwise
     */
    public boolean registerUser(User user, String plainPassword) {
        // Validate password strength
        if (!PasswordUtil.isPasswordStrong(plainPassword)) {
            System.err.println("❌ Password does not meet strength requirements");
            return false;
        }

        // Check if username already exists
        if (usernameExists(user.getUsername())) {
            System.err.println("❌ Username already exists: " + user.getUsername());
            return false;
        }

        // Check if email already exists
        if (emailExists(user.getEmail())) {
            System.err.println("❌ Email already exists: " + user.getEmail());
            return false;
        }

        String sql = "INSERT INTO users (username, password_hash, full_name, email, role, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Hash the password before storing
            String passwordHash = PasswordUtil.hashPassword(plainPassword);

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, passwordHash);
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getRoleAsString());
            pstmt.setBoolean(6, user.isActive());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the generated user ID
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                }

                System.out.println("✅ User registered successfully! ID: " + user.getUserId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error registering user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting user: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

            System.out.println("✅ Retrieved " + users.size() + " users");

        } catch (SQLException e) {
            System.err.println("❌ Error getting users: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Update user information
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET full_name=?, email=?, role=?, is_active=? WHERE user_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getRoleAsString());
            pstmt.setBoolean(4, user.isActive());
            pstmt.setInt(5, user.getUserId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ User updated successfully");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error updating user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Change user password
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        // Get current user
        User user = getUserById(userId);
        if (user == null) {
            return false;
        }

        // Verify old password
        String sql = "SELECT password_hash FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String currentHash = rs.getString("password_hash");

                if (!PasswordUtil.verifyPassword(oldPassword, currentHash)) {
                    System.err.println("❌ Current password is incorrect");
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error verifying password: " + e.getMessage());
            return false;
        }

        // Validate new password
        if (!PasswordUtil.isPasswordStrong(newPassword)) {
            System.err.println("❌ New password does not meet strength requirements");
            return false;
        }

        // Update password
        String updateSql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

            String newHash = PasswordUtil.hashPassword(newPassword);
            pstmt.setString(1, newHash);
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Password changed successfully");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error changing password: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Delete user (deactivate)
     */
    public boolean deleteUser(int userId) {
        // We don't actually delete - just deactivate
        String sql = "UPDATE users SET is_active = FALSE WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ User deactivated successfully");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error checking username: " + e.getMessage());
        }

        return false;
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error checking email: " + e.getMessage());
        }

        return false;
    }

    /**
     * Update last login timestamp
     */
    private void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("⚠️ Error updating last login: " + e.getMessage());
        }
    }

    /**
     * Helper method: Extract User from ResultSet
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRoleFromString(rs.getString("role"));
        user.setActive(rs.getBoolean("is_active"));

        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            user.setCreatedAt(createdTimestamp.toLocalDateTime());
        }

        Timestamp loginTimestamp = rs.getTimestamp("last_login");
        if (loginTimestamp != null) {
            user.setLastLogin(loginTimestamp.toLocalDateTime());
        }

        return user;
    }
}
