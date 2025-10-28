package com.healthcare.controller;

import com.healthcare.service.EmailService;
import com.healthcare.database.UserDAO;
import com.healthcare.model.User;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * LoginController - Beautiful Login Interface
 * Handles user authentication and registration
 */
public class LoginController extends Application {

    private UserDAO userDAO = new UserDAO();
    private User currentUser = null;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("🏥 Healthcare System - Login");

        // Create login screen
        BorderPane mainLayout = createLoginScreen(primaryStage);

        Scene scene = new Scene(mainLayout, 450, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("🔐 Login screen loaded");
    }

    /**
     * Create the login screen layout
     */
    private BorderPane createLoginScreen(Stage stage) {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        // Center: Login Form
        VBox loginBox = createLoginForm(stage);
        layout.setCenter(loginBox);

        return layout;
    }

    /**
     * Create the login form
     */
    private VBox createLoginForm(Stage stage) {
        VBox formBox = new VBox(20);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(40));
        formBox.setMaxWidth(400);
        formBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        // Header
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("🏥");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 48));

        Label systemLabel = new Label("Healthcare System");
        systemLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        systemLabel.setStyle("-fx-text-fill: #667eea;");

        Label subtitleLabel = new Label("Sign in to continue");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setStyle("-fx-text-fill: #666;");

        header.getChildren().addAll(titleLabel, systemLabel, subtitleLabel);

        // Username field
        VBox usernameBox = new VBox(5);
        Label usernameLabel = new Label("Username or Email");
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle("-fx-padding: 12; -fx-font-size: 14; -fx-border-color: #ddd; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");
        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // Password field
        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle("-fx-padding: 12; -fx-font-size: 14; -fx-border-color: #ddd; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");
        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Remember me checkbox (for future enhancement)
        CheckBox rememberMeBox = new CheckBox("Remember me");
        rememberMeBox.setStyle("-fx-text-fill: #666;");

        // Login button
        Button loginButton = new Button("🔐 Sign In");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-font-size: 16; -fx-font-weight: bold; -fx-padding: 15; " +
                "-fx-background-radius: 5; -fx-cursor: hand;");

        // Hover effect
        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle("-fx-background-color: #5568d3; -fx-text-fill: white; " +
                        "-fx-font-size: 16; -fx-font-weight: bold; -fx-padding: 15; " +
                        "-fx-background-radius: 5; -fx-cursor: hand;"));
        loginButton.setOnMouseExited(e ->
                loginButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                        "-fx-font-size: 16; -fx-font-weight: bold; -fx-padding: 15; " +
                        "-fx-background-radius: 5; -fx-cursor: hand;"));

        // Login action
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(),
                passwordField.getText(), stage));

        // Allow Enter key to login
        passwordField.setOnAction(e -> handleLogin(usernameField.getText(),
                passwordField.getText(), stage));

        // Register link
        HBox registerBox = new HBox(5);
        registerBox.setAlignment(Pos.CENTER);
        Label registerLabel = new Label("Don't have an account?");
        registerLabel.setStyle("-fx-text-fill: #666;");
        Hyperlink registerLink = new Hyperlink("Register here");
        registerLink.setStyle("-fx-text-fill: #667eea; -fx-font-weight: bold;");
        registerLink.setOnAction(e -> showRegistrationDialog(stage));
        registerBox.getChildren().addAll(registerLabel, registerLink);

        // Separator
        Separator separator = new Separator();

        // Quick access section (for testing)
        VBox quickAccessBox = new VBox(10);
        quickAccessBox.setAlignment(Pos.CENTER);
        Label quickLabel = new Label("Quick Access (for testing)");
        quickLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #999;");

        Button createAdminButton = new Button("👨‍💼 Create Admin Account");
        createAdminButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; " +
                "-fx-font-size: 12; -fx-padding: 8; -fx-cursor: hand;");
        createAdminButton.setOnAction(e -> createDefaultAdmin());

        quickAccessBox.getChildren().addAll(quickLabel, createAdminButton);

        // Add all elements to form
        formBox.getChildren().addAll(
                header,
                usernameBox,
                passwordBox,
                rememberMeBox,
                loginButton,
                registerBox,
                separator,
                quickAccessBox
        );

        return formBox;
    }

    /**
     * Handle login action
     */
    private void handleLogin(String username, String password, Stage stage) {
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input",
                    "Please enter both username and password!");
            return;
        }

        System.out.println("🔐 Attempting login for user: " + username);

        // Authenticate user
        currentUser = userDAO.authenticateUser(username, password);

        if (currentUser != null) {
            System.out.println("✅ Login successful!");

            // Start session using SessionManager
            com.healthcare.database.SessionManager.getInstance().login(currentUser);

            showAlert(Alert.AlertType.INFORMATION, "Login Successful",
                    "Welcome back, " + currentUser.getFullName() + "!\n" +
                            "Role: " + currentUser.getRole());

            // Open main application based on user role
            openMainApplication(stage, currentUser);

        } else {
            System.out.println("❌ Login failed");
            showAlert(Alert.AlertType.ERROR, "Login Failed",
                    "Invalid username or password!\nPlease try again.");
        }
    }

    /**
     * Open main application after successful login
     */
    private void openMainApplication(Stage loginStage, User user) {
        try {
            // Import the MainApp class
            com.healthcare.main.MainApp mainApp = new com.healthcare.main.MainApp();

            // Create new stage for main application
            Stage mainStage = new Stage();

            // Set title based on user role
            mainStage.setTitle("🏥 Healthcare System - " + user.getRole() + " Dashboard");

            // Start main application
            mainApp.start(mainStage);

            // Close login window
            loginStage.close();

            System.out.println("🚀 Main application opened for: " + user.getFullName());

        } catch (Exception e) {
            System.err.println("❌ Error opening main application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show registration dialog
     */
    private void showRegistrationDialog(Stage ownerStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Register New User");
        dialog.initOwner(ownerStage);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(30));
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: white;");

        // Title
        Label titleLabel = new Label("👤 Create New Account");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #667eea;");
        grid.add(titleLabel, 0, 0, 2, 1);

        // Input fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a strong password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Your full name");

        TextField emailField = new TextField();
        emailField.setPromptText("your.email@example.com");

        ComboBox<User.UserRole> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(User.UserRole.PATIENT, User.UserRole.DOCTOR, User.UserRole.ADMIN);
        roleBox.setValue(User.UserRole.PATIENT);
        roleBox.setPromptText("Select your role");

        // Password strength indicator
        Label strengthLabel = new Label("");
        strengthLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            String msg = com.healthcare.database.PasswordUtil.getPasswordStrengthMessage(newVal);
            strengthLabel.setText(msg);
            if (msg.contains("✅")) {
                strengthLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12;");
            } else {
                strengthLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12;");
            }
        });

        // Add labels and fields
        int row = 1;
        grid.add(new Label("Username:"), 0, row);
        grid.add(usernameField, 1, row++);

        grid.add(new Label("Password:"), 0, row);
        grid.add(passwordField, 1, row++);

        grid.add(new Label(""), 0, row);
        grid.add(strengthLabel, 1, row++);

        grid.add(new Label("Confirm Password:"), 0, row);
        grid.add(confirmPasswordField, 1, row++);

        grid.add(new Label("Full Name:"), 0, row);
        grid.add(fullNameField, 1, row++);

        grid.add(new Label("Email:"), 0, row);
        grid.add(emailField, 1, row++);

        grid.add(new Label("Role:"), 0, row);
        grid.add(roleBox, 1, row++);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button registerBtn = new Button("✅ Register");
        registerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-padding: 10 30; -fx-font-weight: bold;");

        Button cancelBtn = new Button("❌ Cancel");
        cancelBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                "-fx-padding: 10 30;");

        registerBtn.setOnAction(e -> {
            // Validate input
            if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty() ||
                    fullNameField.getText().isEmpty() || emailField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields!");
                return;
            }

            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match!");
                return;
            }

            // Create new user
            User newUser = new User();
            newUser.setUsername(usernameField.getText());
            newUser.setFullName(fullNameField.getText());
            newUser.setEmail(emailField.getText());
            newUser.setRole(roleBox.getValue());
            newUser.setActive(true);

            // Register user
            if (userDAO.registerUser(newUser, passwordField.getText())) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Account created successfully!\n" +
                                "You can now login with your credentials.");
                dialog.close();
                new Thread(() -> {
                    EmailService.sendWelcomeEmail(
                            emailField.getText(),
                            fullNameField.getText(),
                            roleBox.getValue().toString()
                    );
                }).start();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to create account!\n" +
                                "Username or email may already exist.");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(registerBtn, cancelBtn);
        grid.add(buttonBox, 0, row, 2, 1);

        Scene scene = new Scene(grid, 450, 550);
        dialog.setScene(scene);
        dialog.show();
    }

    /**
     * Create default admin account for testing
     */
    private void createDefaultAdmin() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setFullName("System Administrator");
        admin.setEmail("admin@healthcare.com");
        admin.setRole(User.UserRole.ADMIN);
        admin.setActive(true);

        if (userDAO.registerUser(admin, "Admin123")) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Default admin account created!\n\n" +
                            "Username: admin\n" +
                            "Password: Admin123\n\n" +
                            "You can now login with these credentials.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Notice",
                    "Admin account already exists or creation failed!");
        }
    }

    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}