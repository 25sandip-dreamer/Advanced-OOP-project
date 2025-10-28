package com.healthcare.database;

import com.healthcare.model.AssessmentResponse;
import com.healthcare.model.MedicationAssessment;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AssessmentDAO - Medication Assessment Database Access
 * Handles questionnaires and patient responses
 */
public class AssessmentDAO {

    // ============ ASSESSMENT OPERATIONS ============

    /**
     * Get all pending assessments (due for questionnaire)
     */
    public List<MedicationAssessment> getPendingAssessments() {
        List<MedicationAssessment> assessments = new ArrayList<>();
        String sql = "SELECT mm.*, pm.dosage, m.medication_name, " +
                "CONCAT(p.first_name, ' ', p.last_name) as patient_name " +
                "FROM medication_monitoring mm " +
                "JOIN patient_medications pm ON mm.patient_medication_id = pm.patient_medication_id " +
                "JOIN medications_master m ON pm.medication_id = m.medication_id " +
                "JOIN patients p ON pm.patient_id = p.patient_id " +
                "WHERE mm.status = 'Pending' " +
                "AND mm.assessment_date <= CURDATE() " +
                "ORDER BY mm.assessment_date";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                assessments.add(extractAssessmentFromResultSet(rs));
            }

            System.out.println("✅ Retrieved " + assessments.size() + " pending assessments");

        } catch (SQLException e) {
            System.err.println("❌ Error getting pending assessments: " + e.getMessage());
            e.printStackTrace();
        }

        return assessments;
    }

    /**
     * Get assessment by ID
     */
    public MedicationAssessment getAssessmentById(int monitoringId) {
        String sql = "SELECT mm.*, pm.dosage, m.medication_name, " +
                "CONCAT(p.first_name, ' ', p.last_name) as patient_name " +
                "FROM medication_monitoring mm " +
                "JOIN patient_medications pm ON mm.patient_medication_id = pm.patient_medication_id " +
                "JOIN medications_master m ON pm.medication_id = m.medication_id " +
                "JOIN patients p ON pm.patient_id = p.patient_id " +
                "WHERE mm.monitoring_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, monitoringId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractAssessmentFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting assessment: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get assessments for specific patient medication
     */
    public List<MedicationAssessment> getAssessmentsForMedication(int patientMedicationId) {
        List<MedicationAssessment> assessments = new ArrayList<>();
        String sql = "SELECT mm.*, pm.dosage, m.medication_name, " +
                "CONCAT(p.first_name, ' ', p.last_name) as patient_name " +
                "FROM medication_monitoring mm " +
                "JOIN patient_medications pm ON mm.patient_medication_id = pm.patient_medication_id " +
                "JOIN medications_master m ON pm.medication_id = m.medication_id " +
                "JOIN patients p ON pm.patient_id = p.patient_id " +
                "WHERE mm.patient_medication_id = ? " +
                "ORDER BY mm.assessment_day";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientMedicationId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                assessments.add(extractAssessmentFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting assessments: " + e.getMessage());
            e.printStackTrace();
        }

        return assessments;
    }

    /**
     * Mark questionnaire as sent
     */
    public boolean markQuestionnaireSent(int monitoringId) {
        String sql = "UPDATE medication_monitoring SET questionnaire_sent = TRUE WHERE monitoring_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, monitoringId);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Questionnaire marked as sent");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error marking questionnaire sent: " + e.getMessage());
        }

        return false;
    }

    /**
     * Mark questionnaire as completed
     */
    public boolean markQuestionnaireCompleted(int monitoringId) {
        String sql = "UPDATE medication_monitoring SET " +
                "questionnaire_completed = TRUE, " +
                "status = 'Completed' " +
                "WHERE monitoring_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, monitoringId);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Questionnaire marked as completed");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error marking questionnaire completed: " + e.getMessage());
        }

        return false;
    }

    // ============ RESPONSE OPERATIONS ============

    /**
     * Save patient response to questionnaire
     */
    public boolean saveResponse(AssessmentResponse response) {
        String sql = "INSERT INTO medication_assessment_responses " +
                "(monitoring_id, question_text, response_text, response_score, sentiment) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, response.getMonitoringId());
            pstmt.setString(2, response.getQuestionText());
            pstmt.setString(3, response.getResponseText());

            if (response.getResponseScore() != null) {
                pstmt.setInt(4, response.getResponseScore());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            pstmt.setString(5, response.getSentiment());

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    response.setResponseId(generatedKeys.getInt(1));
                }
                System.out.println("✅ Response saved: " + response.getResponseId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error saving response: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get all responses for an assessment
     */
    public List<AssessmentResponse> getResponsesForAssessment(int monitoringId) {
        List<AssessmentResponse> responses = new ArrayList<>();
        String sql = "SELECT * FROM medication_assessment_responses " +
                "WHERE monitoring_id = ? " +
                "ORDER BY response_timestamp";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, monitoringId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                responses.add(extractResponseFromResultSet(rs));
            }

            System.out.println("✅ Retrieved " + responses.size() + " responses");

        } catch (SQLException e) {
            System.err.println("❌ Error getting responses: " + e.getMessage());
            e.printStackTrace();
        }

        return responses;
    }

    /**
     * Update overdue assessments
     * Call this periodically to mark assessments as overdue
     */
    public int updateOverdueAssessments() {
        String sql = "UPDATE medication_monitoring SET status = 'Overdue' " +
                "WHERE status = 'Pending' " +
                "AND assessment_date < CURDATE() " +
                "AND questionnaire_completed = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            int rows = stmt.executeUpdate(sql);

            if (rows > 0) {
                System.out.println("⚠️ Marked " + rows + " assessments as overdue");
            }

            return rows;

        } catch (SQLException e) {
            System.err.println("❌ Error updating overdue assessments: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Mark AI analysis as completed and store results summary
     */
    public boolean markAIAnalysisCompleted(int monitoringId, String analysisSummary) {
        String sql = "UPDATE medication_monitoring SET " +
                "ai_analysis_completed = TRUE " +
                "WHERE monitoring_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, monitoringId);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ AI analysis marked as completed for monitoring ID: " + monitoringId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error marking AI analysis completed: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get all completed assessments (ready for doctor review)
     */
    public List<MedicationAssessment> getCompletedAssessments() {
        List<MedicationAssessment> assessments = new ArrayList<>();
        String sql = "SELECT mm.*, pm.dosage, m.medication_name, " +
                "CONCAT(p.first_name, ' ', p.last_name) as patient_name " +
                "FROM medication_monitoring mm " +
                "JOIN patient_medications pm ON mm.patient_medication_id = pm.patient_medication_id " +
                "JOIN medications_master m ON pm.medication_id = m.medication_id " +
                "JOIN patients p ON pm.patient_id = p.patient_id " +
                "WHERE mm.questionnaire_completed = TRUE " +
                "ORDER BY mm.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                assessments.add(extractAssessmentFromResultSet(rs));
            }

            System.out.println("✅ Retrieved " + assessments.size() + " completed assessments");

        } catch (SQLException e) {
            System.err.println("❌ Error getting completed assessments: " + e.getMessage());
            e.printStackTrace();
        }

        return assessments;
    }

    // ============ HELPER METHODS ============

    /**
     * Extract MedicationAssessment from ResultSet
     */
    private MedicationAssessment extractAssessmentFromResultSet(ResultSet rs) throws SQLException {
        MedicationAssessment assessment = new MedicationAssessment();

        assessment.setMonitoringId(rs.getInt("monitoring_id"));
        assessment.setPatientMedicationId(rs.getInt("patient_medication_id"));
        assessment.setAssessmentDay(rs.getInt("assessment_day"));

        Date assessmentDate = rs.getDate("assessment_date");
        if (assessmentDate != null) {
            assessment.setAssessmentDate(assessmentDate.toLocalDate());
        }

        assessment.setStatus(rs.getString("status"));
        assessment.setQuestionnaireSent(rs.getBoolean("questionnaire_sent"));
        assessment.setQuestionnaireCompleted(rs.getBoolean("questionnaire_completed"));
        assessment.setAiAnalysisCompleted(rs.getBoolean("ai_analysis_completed"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            assessment.setCreatedAt(createdAt.toLocalDateTime());
        }

        // Additional info from joins
        assessment.setPatientName(rs.getString("patient_name"));
        assessment.setMedicationName(rs.getString("medication_name"));
        assessment.setDosage(rs.getString("dosage"));

        return assessment;
    }

    /**
     * Extract AssessmentResponse from ResultSet
     */
    private AssessmentResponse extractResponseFromResultSet(ResultSet rs) throws SQLException {
        AssessmentResponse response = new AssessmentResponse();

        response.setResponseId(rs.getInt("response_id"));
        response.setMonitoringId(rs.getInt("monitoring_id"));
        response.setQuestionText(rs.getString("question_text"));
        response.setResponseText(rs.getString("response_text"));

        int score = rs.getInt("response_score");
        if (!rs.wasNull()) {
            response.setResponseScore(score);
        }

        response.setSentiment(rs.getString("sentiment"));

        Timestamp timestamp = rs.getTimestamp("response_timestamp");
        if (timestamp != null) {
            response.setResponseTimestamp(timestamp.toLocalDateTime());
        }

        return response;
    }
}