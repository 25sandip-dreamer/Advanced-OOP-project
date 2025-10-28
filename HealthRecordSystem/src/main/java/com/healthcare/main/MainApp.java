package com.healthcare.main;

import com.healthcare.database.PatientDAO;
import com.healthcare.model.Patient;
import com.healthcare.service.EmailScheduler;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.Optional;
import com.healthcare.controller.ChatController;


import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
/**
 * MainApp - Your Healthcare Management System GUI
 * This is your first working healthcare application with a beautiful interface!
 */
public class MainApp extends Application {

    private PatientDAO patientDAO = new PatientDAO();
    private TableView<Patient> patientTable = new TableView<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("🏥 Personal Health Record Management System");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        VBox header = createHeader();
        mainLayout.setTop(header);

        VBox tableSection = createTableSection();
        mainLayout.setCenter(tableSection);

        HBox buttonBar = createButtonBar(primaryStage);
        mainLayout.setBottom(buttonBar);

        refreshPatientTable();

        Scene scene = new Scene(mainLayout, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        // Start email scheduler
        EmailScheduler emailScheduler = new EmailScheduler();
        emailScheduler.startScheduler();

// Add shutdown hook to stop scheduler on application exit
        primaryStage.setOnCloseRequest(event -> {
            emailScheduler.stopScheduler();
        });

        System.out.println("🚀 Healthcare System GUI launched successfully!");
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: #2196F3; -fx-padding: 20;");
        header.setAlignment(Pos.CENTER);

        com.healthcare.database.SessionManager session =
                com.healthcare.database.SessionManager.getInstance();

        Label title = new Label("🏥 Personal Health Record Management System");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Manage Patient Records Efficiently");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        Label userLabel = new Label(session.getWelcomeMessage());
        userLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFD700; -fx-font-weight: bold;");

        header.getChildren().addAll(title, subtitle, userLabel);
        return header;
    }

    private VBox createTableSection() {
        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(20));

        Label tableLabel = new Label("📋 Patient Records");
        tableLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableColumn<Patient, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        idCol.setPrefWidth(50);

        TableColumn<Patient, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setPrefWidth(120);

        TableColumn<Patient, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setPrefWidth(120);

        TableColumn<Patient, LocalDate> dobCol = new TableColumn<>("Date of Birth");
        dobCol.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        dobCol.setPrefWidth(120);

        TableColumn<Patient, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setPrefWidth(80);

        TableColumn<Patient, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(120);

        TableColumn<Patient, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(180);

        TableColumn<Patient, String> bloodCol = new TableColumn<>("Blood Group");
        bloodCol.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        bloodCol.setPrefWidth(100);

        patientTable.getColumns().addAll(idCol, firstNameCol, lastNameCol, dobCol,
                genderCol, phoneCol, emailCol, bloodCol);

        patientTable.setStyle("-fx-background-color: white;");

