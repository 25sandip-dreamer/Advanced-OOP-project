package com.healthcare.service;

import java.io.*;
import java.util.Properties;

/**
 * EmailConfig - Email Configuration Manager
 * Manages email settings with support for configuration files
 * Makes it easier to change email settings without modifying code
 */
public class EmailConfig {

    private static final String CONFIG_FILE = "email.properties";
    private static Properties config = new Properties();
    private static boolean configLoaded = false;

    // Default configuration
    private static final String DEFAULT_SMTP_HOST = "smtp.gmail.com";
    private static final String DEFAULT_SMTP_PORT = "587";
    private static final String DEFAULT_FROM_EMAIL = "your-email@gmail.com";
    private static final String DEFAULT_FROM_PASSWORD = "your-app-password";
    private static final String DEFAULT_SYSTEM_NAME = "Healthcare Management System";
    private static final boolean DEFAULT_EMAIL_ENABLED = true;
    private static final boolean DEFAULT_USE_TLS = true;

    /**
     * Load configuration from file or use defaults
     */
    public static void loadConfig() {
        if (configLoaded) {
            return;
        }

        try {
            File configFile = new File(CONFIG_FILE);

            if (configFile.exists()) {
                // Load from file
                FileInputStream input = new FileInputStream(configFile);
                config.load(input);
                input.close();
                System.out.println("✅ Email configuration loaded from: " + CONFIG_FILE);
            } else {
                // Create default configuration file
                createDefaultConfigFile();
                System.out.println("⚠️ Created default email configuration file: " + CONFIG_FILE);
                System.out.println("   Please edit this file with your email settings!");
            }

            configLoaded = true;

        } catch (IOException e) {
            System.err.println("⚠️ Could not load email config file, using defaults");
            loadDefaultConfig();
        }
    }

    /**
     * Create default configuration file
     */
    private static void createDefaultConfigFile() {
        try {
            FileOutputStream output = new FileOutputStream(CONFIG_FILE);

            config.setProperty("smtp.host", DEFAULT_SMTP_HOST);
            config.setProperty("smtp.port", DEFAULT_SMTP_PORT);
            config.setProperty("email.from", DEFAULT_FROM_EMAIL);
            config.setProperty("email.password", DEFAULT_FROM_PASSWORD);
            config.setProperty("system.name", DEFAULT_SYSTEM_NAME);
            config.setProperty("email.enabled", String.valueOf(DEFAULT_EMAIL_ENABLED));
            config.setProperty("smtp.use.tls", String.valueOf(DEFAULT_USE_TLS));

            // Save with helpful comments
            config.store(output,
                    "Healthcare System Email Configuration\n" +
                            "# INSTRUCTIONS:\n" +
                            "# 1. Set email.from to your Gmail address\n" +
                            "# 2. Set email.password to your Gmail App Password (NOT your regular password!)\n" +
                            "# 3. To get App Password: https://myaccount.google.com/apppasswords\n" +
                            "# 4. Set email.enabled to false to disable all emails during testing\n" +
                            "# 5. Save this file and restart the application");

            output.close();
            configLoaded = true;

        } catch (IOException e) {
            System.err.println("❌ Could not create config file: " + e.getMessage());
            loadDefaultConfig();
        }
    }

    /**
     * Load default configuration into memory
     */
    private static void loadDefaultConfig() {
        config.setProperty("smtp.host", DEFAULT_SMTP_HOST);
        config.setProperty("smtp.port", DEFAULT_SMTP_PORT);
        config.setProperty("email.from", DEFAULT_FROM_EMAIL);
        config.setProperty("email.password", DEFAULT_FROM_PASSWORD);
        config.setProperty("system.name", DEFAULT_SYSTEM_NAME);
        config.setProperty("email.enabled", String.valueOf(DEFAULT_EMAIL_ENABLED));
        config.setProperty("smtp.use.tls", String.valueOf(DEFAULT_USE_TLS));
        configLoaded = true;
    }

    // ============ GETTERS ============

    public static String getSmtpHost() {
        loadConfig();
        return config.getProperty("smtp.host", DEFAULT_SMTP_HOST);
    }

    public static String getSmtpPort() {
        loadConfig();
        return config.getProperty("smtp.port", DEFAULT_SMTP_PORT);
    }

    public static String getFromEmail() {
        loadConfig();
        return config.getProperty("email.from", DEFAULT_FROM_EMAIL);
    }

    public static String getFromPassword() {
        loadConfig();
        return config.getProperty("email.password", DEFAULT_FROM_PASSWORD);
    }

    public static String getSystemName() {
        loadConfig();
        return config.getProperty("system.name", DEFAULT_SYSTEM_NAME);
    }

    public static boolean isEmailEnabled() {
        loadConfig();
        return Boolean.parseBoolean(config.getProperty("email.enabled", String.valueOf(DEFAULT_EMAIL_ENABLED)));
    }

    public static boolean useTLS() {
        loadConfig();
        return Boolean.parseBoolean(config.getProperty("smtp.use.tls", String.valueOf(DEFAULT_USE_TLS)));
    }

    /**
     * Check if configuration is valid (not using defaults)
     */
    public static boolean isConfigured() {
        loadConfig();
        String fromEmail = getFromEmail();
        String password = getFromPassword();

        return !fromEmail.equals(DEFAULT_FROM_EMAIL) &&
                !password.equals(DEFAULT_FROM_PASSWORD) &&
                !fromEmail.contains("your-email");
    }

