package com.healthcare.test;

import com.healthcare.service.EmailService;
import com.healthcare.service.EmailConfig;

/**
 * TestEmail - Email Configuration Testing Tool
 * Run this to verify your email setup is working correctly
 *
 * LOCATION: Create this file at:
 * src/com/healthcare/test/TestEmail.java
 *
 * HOW TO RUN:
 * 1. Right-click on this file in your IDE
 * 2. Select "Run As" -> "Java Application"
 * OR
 * 3. From terminal: java com.healthcare.test.TestEmail
 */
public class TestEmail {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   🧪 EMAIL CONFIGURATION TEST TOOL            ║");
        System.out.println("║   Healthcare Management System                 ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        // Step 1: Display current configuration
        System.out.println("Step 1: Checking Email Configuration...");
        System.out.println("──────────────────────────────────────────────────");
        EmailConfig.displayConfig();

        // Step 2: Check if configured
        if (!EmailConfig.isConfigured()) {
            System.out.println("❌ EMAIL NOT CONFIGURED!");
            System.out.println("\n📝 Please configure your email first:");
            System.out.println("   Option 1: Run configuration wizard:");
            System.out.println("   java com.healthcare.service.EmailConfig --wizard\n");
            System.out.println("   Option 2: Edit email.properties file manually\n");
            System.out.println("   Option 3: Use quick setup:");
            System.out.println("   java com.healthcare.service.EmailConfig your-email@gmail.com your-app-password\n");
            return;
        }

        // Step 3: Ask for test email address
        System.out.println("\nStep 2: Send Test Email");
        System.out.println("──────────────────────────────────────────────────");

        String testEmail;

        // Check if email provided as command line argument
        if (args.length > 0) {
            testEmail = args[0];
            System.out.println("📧 Using email from command line: " + testEmail);
        } else {
            // Use configured email as default
            testEmail = EmailConfig.getFromEmail();
            System.out.println("📧 Sending test email to: " + testEmail);
            System.out.println("   (You can specify a different email as argument)");
            System.out.println("   Example: java TestEmail another@email.com\n");
        }

        // Step 4: Send test email
        System.out.println("\n🚀 Sending test email...");
        System.out.println("──────────────────────────────────────────────────");

        boolean success = EmailService.testEmailConfiguration(testEmail);

        // Step 5: Results
        System.out.println("\n╔════════════════════════════════════════════════╗");
        if (success) {
            System.out.println("║   ✅ SUCCESS! Email Sent Successfully!        ║");
            System.out.println("╚════════════════════════════════════════════════╝");
            System.out.println("\n📬 CHECK YOUR INBOX!");
            System.out.println("   Email sent to: " + testEmail);
            System.out.println("   Subject: Test Email - Healthcare System");
            System.out.println("\n💡 TIP: Check your spam folder if you don't see it");
            System.out.println("\n✅ Your email configuration is working correctly!");
            System.out.println("   You can now integrate emails into your application.");
        } else {
            System.out.println("║   ❌ FAILED! Email Could Not Be Sent         ║");
            System.out.println("╚════════════════════════════════════════════════╝");
            System.out.println("\n🔍 TROUBLESHOOTING:");
            System.out.println("   1. Check your email and password in email.properties");
            System.out.println("   2. Make sure you're using Gmail App Password (not regular password)");
            System.out.println("   3. Verify your internet connection");
            System.out.println("   4. Check the error message above for details");
            System.out.println("\n📚 HELPFUL LINKS:");
            System.out.println("   Gmail App Password: https://myaccount.google.com/apppasswords");
            System.out.println("   Setup Guide: See 'Email Setup Guide' documentation");
        }
        System.out.println("\n══════════════════════════════════════════════════\n");

        // Step 6: Additional tests
        if (success) {
            System.out.println("🎯 NEXT STEPS:");
            System.out.println("   1. ✅ Email configuration is working");
            System.out.println("   2. Add integration code to your controllers");
            System.out.println("   3. Test each email type:");
            System.out.println("      - Register new user (welcome email)");
            System.out.println("      - Prescribe medication (prescription email)");
            System.out.println("      - Complete assessment (notification email)");
            System.out.println("   4. (Optional) Set up EmailScheduler for daily reminders");
            System.out.println("\n💡 Want to test all email types? Run comprehensive tests:");
            System.out.println("   java com.healthcare.test.TestEmail --all\n");

            // If --all flag is provided, run all tests
            if (args.length > 0 && args[args.length - 1].equals("--all")) {
                runComprehensiveTests(testEmail);
            }
        }
    }

