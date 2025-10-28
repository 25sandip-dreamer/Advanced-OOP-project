package com.healthcare.database;

import com.healthcare.model.ChatConversation;
import com.healthcare.model.ChatMessage;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ChatDAO - Database operations for chat system
 * Location: src/com/healthcare/database/ChatDAO.java
 */
public class ChatDAO {

    /**
     * Get or create conversation between doctor and patient
     */
    public int getOrCreateConversation(int patientId, int doctorId) {
        // Check if conversation exists
        String checkSql = "SELECT conversation_id FROM chat_conversations " +
                "WHERE patient_id = ? AND doctor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {

            pstmt.setInt(1, patientId);
            pstmt.setInt(2, doctorId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("conversation_id");
            }

        } catch (SQLException e) {
            System.err.println("Error checking conversation: " + e.getMessage());
        }

        // Create new conversation if doesn't exist
        String insertSql = "INSERT INTO chat_conversations (patient_id, doctor_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, patientId);
            pstmt.setInt(2, doctorId);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error creating conversation: " + e.getMessage());
        }

        return -1;
    }

    /**
     * Send a message
     */
    public boolean sendMessage(ChatMessage message) {
        String sql = "INSERT INTO chat_messages (conversation_id, sender_id, sender_type, message_text) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, message.getConversationId());
            pstmt.setInt(2, message.getSenderId());
            pstmt.setString(3, message.getSenderType());
            pstmt.setString(4, message.getMessageText());

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error sending message: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all messages in a conversation
     */
    public List<ChatMessage> getMessages(int conversationId) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT m.message_id, m.conversation_id, m.sender_id, m.sender_type, " +
                "m.message_text, m.sent_at, m.is_read, " +
                "CONCAT(u.first_name, ' ', u.last_name) AS sender_name " +
                "FROM chat_messages m " +
                "JOIN users u ON m.sender_id = u.user_id " +
                "WHERE m.conversation_id = ? " +
                "ORDER BY m.sent_at ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, conversationId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ChatMessage msg = new ChatMessage();
                msg.setMessageId(rs.getInt("message_id"));
                msg.setConversationId(rs.getInt("conversation_id"));
                msg.setSenderId(rs.getInt("sender_id"));
                msg.setSenderType(rs.getString("sender_type"));
                msg.setMessageText(rs.getString("message_text"));
                msg.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
                msg.setRead(rs.getBoolean("is_read"));
                msg.setSenderName(rs.getString("sender_name"));

                messages.add(msg);
            }

        } catch (SQLException e) {
            System.err.println("Error getting messages: " + e.getMessage());
            e.printStackTrace();
        }

        return messages;
    }

    /**
     * Get all conversations for a user (doctor or patient)
     */
    public List<ChatConversation> getConversationsForUser(int userId, String userType) {
        List<ChatConversation> conversations = new ArrayList<>();

        String sql = "SELECT c.conversation_id, c.patient_id, c.doctor_id, " +
                "c.created_at, c.last_message_at, " +
                "CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                "CONCAT(u.first_name, ' ', u.last_name) AS doctor_name, " +
                "(SELECT message_text FROM chat_messages " +
                " WHERE conversation_id = c.conversation_id " +
                " ORDER BY sent_at DESC LIMIT 1) AS last_message, " +
                "(SELECT COUNT(*) FROM chat_messages " +
                " WHERE conversation_id = c.conversation_id " +
                " AND sender_id != ? AND is_read = FALSE) AS unread_count " +
                "FROM chat_conversations c " +
                "JOIN patients p ON c.patient_id = p.patient_id " +
                "JOIN users u ON c.doctor_id = u.user_id " +
                "WHERE " + (userType.equals("DOCTOR") ? "c.doctor_id = ?" : "c.patient_id = ?") +
                " ORDER BY c.last_message_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ChatConversation conv = new ChatConversation();
                conv.setConversationId(rs.getInt("conversation_id"));
                conv.setPatientId(rs.getInt("patient_id"));
                conv.setPatientName(rs.getString("patient_name"));
                conv.setDoctorId(rs.getInt("doctor_id"));
                conv.setDoctorName(rs.getString("doctor_name"));
                conv.setLastMessageAt(rs.getTimestamp("last_message_at").toLocalDateTime());
                conv.setLastMessage(rs.getString("last_message"));
                conv.setUnreadCount(rs.getInt("unread_count"));

                conversations.add(conv);
            }

        } catch (SQLException e) {
            System.err.println("Error getting conversations: " + e.getMessage());
            e.printStackTrace();
        }

        return conversations;
    }

    /**
     * Mark messages as read
     */
    public boolean markMessagesAsRead(int conversationId, int userId) {
        String sql = "UPDATE chat_messages SET is_read = TRUE " +
                "WHERE conversation_id = ? AND sender_id != ? AND is_read = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, conversationId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get unread message count for a user
     */
    public int getUnreadCount(int userId, String userType) {
        String sql = "SELECT COUNT(*) AS unread FROM chat_messages m " +
                "JOIN chat_conversations c ON m.conversation_id = c.conversation_id " +
                "WHERE " + (userType.equals("DOCTOR") ? "c.doctor_id = ?" : "c.patient_id = ?") +
                " AND m.sender_id != ? AND m.is_read = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("unread");
            }

        } catch (SQLException e) {
            System.err.println("Error getting unread count: " + e.getMessage());
        }

        return 0;
    }
}