        tableBox.getChildren().addAll(tableLabel, patientTable);
        return tableBox;
    }

    private HBox createButtonBar(Stage stage) {
        HBox buttonBar = new HBox(15);
        buttonBar.setPadding(new Insets(15));
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setStyle("-fx-background-color: #e0e0e0;");

        Button addBtn = new Button("➕ Add Patient");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        addBtn.setOnAction(e -> showAddPatientDialog(stage));

        Button viewProfileBtn = new Button("👤 View Profile");
        viewProfileBtn.setStyle("-fx-background-color: #00BCD4; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        viewProfileBtn.setOnAction(e -> openPatientProfile());

        Button medicationBtn = new Button("💊 Medications");
        medicationBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        medicationBtn.setOnAction(e -> openMedicationManagement());

        Button dashboardBtn = new Button("👨‍⚕️ Doctor Dashboard");
        dashboardBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20; -fx-font-weight: bold;");
        dashboardBtn.setOnAction(e -> openDoctorDashboard());

        Button chatBtn = new Button("💬 Messages");
        chatBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        chatBtn.setOnAction(e -> openChat());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        refreshBtn.setOnAction(e -> refreshPatientTable());

        Button deleteBtn = new Button("🗑️ Delete Selected");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        deleteBtn.setOnAction(e -> deleteSelectedPatient());

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        logoutBtn.setOnAction(e -> handleLogout(stage));

        Button exitBtn = new Button("❌ Exit");
        exitBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        exitBtn.setOnAction(e -> stage.close());

        buttonBar.getChildren().addAll(addBtn, viewProfileBtn, medicationBtn, dashboardBtn,chatBtn, refreshBtn,
                deleteBtn, logoutBtn, exitBtn);
        return buttonBar;
    }

    private void refreshPatientTable() {
        patientTable.getItems().clear();
        patientTable.getItems().addAll(patientDAO.getAllPatients());
        System.out.println("🔄 Patient table refreshed");
    }
// ============================================
// CORRECTED showAddPatientDialog() METHOD
// This matches YOUR actual Patient model!
// Replace in MainApp.java
// ============================================

    private void showAddPatientDialog(Stage ownerStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Add New Patient");
        dialog.initOwner(ownerStage);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setStyle("-fx-background-color: #f9f9f9;");

        // ========== TITLE ==========
        Label titleLabel = new Label("➕ Add New Patient");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #667eea;");
        grid.add(titleLabel, 0, 0, 2, 1);

        int row = 1;

        // ========== FORM FIELDS WITH CLEAR LABELS ==========

        // First Name
        Label firstNameLbl = new Label("First Name: *");
        firstNameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Enter first name");
        grid.add(firstNameLbl, 0, row);
        grid.add(firstNameField, 1, row++);

        // Last Name
        Label lastNameLbl = new Label("Last Name: *");
        lastNameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Enter last name");
        grid.add(lastNameLbl, 0, row);
        grid.add(lastNameField, 1, row++);

        // Date of Birth
        Label dobLbl = new Label("Date of Birth: *");
        dobLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        DatePicker dobPicker = new DatePicker();
        dobPicker.setPromptText("Select birth date");
        grid.add(dobLbl, 0, row);
        grid.add(dobPicker, 1, row++);

        // Gender
        Label genderLbl = new Label("Gender: *");
        genderLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female", "Other");
        genderBox.setPromptText("Select gender");
        grid.add(genderLbl, 0, row);
        grid.add(genderBox, 1, row++);

        // Phone
        Label phoneLbl = new Label("Phone Number:");
        phoneLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField phoneField = new TextField();
        phoneField.setPromptText("e.g., +1 555-123-4567");
        grid.add(phoneLbl, 0, row);
        grid.add(phoneField, 1, row++);

        // Email
        Label emailLbl = new Label("Email Address:");
        emailLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField emailField = new TextField();
        emailField.setPromptText("e.g., john@email.com");
        grid.add(emailLbl, 0, row);
        grid.add(emailField, 1, row++);

        // Address
        Label addressLbl = new Label("Address:");
        addressLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextArea addressField = new TextArea();
        addressField.setPromptText("Enter full address");
        addressField.setPrefRowCount(3);
        addressField.setWrapText(true);
        grid.add(addressLbl, 0, row);
        grid.add(addressField, 1, row++);

        // Blood Group
        Label bloodLbl = new Label("Blood Group:");
        bloodLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        ComboBox<String> bloodGroupBox = new ComboBox<>();
        bloodGroupBox.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        bloodGroupBox.setPromptText("Select blood type");
        grid.add(bloodLbl, 0, row);
        grid.add(bloodGroupBox, 1, row++);

        // Required fields note
        Label reqNote = new Label("* Required fields");
        reqNote.setStyle("-fx-font-size: 11px; -fx-text-fill: #f44336; -fx-font-style: italic;");
        grid.add(reqNote, 0, row, 2, 1);
        row++;

        // ========== BUTTONS ==========
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveBtn = new Button("💾 Save");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 25;");

        Button cancelBtn = new Button("✖ Cancel");
        cancelBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 25;");

        saveBtn.setOnAction(e -> {
            // Validation
            if (firstNameField.getText().isEmpty() ||
                    lastNameField.getText().isEmpty() ||
                    dobPicker.getValue() == null ||
                    genderBox.getValue() == null) {

                showAlert("Error", "Please fill all required fields (marked with *)!");
                return;
            }

            // Create patient object - USING YOUR ACTUAL METHOD NAMES
            Patient patient = new Patient();
            patient.setFirstName(firstNameField.getText());
            patient.setLastName(lastNameField.getText());
            patient.setDateOfBirth(dobPicker.getValue());
            patient.setGender(genderBox.getValue());
            patient.setPhone(phoneField.getText());      // Your model uses setPhone
            patient.setEmail(emailField.getText());
            patient.setAddress(addressField.getText());
            patient.setBloodGroup(bloodGroupBox.getValue());  // Your model uses setBloodGroup

            // Save to database
            if (patientDAO.addPatient(patient)) {
                showAlert("Success", "Patient added successfully!");
                refreshPatientTable();
                dialog.close();
            } else {
                showAlert("Error", "Failed to add patient!");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        grid.add(buttonBox, 0, row, 2, 1);

        Scene scene = new Scene(grid, 500, 550);
        dialog.setScene(scene);
        dialog.show();
    }

    private void deleteSelectedPatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a patient to delete!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Patient?");
        confirm.setContentText("Are you sure you want to delete " + selected.getFullName() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (patientDAO.deletePatient(selected.getPatientId())) {
                    showAlert("Success", "Patient deleted successfully!");
                    refreshPatientTable();
                }
            }
        });
    }

    private void handleLogout(Stage stage) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will be returned to the login screen.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                com.healthcare.database.SessionManager.getInstance().logout();
                stage.close();

                try {
                    com.healthcare.controller.LoginController loginController =
                            new com.healthcare.controller.LoginController();
                    Stage loginStage = new Stage();
                    loginController.start(loginStage);

                    System.out.println("👋 User logged out successfully");
                } catch (Exception e) {
                    System.err.println("❌ Error returning to login: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void openMedicationManagement() {
        try {
            com.healthcare.controller.MedicationController medicationController =
                    new com.healthcare.controller.MedicationController();
            Stage medicationStage = new Stage();
            medicationController.start(medicationStage);

            System.out.println("💊 Medication management opened");
        } catch (Exception e) {
            System.err.println("❌ Error opening medication management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openDoctorDashboard() {
        try {
            com.healthcare.controller.DoctorDashboardController dashboardController =
                    new com.healthcare.controller.DoctorDashboardController();
            Stage dashboardStage = new Stage();
            dashboardController.start(dashboardStage);

            System.out.println("👨‍⚕️ Doctor dashboard opened");
        } catch (Exception e) {
            System.err.println("❌ Error opening doctor dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openPatientProfile() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a patient to view their profile!");
            return;
        }

        try {
            com.healthcare.controller.PatientProfileController profileController =
                    new com.healthcare.controller.PatientProfileController(selected);
            Stage profileStage = new Stage();
            profileController.start(profileStage);

            System.out.println("👤 Patient profile opened for: " + selected.getFullName());
        } catch (Exception e) {
            System.err.println("❌ Error opening patient profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void openChat() {
        try {
            ChatController chatController = new ChatController();
            Stage chatStage = new Stage();
            chatController.start(chatStage);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open chat: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}