    /**
     * Run comprehensive email tests (all email types)
     */
    private static void runComprehensiveTests(String testEmail) {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║   🧪 COMPREHENSIVE EMAIL TESTS                ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        int passed = 0;
        int failed = 0;

        // Test 1: Welcome Email
        System.out.println("Test 1: Welcome Email");
        System.out.println("──────────────────────────────────────────────────");
        if (EmailService.sendWelcomeEmail(testEmail, "Test User", "Patient")) {
            System.out.println("✅ PASSED\n");
            passed++;
        } else {
            System.out.println("❌ FAILED\n");
            failed++;
        }

        sleep(2000); // Wait 2 seconds between emails

        // Test 2: Medication Prescribed
        System.out.println("Test 2: Medication Prescribed Email");
        System.out.println("──────────────────────────────────────────────────");
        if (EmailService.sendMedicationPrescribedEmail(
                testEmail,
                "Test Patient",
                "Test Medication",
                "10mg",
                "Twice daily",
                "Take with food")) {
            System.out.println("✅ PASSED\n");
            passed++;
        } else {
            System.out.println("❌ FAILED\n");
            failed++;
        }

        sleep(2000);

        // Test 3: Assessment Reminder
        System.out.println("Test 3: Assessment Reminder Email");
        System.out.println("──────────────────────────────────────────────────");
        if (EmailService.sendAssessmentReminderEmail(
                testEmail,
                "Test Patient",
                "Test Medication",
                "Today")) {
            System.out.println("✅ PASSED\n");
            passed++;
        } else {
            System.out.println("❌ FAILED\n");
            failed++;
        }

        sleep(2000);

        // Test 4: Assessment Completed
        System.out.println("Test 4: Assessment Completed Email");
        System.out.println("──────────────────────────────────────────────────");
        if (EmailService.sendAssessmentCompletedEmail(
                testEmail,
                "Test Doctor",
                "Test Patient",
                "Test Medication",
                7)) {
            System.out.println("✅ PASSED\n");
            passed++;
        } else {
            System.out.println("❌ FAILED\n");
            failed++;
        }

        sleep(2000);

        // Test 5: Critical Alert
        System.out.println("Test 5: Critical Alert Email");
        System.out.println("──────────────────────────────────────────────────");
        if (EmailService.sendCriticalAlertEmail(
                testEmail,
                "Test Doctor",
                "Test Patient",
                "Test Medication",
                "HIGH",
                "This is a test critical alert with important information.")) {
            System.out.println("✅ PASSED\n");
            passed++;
        } else {
            System.out.println("❌ FAILED\n");
            failed++;
        }

        sleep(2000);

        // Test 6: Overdue Warning
        System.out.println("Test 6: Overdue Assessment Warning");
        System.out.println("──────────────────────────────────────────────────");
        if (EmailService.sendOverdueAssessmentEmail(
                testEmail,
                "Test Patient",
                "Test Medication",
                3)) {
            System.out.println("✅ PASSED\n");
            passed++;
        } else {
            System.out.println("❌ FAILED\n");
            failed++;
        }

        // Summary
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║   📊 TEST RESULTS                             ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println("   Total Tests: " + (passed + failed));
        System.out.println("   ✅ Passed: " + passed);
        System.out.println("   ❌ Failed: " + failed);
        System.out.println("\n📬 Check your inbox at: " + testEmail);
        System.out.println("   You should have received " + passed + " test emails");
        System.out.println("\n══════════════════════════════════════════════════\n");
    }

    /**
     * Helper method to pause between tests
     */
    private static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            // Ignore
        }
    }
}