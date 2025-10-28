package com.healthcare.service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * EmailService - Email Notification System
 * Sends automated emails for various healthcare events
 * Supports Gmail, Outlook, and other SMTP providers
 */
public class EmailService {

    // Email configuration
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "rudrobasak75@gmail.com"; // CHANGE THIS
    private static final String EMAIL_PASSWORD = "oeuw gsri ciqs cyif"; // CHANGE THIS
    private static final String SYSTEM_NAME = "Healthcare Management System";

    // Enable/disable emails (useful for testing)
    private static boolean EMAIL_ENABLED = true;

    /**
     * Send email with HTML content
     */
    public static boolean sendEmail(String toEmail, String subject, String htmlBody) {
        if (!EMAIL_ENABLED) {
            System.out.println("📧 [EMAIL DISABLED] Would send to: " + toEmail);
            System.out.println("   Subject: " + subject);
            return true;
        }

        try {
            // Setup mail server properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            // Create authenticator
            Authenticator auth = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            };

            // Create session
            Session session = Session.getInstance(props, auth);

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM, SYSTEM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            // Set HTML content
            message.setContent(htmlBody, "text/html; charset=utf-8");

            // Send email
            Transport.send(message);

            System.out.println("✅ Email sent successfully to: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Failed to send email to: " + toEmail);
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ============ NOTIFICATION TEMPLATES ============

    /**
     * Send notification when medication is prescribed
     */
    public static boolean sendMedicationPrescribedEmail(
            String patientEmail,
            String patientName,
            String medicationName,
            String dosage,
            String frequency,
            String instructions) {

        String subject = "💊 New Medication Prescribed - " + medicationName;

        String htmlBody = String.format(
                getEmailTemplate(),
                "New Medication Prescribed",
                "#4CAF50",
                "💊",
                String.format(
                        "<p>Dear %s,</p>" +
                                "<p>A new medication has been prescribed for you:</p>" +
                                "<div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                                "    <h3 style='margin-top: 0; color: #333;'>%s %s</h3>" +
                                "    <p><strong>Frequency:</strong> %s</p>" +
                                "    <p><strong>Instructions:</strong> %s</p>" +
                                "</div>" +
                                "<p><strong>Important:</strong> You will receive an assessment questionnaire in 7 days to check how the medication is working.</p>" +
                                "<p>Please take your medication as prescribed and contact your doctor if you have any concerns.</p>",
                        patientName,
                        medicationName,
                        dosage,
                        frequency,
                        instructions != null ? instructions : "Take as directed"
                )
        );

        return sendEmail(patientEmail, subject, htmlBody);
    }

    /**
     * Send 7-day assessment reminder
     */
    public static boolean sendAssessmentReminderEmail(
            String patientEmail,
            String patientName,
            String medicationName,
            String dueDate) {

        String subject = "📋 Medication Assessment Due - " + medicationName;

        String htmlBody = String.format(
                getEmailTemplate(),
                "Medication Assessment Reminder",
                "#FF9800",
                "📋",
                String.format(
                        "<p>Dear %s,</p>" +
                                "<p>It's time for your 7-day medication assessment for <strong>%s</strong>.</p>" +
                                "<div style='background-color: #FFF3E0; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #FF9800;'>" +
                                "    <h3 style='margin-top: 0; color: #333;'>⏰ Assessment Due: %s</h3>" +
                                "    <p>Please complete your questionnaire to help your doctor monitor your treatment.</p>" +
                                "</div>" +
                                "<p><strong>Why is this important?</strong></p>" +
                                "<ul>" +
                                "    <li>Helps your doctor ensure the medication is working</li>" +
                                "    <li>Allows early detection of side effects</li>" +
                                "    <li>Ensures the best possible treatment outcome</li>" +
                                "</ul>" +
                                "<p>Please log in to the Healthcare System to complete your assessment.</p>",
                        patientName,
                        medicationName,
                        dueDate
                )
        );

        return sendEmail(patientEmail, subject, htmlBody);
    }

    /**
     * Send notification when patient completes assessment (to doctor)
     */
    public static boolean sendAssessmentCompletedEmail(
            String doctorEmail,
            String doctorName,
            String patientName,
            String medicationName,
            int assessmentDay) {

        String subject = "✅ Patient Assessment Completed - " + patientName;

        String htmlBody = String.format(
                getEmailTemplate(),
                "Assessment Completed",
                "#2196F3",
                "✅",
                String.format(
                        "<p>Dear Dr. %s,</p>" +
                                "<p>Your patient <strong>%s</strong> has completed their Day %d assessment for <strong>%s</strong>.</p>" +
                                "<div style='background-color: #E3F2FD; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                                "    <h3 style='margin-top: 0; color: #333;'>📊 Assessment Ready for Review</h3>" +
                                "    <p><strong>Patient:</strong> %s</p>" +
                                "    <p><strong>Medication:</strong> %s</p>" +
                                "    <p><strong>Assessment Day:</strong> Day %d</p>" +
                                "</div>" +
                                "<p>Please review the patient's responses and AI analysis in the Doctor Dashboard.</p>" +
                                "<p><em>AI-powered analysis is available to help identify any concerns.</em></p>",
                        doctorName,
                        patientName,
                        assessmentDay,
                        medicationName,
                        patientName,
                        medicationName,
                        assessmentDay
                )
        );

        return sendEmail(doctorEmail, subject, htmlBody);
    }

    /**
     * Send critical alert when AI detects issues
     */
    public static boolean sendCriticalAlertEmail(
            String doctorEmail,
            String doctorName,
            String patientName,
            String medicationName,
            String riskLevel,
            String concerns) {

        String subject = "🚨 URGENT: Critical Patient Alert - " + patientName;

        String htmlBody = String.format(
                getEmailTemplate(),
                "URGENT: Critical Patient Alert",
                "#D32F2F",
                "🚨",
                String.format(
                        "<p>Dear Dr. %s,</p>" +
                                "<div style='background-color: #FFEBEE; padding: 20px; border-radius: 8px; margin: 20px 0; border: 2px solid #D32F2F;'>" +
                                "    <h3 style='margin-top: 0; color: #D32F2F;'>⚠️ IMMEDIATE ATTENTION REQUIRED</h3>" +
                                "    <p><strong>Patient:</strong> %s</p>" +
                                "    <p><strong>Medication:</strong> %s</p>" +
                                "    <p><strong>Risk Level:</strong> <span style='color: #D32F2F; font-weight: bold;'>%s</span></p>" +
                                "</div>" +
                                "<p><strong>AI Analysis Summary:</strong></p>" +
                                "<p>%s</p>" +
                                "<p style='color: #D32F2F; font-weight: bold;'>⚠️ This patient requires immediate follow-up. Please review their assessment in the Doctor Dashboard as soon as possible.</p>",
                        doctorName,
                        patientName,
                        medicationName,
                        riskLevel,
                        concerns
                )
        );

        return sendEmail(doctorEmail, subject, htmlBody);
    }

    /**
     * Send overdue assessment warning
     */
    public static boolean sendOverdueAssessmentEmail(
            String patientEmail,
            String patientName,
            String medicationName,
            int daysOverdue) {

        String subject = "⚠️ OVERDUE: Medication Assessment - " + medicationName;

        String htmlBody = String.format(
                getEmailTemplate(),
                "Overdue Assessment Warning",
                "#f44336",
                "⚠️",
                String.format(
                        "<p>Dear %s,</p>" +
                                "<div style='background-color: #FFEBEE; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #f44336;'>" +
                                "    <h3 style='margin-top: 0; color: #D32F2F;'>⚠️ Your Assessment is %d Days Overdue</h3>" +
                                "    <p><strong>Medication:</strong> %s</p>" +
                                "</div>" +
                                "<p><strong>Why this matters:</strong></p>" +
                                "<p>Your doctor needs your feedback to ensure your medication is safe and effective. " +
                                "Delayed assessments can lead to undetected side effects or reduced treatment effectiveness.</p>" +
                                "<p><strong>Please complete your assessment today.</strong></p>" +
                                "<p>If you're experiencing any issues or concerns, please contact your doctor immediately.</p>",
                        patientName,
                        daysOverdue,
                        medicationName
                )
        );

        return sendEmail(patientEmail, subject, htmlBody);
    }

    /**
     * Send welcome email to new users
     */
    public static boolean sendWelcomeEmail(
            String userEmail,
            String userName,
            String role) {

        String subject = "🎉 Welcome to Healthcare Management System!";

        String htmlBody = String.format(
                getEmailTemplate(),
                "Welcome!",
                "#667eea",
                "🎉",
                String.format(
                        "<p>Dear %s,</p>" +
                                "<p>Welcome to the Healthcare Management System! Your account has been successfully created.</p>" +
                                "<div style='background-color: #f0f4ff; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                                "    <h3 style='margin-top: 0; color: #333;'>Account Details</h3>" +
                                "    <p><strong>Email:</strong> %s</p>" +
                                "    <p><strong>Role:</strong> %s</p>" +
                                "</div>" +
                                "<p><strong>What's Next?</strong></p>" +
                                "<ul>" +
                                "    <li>Log in to the system using your credentials</li>" +
                                "    <li>Complete your profile information</li>" +
                                "    <li>Explore the features available to you</li>" +
                                "</ul>" +
                                "<p>If you have any questions, please don't hesitate to reach out to our support team.</p>",
                        userName,
                        userEmail,
                        role
                )
        );

        return sendEmail(userEmail, subject, htmlBody);
    }

    // ============ UTILITY METHODS ============

    /**
     * Get professional HTML email template
     */
    private static String getEmailTemplate() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f5f5f5;'>" +
                "    <table width='100%%' cellpadding='0' cellspacing='0' style='background-color: #f5f5f5; padding: 20px;'>" +
                "        <tr>" +
                "            <td align='center'>" +
                "                <table width='600' cellpadding='0' cellspacing='0' style='background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +
                "                    <!-- Header -->" +
                "                    <tr>" +
                "                        <td style='background-color: %2$s; padding: 30px; text-align: center;'>" +
                "                            <h1 style='color: white; margin: 0; font-size: 32px;'>%3$s</h1>" +
                "                            <h2 style='color: white; margin: 10px 0 0 0; font-size: 24px; font-weight: normal;'>%1$s</h2>" +
                "                        </td>" +
                "                    </tr>" +
                "                    <!-- Content -->" +
                "                    <tr>" +
                "                        <td style='padding: 40px; color: #333; font-size: 16px; line-height: 1.6;'>" +
                "                            %4$s" +
                "                        </td>" +
                "                    </tr>" +
                "                    <!-- Footer -->" +
                "                    <tr>" +
                "                        <td style='background-color: #f9f9f9; padding: 20px; text-align: center; color: #666; font-size: 14px;'>" +
                "                            <p style='margin: 0;'>This is an automated message from Healthcare Management System</p>" +
                "                            <p style='margin: 5px 0 0 0;'>Sent on " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")) +
                "                            </p>" +
                "                        </td>" +
                "                    </tr>" +
                "                </table>" +
                "            </td>" +
                "        </tr>" +
                "    </table>" +
                "</body>" +
                "</html>";
    }

