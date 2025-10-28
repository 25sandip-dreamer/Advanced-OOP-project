package com.healthcare.main;

import com.healthcare.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Simple test program to verify our database connection works
 * This is your FIRST working healthcare program!
 */
public class TestConnection {

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("🏥 HEALTHCARE SYSTEM - TEST MODE");
        System.out.println("=================================\n");

        // Test 1: Can we connect to database?
        System.out.println("TEST 1: Testing database connection...");
        Connection conn = DatabaseConnection.getConnection();

        if (conn != null) {
            System.out.println("✅ Test 1 PASSED: Database connection successful!\n");

            // Test 2: Can we read data from database?
            System.out.println("TEST 2: Reading patient data...");
            readPatients(conn);

        } else {
            System.out.println("❌ Test 1 FAILED: Could not connect to database");
            System.out.println("Please check your MySQL setup and password!");
        }

        // Close connection when done
        DatabaseConnection.closeConnection();

        System.out.println("\n=================================");
        System.out.println("🏁 TEST COMPLETED");
        System.out.println("=================================");
    }

    /**
     * Read and display all patients from database
     * This proves we can actually read data!
     */
    private static void readPatients(Connection conn) {
        try {
            // Create a statement to run SQL queries
            Statement stmt = conn.createStatement();

            // Execute SQL query to get all patients
            String sql = "SELECT * FROM patients";
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("📋 Patients in database:");
            System.out.println("----------------------------------------");

            // Loop through results and print each patient
            int count = 0;
            while (rs.next()) {
                count++;
                int id = rs.getInt("patient_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String dob = rs.getString("date_of_birth");
                String phone = rs.getString("phone");

                System.out.println("Patient #" + count);
                System.out.println("  ID: " + id);
                System.out.println("  Name: " + firstName + " " + lastName);
                System.out.println("  Birth Date: " + dob);
                System.out.println("  Phone: " + phone);
                System.out.println("----------------------------------------");
            }

            if (count == 0) {
                System.out.println("⚠️ No patients found in database.");
                System.out.println("💡 Tip: Run the SQL INSERT command from setup guide!");
            } else {
                System.out.println("✅ Test 2 PASSED: Successfully read " + count + " patient(s)!");
            }

            // Close resources
            rs.close();
            stmt.close();

        } catch (Exception e) {
            System.err.println("❌ Test 2 FAILED: Error reading data");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
