package com.healthcare.database;

import com.healthcare.model.Patient;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PatientDAO (Data Access Object)
 * This class handles ALL database operations for patients
 * Create, Read, Update, Delete (CRUD)
 */
public class PatientDAO {

    /**
     * Get ALL patients from database
     * @return List of all patients
     */

    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();

        String sql = "SELECT patient_id, first_name, last_name, date_of_birth, gender, " +
                "phone, email, address, blood_group, created_at " +
                "FROM patients ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Patient patient = new Patient();
                patient.setPatientId(rs.getInt("patient_id"));
                patient.setFirstName(rs.getString("first_name"));
                patient.setLastName(rs.getString("last_name"));
                patient.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                patient.setGender(rs.getString("gender"));
                patient.setPhone(rs.getString("phone"));
                patient.setEmail(rs.getString("email"));
                patient.setAddress(rs.getString("address"));
                patient.setBloodGroup(rs.getString("blood_group"));

                patients.add(patient);
            }

        } catch (SQLException e) {
            System.err.println("Error getting patients: " + e.getMessage());
            e.printStackTrace();
        }

        return patients;
    }

    /**
     * Get a SINGLE patient by ID
     * @param patientId The patient's ID
     * @return Patient object or null if not found
     */
    public Patient getPatientById(int patientId) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractPatientFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting patient: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * ADD a new patient to database
     * @param patient The patient to add
     * @return true if successful, false otherwise
     */
    public boolean addPatient(Patient patient) {
        String sql = "INSERT INTO patients (first_name, last_name, date_of_birth, gender, " +
                "phone, email, address, blood_group, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patient.getFirstName());
            pstmt.setString(2, patient.getLastName());
            pstmt.setDate(3, Date.valueOf(patient.getDateOfBirth()));
            pstmt.setString(4, patient.getGender());
            pstmt.setString(5, patient.getPhone());
            pstmt.setString(6, patient.getEmail());
            pstmt.setString(7, patient.getAddress());
            pstmt.setString(8, patient.getBloodGroup());
            pstmt.setDate(9, Date.valueOf(java.time.LocalDate.now()));

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error adding patient: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * UPDATE an existing patient
     * @param patient The patient with updated information
     * @return true if successful, false otherwise
     */
    public boolean updatePatient(Patient patient) {
        String sql = "UPDATE patients SET first_name=?, last_name=?, date_of_birth=?, " +
                "gender=?, phone=?, email=?, address=?, blood_group=? WHERE patient_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patient.getFirstName());
            pstmt.setString(2, patient.getLastName());
            pstmt.setDate(3, Date.valueOf(patient.getDateOfBirth()));
            pstmt.setString(4, patient.getGender());
            pstmt.setString(5, patient.getPhone());
            pstmt.setString(6, patient.getEmail());
            pstmt.setString(7, patient.getAddress());
            pstmt.setString(8, patient.getBloodGroup());
            pstmt.setInt(9, patient.getPatientId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Patient updated successfully!");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error updating patient: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * DELETE a patient from database
     * @param patientId The ID of patient to delete
     * @return true if successful, false otherwise
     */
    public boolean deletePatient(int patientId) {
        String sql = "DELETE FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Patient deleted successfully!");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error deleting patient: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * SEARCH patients by name
     * @param searchTerm The name to search for
     * @return List of matching patients
     */
    public List<Patient> searchPatients(String searchTerm) {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE first_name LIKE ? OR last_name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Patient patient = extractPatientFromResultSet(rs);
                patients.add(patient);
            }

            System.out.println("✅ Found " + patients.size() + " matching patients");

        } catch (SQLException e) {
            System.err.println("❌ Error searching patients: " + e.getMessage());
            e.printStackTrace();
        }

        return patients;
    }

    /**
     * Helper method: Convert ResultSet row to Patient object
     * This avoids repeating code
     */
    private Patient extractPatientFromResultSet(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientId(rs.getInt("patient_id"));
        patient.setFirstName(rs.getString("first_name"));
        patient.setLastName(rs.getString("last_name"));

        // Convert SQL Date to LocalDate
        Date dobDate = rs.getDate("date_of_birth");
        if (dobDate != null) {
            patient.setDateOfBirth(dobDate.toLocalDate());
        }

        patient.setGender(rs.getString("gender"));
        patient.setPhone(rs.getString("phone"));
        patient.setEmail(rs.getString("email"));
        patient.setAddress(rs.getString("address"));
        patient.setBloodGroup(rs.getString("blood_group"));

        return patient;
    }
}