    /**
     * Enable/disable email notifications
     */
    public static void setEmailEnabled(boolean enabled) {
        EMAIL_ENABLED = enabled;
        System.out.println(enabled ? "✅ Email notifications ENABLED" : "⚠️ Email notifications DISABLED");
    }

    /**
     * Test email configuration
     */
    public static boolean testEmailConfiguration(String testEmail) {
        System.out.println("📧 Testing email configuration...");

        String subject = "Test Email - Healthcare System";
        String body = String.format(
                getEmailTemplate(),
                "Email Configuration Test",
                "#2196F3",
                "✅",
                "<p>This is a test email from the Healthcare Management System.</p>" +
                        "<p>If you're receiving this, your email configuration is working correctly!</p>" +
                        "<p><strong>Test successful! ✅</strong></p>"
        );

        return sendEmail(testEmail, subject, body);
    }

    // ============ BATCH OPERATIONS ============

    /**
     * Send assessment reminders to all patients with pending assessments
     */
    public static int sendBatchAssessmentReminders() {
        System.out.println("📧 Sending batch assessment reminders...");

        // This would integrate with AssessmentDAO to get pending assessments
        // For now, this is a placeholder

        // Example:
        // List<MedicationAssessment> pending = new AssessmentDAO().getPendingAssessments();
        // int sent = 0;
        // for (MedicationAssessment assessment : pending) {
        //     if (sendAssessmentReminderEmail(...)) {
        //         sent++;
        //     }
        // }
        // return sent;

        return 0;
    }
}