package com.healthcare.controller;

import com.healthcare.database.AssessmentDAO;
import com.healthcare.database.MedicationDAO;
import com.healthcare.database.PatientDAO;
import com.healthcare.model.MedicationAssessment;
import com.healthcare.model.Patient;
import com.healthcare.model.PatientMedication;
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

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PatientProfileController - Comprehensive Patient Profile View
 * Shows complete patient information, medications, and assessment history
 */
public class PatientProfileController extends Application {

    private PatientDAO patientDAO = new PatientDAO();
    private MedicationDAO medicationDAO = new MedicationDAO();
    private AssessmentDAO assessmentDAO = new AssessmentDAO();

    private Patient currentPatient;

    public PatientProfileController() {
    }

    public PatientProfileController(Patient patient) {
        this.currentPatient = patient;
    }

    @Override
    public void start(Stage primaryStage) {
        // For testing - show patient selection if no patient provided
        if (currentPatient == null) {
            showPatientSelection(primaryStage);
        } else {
            showPatientProfile(primaryStage, currentPatient);
        }
    }

    /**
     * Show patient selection dialog
     */
    private void showPatientSelection(Stage stage) {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Label title = new Label("👤 Select Patient Profile");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        ComboBox<Patient> patientBox = new ComboBox<>();
        List<Patient> patients = patientDAO.getAllPatients();
        patientBox.getItems().addAll(patients);
        patientBox.setPromptText("Select a patient...");
        patientBox.setPrefWidth(400);

        patientBox.setCellFactory(lv -> new ListCell<Patient>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                setText(empty ? "" : patient.getFullName() + " (ID: " + patient.getPatientId() + ")");
            }
        });
        patientBox.setButtonCell(new ListCell<Patient>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                setText(empty ? "" : patient.getFullName());
            }
        });

        Button viewBtn = new Button("View Profile");
        viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 15 40;");
        viewBtn.setDisable(true);

        patientBox.setOnAction(e -> viewBtn.setDisable(patientBox.getValue() == null));

        viewBtn.setOnAction(e -> {
            if (patientBox.getValue() != null) {
                showPatientProfile(stage, patientBox.getValue());
            }
        });

        layout.getChildren().addAll(title, patientBox, viewBtn);

        Scene scene = new Scene(layout, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Patient Profile Selection");
        stage.show();
    }

    /**
     * Show complete patient profile
     */
    private void showPatientProfile(Stage stage, Patient patient) {
        this.currentPatient = patient;
        stage.setTitle("👤 Patient Profile - " + patient.getFullName());

        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        VBox header = createProfileHeader(patient);
        mainLayout.setTop(header);

        // Center: Scrollable content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox content = createProfileContent(patient);
        scrollPane.setContent(content);
        mainLayout.setCenter(scrollPane);

        // Bottom: Action buttons
        HBox buttonBar = createActionButtons(stage, patient);
        mainLayout.setBottom(buttonBar);

        Scene scene = new Scene(mainLayout, 1100, 700);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Create profile header with patient photo and basic info
     */
    private VBox createProfileHeader(Patient patient) {
        VBox header = new VBox(15);
        header.setPadding(new Insets(30));
        header.setStyle("-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%);");
        header.setAlignment(Pos.CENTER);

        // Patient avatar (emoji for now)
        Label avatar = new Label("👤");
        avatar.setFont(Font.font(60));
        avatar.setStyle("-fx-background-color: white; -fx-background-radius: 50; " +
                "-fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        Label name = new Label(patient.getFullName());
        name.setFont(Font.font("System", FontWeight.BOLD, 32));
        name.setTextFill(Color.WHITE);

        HBox infoBox = new HBox(40);
        infoBox.setAlignment(Pos.CENTER);

        Label ageLabel = new Label("Age: " + patient.getAge() + " years");
        ageLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        ageLabel.setTextFill(Color.web("#FFD700"));

        Label genderLabel = new Label("Gender: " + patient.getGender());
        genderLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        genderLabel.setTextFill(Color.web("#FFD700"));

        Label bloodLabel = new Label("Blood: " +
                (patient.getBloodGroup() != null ? patient.getBloodGroup() : "N/A"));
        bloodLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        bloodLabel.setTextFill(Color.web("#FFD700"));

        infoBox.getChildren().addAll(ageLabel, genderLabel, bloodLabel);

        Label idLabel = new Label("Patient ID: " + patient.getPatientId());
        idLabel.setFont(Font.font("System", 14));
        idLabel.setTextFill(Color.web("#E0E0E0"));

        header.getChildren().addAll(avatar, name, infoBox, idLabel);
        return header;
    }

    /**
     * Create profile content sections
     */
    private VBox createProfileContent(Patient patient) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        // Statistics cards
        HBox statsBox = createStatisticsCards(patient);

        // Contact Information
        VBox contactSection = createContactSection(patient);

        // Current Medications
        VBox medicationSection = createMedicationSection(patient);

        // Assessment History
        VBox assessmentSection = createAssessmentSection(patient);

        content.getChildren().addAll(statsBox, contactSection, medicationSection, assessmentSection);
        return content;
    }

    /**
     * Create statistics cards
     */
    private HBox createStatisticsCards(Patient patient) {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        List<PatientMedication> medications = medicationDAO.getPatientMedications(patient.getPatientId());
        long activeMeds = medications.stream().filter(m -> m.isActive()).count();

        List<MedicationAssessment> assessments = medications.stream()
                .flatMap(m -> assessmentDAO.getAssessmentsForMedication(m.getPatientMedicationId()).stream())
                .toList();
        long completedAssessments = assessments.stream()
                .filter(a -> a.isQuestionnaireCompleted())
                .count();

        VBox medsCard = createStatCard("💊", "Active Medications", String.valueOf(activeMeds), "#9C27B0");
        VBox assessCard = createStatCard("📝", "Completed Assessments", String.valueOf(completedAssessments), "#2196F3");
        VBox ageCard = createStatCard("🎂", "Age", patient.getAge() + " years", "#4CAF50");

        statsBox.getChildren().addAll(medsCard, assessCard, ageCard);
        return statsBox;
    }

    private VBox createStatCard(String icon, String label, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25));
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3); " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 4 0;");

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(36));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");

        Label labelText = new Label(label);
        labelText.setFont(Font.font("System", FontWeight.BOLD, 14));
        labelText.setStyle("-fx-text-fill: #666;");

        card.getChildren().addAll(iconLabel, valueLabel, labelText);
        return card;
    }

    /**
     * Create contact information section
     */
    private VBox createContactSection(Patient patient) {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);");

        Label title = new Label("📞 Contact Information");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #333;");

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(15);
        grid.setPadding(new Insets(15, 0, 0, 0));

        addInfoRow(grid, 0, "📱 Phone:", patient.getPhone() != null ? patient.getPhone() : "N/A");
        addInfoRow(grid, 1, "📧 Email:", patient.getEmail() != null ? patient.getEmail() : "N/A");
        addInfoRow(grid, 2, "📅 Date of Birth:", patient.getDateOfBirth().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        addInfoRow(grid, 3, "🏠 Address:", patient.getAddress() != null ? patient.getAddress() : "N/A");

        section.getChildren().addAll(title, grid);
        return section;
    }

    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("System", FontWeight.BOLD, 14));
        labelNode.setStyle("-fx-text-fill: #666;");

        Label valueNode = new Label(value);
        valueNode.setFont(Font.font("System", 14));
        valueNode.setStyle("-fx-text-fill: #333;");
        valueNode.setWrapText(true);
        valueNode.setMaxWidth(500);

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    /**
     * Create medication section
     */
    private VBox createMedicationSection(Patient patient) {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);");

        Label title = new Label("💊 Current Medications");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #333;");

        List<PatientMedication> medications = medicationDAO.getPatientMedications(patient.getPatientId());

        if (medications.isEmpty()) {
            Label noMeds = new Label("No medications prescribed yet");
            noMeds.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            section.getChildren().addAll(title, noMeds);
        } else {
            VBox medList = new VBox(10);

            for (PatientMedication med : medications) {
                HBox medCard = createMedicationCard(med);
                medList.getChildren().add(medCard);
            }

            section.getChildren().addAll(title, medList);
        }

        return section;
    }

    private HBox createMedicationCard(PatientMedication med) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8; " +
                "-fx-border-color: " + (med.isActive() ? "#4CAF50" : "#999") + "; " +
                "-fx-border-radius: 8; -fx-border-width: 0 0 0 4;");

        VBox infoBox = new VBox(5);

        Label medName = new Label(med.getMedicationName() + " " + med.getDosage());
        medName.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label frequency = new Label(med.getFrequency() + " • " + med.getRoute());
        frequency.setStyle("-fx-text-fill: #666;");

        Label duration = new Label("Started " + med.getDurationText() + " • Status: " + med.getStatus());
        duration.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        infoBox.getChildren().addAll(medName, frequency, duration);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(med.isActive() ? "✅ Active" : "⏸️ " + med.getStatus());
        statusBadge.setPadding(new Insets(5, 15, 5, 15));
        statusBadge.setStyle("-fx-background-color: " + (med.isActive() ? "#4CAF5020" : "#99999920") + "; " +
                "-fx-background-radius: 15; -fx-text-fill: " +
                (med.isActive() ? "#4CAF50" : "#999") + "; -fx-font-weight: bold;");

        card.getChildren().addAll(infoBox, spacer, statusBadge);
        return card;
    }

    /**
     * Create assessment history section
     */
    private VBox createAssessmentSection(Patient patient) {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);");

        Label title = new Label("📋 Assessment History");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #333;");

        List<PatientMedication> medications = medicationDAO.getPatientMedications(patient.getPatientId());
        List<MedicationAssessment> allAssessments = medications.stream()
                .flatMap(m -> assessmentDAO.getAssessmentsForMedication(m.getPatientMedicationId()).stream())
                .toList();

        if (allAssessments.isEmpty()) {
            Label noAssessments = new Label("No assessments completed yet");
            noAssessments.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            section.getChildren().addAll(title, noAssessments);
        } else {
            VBox assessmentList = new VBox(10);

            for (MedicationAssessment assessment : allAssessments) {
                HBox assessCard = createAssessmentCard(assessment);
                assessmentList.getChildren().add(assessCard);
            }

            section.getChildren().addAll(title, assessmentList);
        }

        return section;
    }

    private HBox createAssessmentCard(MedicationAssessment assessment) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8;");

        Label icon = new Label(assessment.isQuestionnaireCompleted() ? "✅" : "⏳");
        icon.setFont(Font.font(24));

        VBox infoBox = new VBox(5);

        Label medName = new Label(assessment.getMedicationName() + " - Day " + assessment.getAssessmentDay());
        medName.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label status = new Label("Status: " + assessment.getStatus() +
                (assessment.isAiAnalysisCompleted() ? " • AI Analysis Complete" : ""));
        status.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        infoBox.getChildren().addAll(medName, status);

        card.getChildren().addAll(icon, infoBox);
        return card;
    }

    /**
     * Create action buttons
     */
    private HBox createActionButtons(Stage stage, Patient patient) {
        HBox buttonBar = new HBox(15);
        buttonBar.setPadding(new Insets(20));
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setStyle("-fx-background-color: white; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, -2);");

        Button editBtn = new Button("✏️ Edit Profile");
        editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 12 25;");
        editBtn.setOnAction(e -> showAlert("Coming Soon", "Edit profile feature coming soon!"));

        Button prescribeBtn = new Button("💊 Prescribe Medication");
        prescribeBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 12 25;");
        prescribeBtn.setOnAction(e -> openMedicationManagement());

        Button assessmentsBtn = new Button("📋 View Assessments");
        assessmentsBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 12 25;");
        assessmentsBtn.setOnAction(e -> openDoctorDashboard());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 12 25;");
        refreshBtn.setOnAction(e -> showPatientProfile(stage, patient));

        Button backBtn = new Button("⬅️ Back");
        backBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 12 25;");
        backBtn.setOnAction(e -> stage.close());

        buttonBar.getChildren().addAll(editBtn, prescribeBtn, assessmentsBtn, refreshBtn, backBtn);
        return buttonBar;
    }

    private void openMedicationManagement() {
        try {
            MedicationController controller = new MedicationController();
            Stage stage = new Stage();
            controller.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openDoctorDashboard() {
        try {
            DoctorDashboardController controller = new DoctorDashboardController();
            Stage stage = new Stage();
            controller.start(stage);
        } catch (Exception e) {
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

    public static void main(String[] args) {
        launch(args);
    }
}
