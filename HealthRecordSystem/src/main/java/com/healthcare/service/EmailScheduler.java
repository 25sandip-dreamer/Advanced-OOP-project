package com.healthcare.service;

import com.healthcare.database.AssessmentDAO;
import com.healthcare.database.MedicationDAO;
import com.healthcare.database.PatientDAO;
import com.healthcare.model.MedicationAssessment;
import com.healthcare.model.Patient;
import com.healthcare.model.PatientMedication;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * EmailScheduler - Automated Email Scheduler
 * Sends daily reminders for pending assessments and overdue warnings
 * Runs automatically in the background
 */
public class EmailScheduler {

    private Timer timer;
    private AssessmentDAO assessmentDAO;
    private PatientDAO patientDAO;
    private MedicationDAO medicationDAO;

    private boolean isRunning = false;

    /**
     * Constructor
     */
    public EmailScheduler() {
        this.assessmentDAO = new AssessmentDAO();
        this.patientDAO = new PatientDAO();
        this.medicationDAO = new MedicationDAO();
    }

    /**
     * Start the email scheduler
     * Runs daily at 9:00 AM
     */
    public void startScheduler() {
        if (isRunning) {
            System.out.println("⚠️ Email scheduler is already running!");
            return;
        }

        timer = new Timer("EmailScheduler-Thread", true); // Daemon thread

        // Calculate delay until 9 AM
        long initialDelay = calculateDelayUntil9AM();

        // Run every 24 hours (86400000 milliseconds)
        long dailyInterval = 24 * 60 * 60 * 1000;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("\n========================================");
                System.out.println("📧 DAILY EMAIL CHECK - " + LocalDateTime.now());
                System.out.println("========================================");
                sendDailyReminders();
            }
        }, initialDelay, dailyInterval);

        isRunning = true;

        System.out.println("✅ Email scheduler started successfully!");
        System.out.println("⏰ First run scheduled for: " + getNextRunTime());
        System.out.println("📅 Will run daily at 9:00 AM");
    }

    /**
     * Calculate milliseconds until next 9 AM
     */
    private long calculateDelayUntil9AM() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next9AM = now.toLocalDate().atTime(9, 0);

        // If it's already past 9 AM today, schedule for 9 AM tomorrow
        if (now.isAfter(next9AM)) {
            next9AM = next9AM.plusDays(1);
        }

        long delay = ChronoUnit.MILLIS.between(now, next9AM);

        // For testing: run immediately if within 1 minute of starting
        // Comment this out in production
        if (delay > 60000) { // More than 1 minute away
            // For testing, run after 10 seconds instead of waiting until 9 AM
            // return 10000; // Uncomment for immediate testing
        }

        return delay;
    }

    /**
     * Get next scheduled run time
     */
    private String getNextRunTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next9AM = now.toLocalDate().atTime(9, 0);

        if (now.isAfter(next9AM)) {
            next9AM = next9AM.plusDays(1);
        }

        return next9AM.toString();
    }

    /**
     * Send daily email reminders
     * This is the main method that runs daily
     */
    private void sendDailyReminders() {
        try {
            int remindersSent = 0;
            int overdueWarningsSent = 0;
            int errors = 0;

            // Get all pending assessments
            List<MedicationAssessment> pendingAssessments = assessmentDAO.getPendingAssessments();

            System.out.println("📋 Found " + pendingAssessments.size() + " pending assessments");

            LocalDate today = LocalDate.now();

            for (MedicationAssessment assessment : pendingAssessments) {
                try {
                    // Get patient information
                    Patient patient = getPatientForAssessment(assessment);

                    if (patient == null || patient.getEmail() == null) {
                        System.out.println("⚠️ No email found for patient: " + assessment.getPatientName());
                        continue;
                    }

                    LocalDate dueDate = assessment.getAssessmentDate();
                    long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);

                    // Send reminder if due today or tomorrow
                    if (daysUntilDue >= 0 && daysUntilDue <= 1) {
                        boolean sent = EmailService.sendAssessmentReminderEmail(
                                patient.getEmail(),
                                patient.getFullName(),
                                assessment.getMedicationName(),
                                dueDate.toString()
                        );

                        if (sent) {
                            remindersSent++;
                            System.out.println("  ✅ Reminder sent to: " + patient.getFullName());
                        } else {
                            errors++;
                        }
                    }
                    // Send overdue warning if past due date
                    else if (daysUntilDue < 0) {
                        int daysOverdue = (int) Math.abs(daysUntilDue);

                        // Send overdue warnings on days 1, 3, 7, and every 7 days after
                        if (daysOverdue == 1 || daysOverdue == 3 || daysOverdue == 7 || daysOverdue % 7 == 0) {
                            boolean sent = EmailService.sendOverdueAssessmentEmail(
                                    patient.getEmail(),
                                    patient.getFullName(),
                                    assessment.getMedicationName(),
                                    daysOverdue
                            );

                            if (sent) {
                                overdueWarningsSent++;
                                System.out.println("  ⚠️ Overdue warning sent to: " + patient.getFullName() + " (" + daysOverdue + " days)");
                            } else {
                                errors++;
                            }
                        }
                    }

                } catch (Exception e) {
                    errors++;
                    System.err.println("  ❌ Error processing assessment: " + e.getMessage());
                }
            }

            // Summary
            System.out.println("\n📊 DAILY EMAIL SUMMARY:");
            System.out.println("  ✅ Reminders sent: " + remindersSent);
            System.out.println("  ⚠️ Overdue warnings sent: " + overdueWarningsSent);
            System.out.println("  ❌ Errors: " + errors);
            System.out.println("  📅 Next run: Tomorrow at 9:00 AM");
            System.out.println("========================================\n");

        } catch (Exception e) {
            System.err.println("❌ Critical error in email scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get patient information for an assessment
     * Helper method to find patient by name
     */
    private Patient getPatientForAssessment(MedicationAssessment assessment) {
        try {
            // Get patient by name from the assessment
            if (assessment.getPatientName() == null || assessment.getPatientName().isEmpty()) {
                System.err.println("⚠️ Assessment has no patient name");
                return null;
            }

            // Try to get patient by name
            List<Patient> allPatients = patientDAO.getAllPatients();
            for (Patient p : allPatients) {
                if (p.getFullName().equalsIgnoreCase(assessment.getPatientName())) {
                    return p;
                }
            }

            System.err.println("⚠️ Could not find patient: " + assessment.getPatientName());
            return null;

        } catch (Exception e) {
            System.err.println("❌ Error getting patient info: " + e.getMessage());
            return null;
        }
    }

    /**
     * Stop the email scheduler
     */
    public void stopScheduler() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            isRunning = false;
            System.out.println("⏹️ Email scheduler stopped");
        } else {
            System.out.println("⚠️ Email scheduler was not running");
        }
    }

    /**
     * Check if scheduler is running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Force run the daily check immediately (for testing)
     */
    public void runNow() {
        System.out.println("🔄 Running email check manually...");
        sendDailyReminders();
    }

    /**
     * Get scheduler status
     */
    public void printStatus() {
        System.out.println("\n📧 EMAIL SCHEDULER STATUS:");
        System.out.println("==================================");
        System.out.println("Status: " + (isRunning ? "✅ RUNNING" : "⏹️ STOPPED"));

        if (isRunning) {
            System.out.println("Next run: " + getNextRunTime());
            System.out.println("Schedule: Daily at 9:00 AM");
        }

        System.out.println("Email enabled: " + (EmailConfig.isEmailEnabled() ? "✅ YES" : "❌ NO"));
        System.out.println("==================================\n");
    }

    /**
     * Test method - main entry point for testing
     */
    public static void main(String[] args) {
        System.out.println("🧪 TESTING EMAIL SCHEDULER");
        System.out.println("==================================\n");

        EmailScheduler scheduler = new EmailScheduler();

        if (args.length > 0 && args[0].equals("--run-now")) {
            // Run check immediately
            System.out.println("▶️ Running daily check immediately...\n");
            scheduler.runNow();
        } else if (args.length > 0 && args[0].equals("--start")) {
            // Start scheduler
            scheduler.startScheduler();
            scheduler.printStatus();

            // Keep program running
            System.out.println("Press Ctrl+C to stop the scheduler...");
            try {
                Thread.currentThread().join(); // Wait forever
            } catch (InterruptedException e) {
                System.out.println("\n⏹️ Scheduler interrupted, stopping...");
                scheduler.stopScheduler();
            }
        } else {
            // Print usage
            System.out.println("Usage:");
            System.out.println("  java EmailScheduler --run-now    (Test immediately)");
            System.out.println("  java EmailScheduler --start      (Start scheduler)");
            System.out.println("\nOr integrate into MainApp.java for automatic startup");
        }
    }
}