    /**
     * Get all configuration as Properties (for JavaMail)
     */
    public static Properties getMailProperties() {
        loadConfig();

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(useTLS()));
        props.put("mail.smtp.host", getSmtpHost());
        props.put("mail.smtp.port", getSmtpPort());
        props.put("mail.smtp.ssl.trust", getSmtpHost());
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        return props;
    }

    /**
     * Display current configuration (hides password)
     */
    public static void displayConfig() {
        loadConfig();

        System.out.println("\n📧 Email Configuration:");
        System.out.println("==================================");
        System.out.println("SMTP Host: " + getSmtpHost());
        System.out.println("SMTP Port: " + getSmtpPort());
        System.out.println("From Email: " + getFromEmail());
        System.out.println("Password: " + (getFromPassword().equals(DEFAULT_FROM_PASSWORD) ? "NOT SET" : "***********"));
        System.out.println("System Name: " + getSystemName());
        System.out.println("Email Enabled: " + isEmailEnabled());
        System.out.println("Use TLS: " + useTLS());
        System.out.println("Configured: " + (isConfigured() ? "✅ YES" : "❌ NO - Please set your email and password!"));
        System.out.println("==================================\n");
    }

    /**
     * Interactive configuration setup (console-based)
     */
    public static void setupWizard() {
        System.out.println("\n🔧 EMAIL CONFIGURATION WIZARD");
        System.out.println("==================================");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.print("\nEnter your Gmail address: ");
            String email = reader.readLine();

            System.out.print("Enter your Gmail App Password: ");
            String password = reader.readLine();

            System.out.print("Enter system name (default: Healthcare Management System): ");
            String systemName = reader.readLine();
            if (systemName.trim().isEmpty()) {
                systemName = DEFAULT_SYSTEM_NAME;
            }

            System.out.print("Enable emails? (yes/no, default: yes): ");
            String enabled = reader.readLine();
            boolean emailEnabled = enabled.isEmpty() || enabled.toLowerCase().startsWith("y");

            // Save configuration
            config.setProperty("email.from", email);
            config.setProperty("email.password", password);
            config.setProperty("system.name", systemName);
            config.setProperty("email.enabled", String.valueOf(emailEnabled));

            FileOutputStream output = new FileOutputStream(CONFIG_FILE);
            config.store(output, "Healthcare System Email Configuration");
            output.close();

            System.out.println("\n✅ Configuration saved to: " + CONFIG_FILE);
            System.out.println("📧 Email notifications are " + (emailEnabled ? "ENABLED" : "DISABLED"));

            // Test configuration
            System.out.print("\nWould you like to send a test email? (yes/no): ");
            String test = reader.readLine();
            if (test.toLowerCase().startsWith("y")) {
                System.out.print("Enter test email address: ");
                String testEmail = reader.readLine();

                // Import and test
                com.healthcare.service.EmailService.testEmailConfiguration(testEmail);
            }

        } catch (IOException e) {
            System.err.println("❌ Error during setup: " + e.getMessage());
        }
    }

    /**
     * Quick setup for Gmail
     */
    public static void quickSetupGmail(String email, String appPassword) {
        config.setProperty("smtp.host", "smtp.gmail.com");
        config.setProperty("smtp.port", "587");
        config.setProperty("email.from", email);
        config.setProperty("email.password", appPassword);
        config.setProperty("smtp.use.tls", "true");
        config.setProperty("email.enabled", "true");

        try {
            FileOutputStream output = new FileOutputStream(CONFIG_FILE);
            config.store(output, "Healthcare System Email Configuration");
            output.close();
            configLoaded = true;
            System.out.println("✅ Gmail configuration saved!");
        } catch (IOException e) {
            System.err.println("❌ Could not save configuration: " + e.getMessage());
        }
    }

    /**
     * Quick setup for Outlook/Office 365
     */
    public static void quickSetupOutlook(String email, String password) {
        config.setProperty("smtp.host", "smtp.office365.com");
        config.setProperty("smtp.port", "587");
        config.setProperty("email.from", email);
        config.setProperty("email.password", password);
        config.setProperty("smtp.use.tls", "true");
        config.setProperty("email.enabled", "true");

        try {
            FileOutputStream output = new FileOutputStream(CONFIG_FILE);
            config.store(output, "Healthcare System Email Configuration");
            output.close();
            configLoaded = true;
            System.out.println("✅ Outlook configuration saved!");
        } catch (IOException e) {
            System.err.println("❌ Could not save configuration: " + e.getMessage());
        }
    }

    /**
     * Main method - run configuration wizard
     */
    public static void main(String[] args) {
        System.out.println("🏥 HEALTHCARE SYSTEM - EMAIL CONFIGURATION");

        if (args.length > 0 && args[0].equals("--wizard")) {
            setupWizard();
        } else if (args.length >= 2) {
            // Command line setup: java EmailConfig gmail@example.com app-password
            quickSetupGmail(args[0], args[1]);
            System.out.println("✅ Email configured via command line!");
        } else {
            displayConfig();
            System.out.println("\nUsage:");
            System.out.println("  java EmailConfig --wizard              (Interactive setup)");
            System.out.println("  java EmailConfig email@gmail.com pass  (Quick setup)");
        }
    }
}