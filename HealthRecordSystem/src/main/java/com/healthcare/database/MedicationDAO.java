package com.healthcare.database;

import com.healthcare.model.Medication;
import com.healthcare.model.PatientMedication;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MedicationDAO - Medication Database Access Object
 * Handles all database operations for medications
 */
public class MedicationDAO {

    // ============ MEDICATION MASTER OPERATIONS ============

    /**
     * Get all medications from master list
     */
    public List<Medication> getAllMedications() {
        List<Medication> medications = new ArrayList<>();
        String sql = "SELECT * FROM medications_master ORDER BY medication_name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medications.add(extractMedicationFromResultSet(rs));
            }

            System.out.println("✅ Retrieved " + medications.size() + " medications from master list");

        } catch (SQLException e) {
            System.err.println("❌ Error getting medications: " + e.getMessage());
            e.printStackTrace();
        }

        return medications;
    }

    /**
     * Get medication by ID
     */
    public Medication getMedicationById(int medicationId) {
        String sql = "SELECT * FROM medications_master WHERE medication_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, medicationId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractMedicationFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting medication: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // ============ PATIENT MEDICATION OPERATIONS ============

    /**
     * Prescribe medication to patient
     */
    public boolean prescribeMedication(PatientMedication patientMedication) {
        String sql = "INSERT INTO patient_medications " +
                "(patient_id, medication_id, prescribed_by, dosage, frequency, route, " +
                "start_date, end_date, status, instructions, reason) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, patientMedication.getPatientId());
            pstmt.setInt(2, patientMedication.getMedicationId());
            pstmt.setInt(3, patientMedication.getPrescribedBy());
            pstmt.setString(4, patientMedication.getDosage());
            pstmt.setString(5, patientMedication.getFrequency());
            pstmt.setString(6, patientMedication.getRoute());
            pstmt.setDate(7, Date.valueOf(patientMedication.getStartDate()));

            if (patientMedication.getEndDate() != null) {
                pstmt.setDate(8, Date.valueOf(patientMedication.getEndDate()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }

            pstmt.setString(9, patientMedication.getStatus());
            pstmt.setString(10, patientMedication.getInstructions());
            pstmt.setString(11, patientMedication.getReason());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    patientMedication.setPatientMedicationId(generatedKeys.getInt(1));
                }

                System.out.println("✅ Medication prescribed successfully! ID: " +
                        patientMedication.getPatientMedicationId());

                // Create initial monitoring record for 7-day assessment
                createMonitoringRecord(patientMedication.getPatientMedicationId(), 7);

                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error prescribing medication: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get all medications for a specific patient
     */
    public List<PatientMedication> getPatientMedications(int patientId) {
        List<PatientMedication> medications = new ArrayList<>();
        String sql = "SELECT pm.*, m.medication_name, m.generic_name, " +
                "CONCAT(p.first_name, ' ', p.last_name) as patient_name, " +
                "u.full_name as doctor_name " +
                "FROM patient_medications pm " +
                "JOIN medications_master m ON pm.medication_id = m.medication_id " +
                "JOIN patients p ON pm.patient_id = p.patient_id " +
                "LEFT JOIN users u ON pm.prescribed_by = u.user_id " +
                "WHERE pm.patient_id = ? " +
                "ORDER BY pm.start_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                medications.add(extractPatientMedicationFromResultSet(rs));
            }

            System.out.println("✅ Retrieved " + medications.size() +
                    " medications for patient ID: " + patientId);

        } catch (SQLException e) {
            System.err.println("❌ Error getting patient medications: " + e.getMessage());
            e.printStackTrace();
        }

        return medications;
    }

    /**
     * Get all active medications across all patients
     */
    public List<PatientMedication> getAllActiveMedications() {
        List<PatientMedication> medications = new ArrayList<>();
        String sql = "SELECT pm.*, m.medication_name, m.generic_name, " +
                "CONCAT(p.first_name, ' ', p.last_name) as patient_name, " +
                "u.full_name as doctor_name " +
                "FROM patient_medications pm " +
                "JOIN medications_master m ON pm.medication_id = m.medication_id " +
                "JOIN patients p ON pm.patient_id = p.patient_id " +
                "LEFT JOIN users u ON pm.prescribed_by = u.user_id " +
                "WHERE pm.status = 'Active' " +
                "ORDER BY pm.start_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medications.add(extractPatientMedicationFromResultSet(rs));
            }

            System.out.println("✅ Retrieved " + medications.size() + " active medications");

        } catch (SQLException e) {
            System.err.println("❌ Error getting active medications: " + e.getMessage());
            e.printStackTrace();
        }

        return medications;
    }

    /**
     * Get medications due for 7-day assessment
     */
    public List<PatientMedication> getMedicationsDueForAssessment() {
        List<PatientMedication> medications = new ArrayList<>();
        String sql = "SELECT pm.*, m.medication_name, m.generic_name, " +
                "CONCAT(p.first_name, ' ', p.last_name) as patient_name, " +
                "u.full_name as doctor_name " +
                "FROM patient_medications pm " +
                "JOIN medications_master m ON pm.medication_id = m.medication_id " +
                "JOIN patients p ON pm.patient_id = p.patient_id " +
                "LEFT JOIN users u ON pm.prescribed_by = u.user_id " +
                "WHERE pm.status = 'Active' " +
                "AND DATEDIFF(CURDATE(), pm.start_date) BETWEEN 7 AND 10 " +
                "ORDER BY pm.start_date";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medications.add(extractPatientMedicationFromResultSet(rs));
            }

            System.out.println("✅ Found " + medications.size() +
                    " medications due for assessment");

        } catch (SQLException e) {
            System.err.println("❌ Error getting medications due for assessment: " +
                    e.getMessage());
            e.printStackTrace();
        }

        return medications;
    }

    /**
     * Update medication status
     */
    public boolean updateMedicationStatus(int patientMedicationId, String newStatus) {
        String sql = "UPDATE patient_medications SET status = ?, " +
                "end_date = CASE WHEN ? != 'Active' THEN CURDATE() ELSE end_date END " +
                "WHERE patient_medication_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setString(2, newStatus);
            pstmt.setInt(3, patientMedicationId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Medication status updated to: " + newStatus);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error updating medication status: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Delete patient medication
     */
    public boolean deletePatientMedication(int patientMedicationId) {
        String sql = "DELETE FROM patient_medications WHERE patient_medication_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientMedicationId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Medication deleted successfully");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error deleting medication: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // ============ MONITORING OPERATIONS ============

    /**
     * Create monitoring record for medication assessment
     */
    private void createMonitoringRecord(int patientMedicationId, int assessmentDay) {
        String sql = "INSERT INTO medication_monitoring " +
                "(patient_medication_id, assessment_day, assessment_date, status) " +
                "VALUES (?, ?, DATE_ADD(CURDATE(), INTERVAL ? DAY), 'Pending')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientMedicationId);
            pstmt.setInt(2, assessmentDay);
            pstmt.setInt(3, assessmentDay);

            pstmt.executeUpdate();
            System.out.println("📅 Monitoring record created for day " + assessmentDay);

        } catch (SQLException e) {
            System.err.println("⚠️ Error creating monitoring record: " + e.getMessage());
        }
    }

    // ============ HELPER METHODS ============

    /**
     * Extract Medication from ResultSet
     */
    private Medication extractMedicationFromResultSet(ResultSet rs) throws SQLException {
        Medication medication = new Medication();
        medication.setMedicationId(rs.getInt("medication_id"));
        medication.setMedicationName(rs.getString("medication_name"));
        medication.setGenericName(rs.getString("generic_name"));
        medication.setCategory(rs.getString("category"));
        medication.setDescription(rs.getString("description"));
        medication.setCommonSideEffects(rs.getString("common_side_effects"));
        medication.setContraindications(rs.getString("contraindications"));
        return medication;
    }

    /**
     * Extract PatientMedication from ResultSet
     */
    private PatientMedication extractPatientMedicationFromResultSet(ResultSet rs)
            throws SQLException {
        PatientMedication pm = new PatientMedication();
        pm.setPatientMedicationId(rs.getInt("patient_medication_id"));
        pm.setPatientId(rs.getInt("patient_id"));
        pm.setMedicationId(rs.getInt("medication_id"));
        pm.setPrescribedBy(rs.getInt("prescribed_by"));

        pm.setMedicationName(rs.getString("medication_name"));
        pm.setDosage(rs.getString("dosage"));
        pm.setFrequency(rs.getString("frequency"));
        pm.setRoute(rs.getString("route"));

        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            pm.setStartDate(startDate.toLocalDate());
        }

        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            pm.setEndDate(endDate.toLocalDate());
        }

        pm.setStatus(rs.getString("status"));
        pm.setInstructions(rs.getString("instructions"));
        pm.setReason(rs.getString("reason"));

        pm.setPatientName(rs.getString("patient_name"));
        pm.setDoctorName(rs.getString("doctor_name"));

        return pm;
    }
}