package com.healthcare.controller;

import com.healthcare.service.EmailService;
import com.healthcare.database.MedicationDAO;
import com.healthcare.database.PatientDAO;
import com.healthcare.database.SessionManager;
import com.healthcare.model.Medication;
import com.healthcare.model.Patient;
import com.healthcare.model.PatientMedication;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

/**
 * MedicationController - Medication Management Interface
 * Handles prescribing and tracking medications
 */
public class MedicationController extends Application {

    private MedicationDAO medicationDAO = new MedicationDAO();
    private PatientDAO patientDAO = new PatientDAO();
    private TableView<PatientMedication> medicationTable = new TableView<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("💊 Medication Management System");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        VBox header = createHeader();
        mainLayout.setTop(header);

        VBox tableSection = createTableSection();
        mainLayout.setCenter(tableSection);

        HBox buttonBar = createButtonBar(primaryStage);
        mainLayout.setBottom(buttonBar);

        refreshMedicationTable();

        Scene scene = new Scene(mainLayout, 1100, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("💊 Medication Management GUI launched");
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: #9C27B0; -fx-padding: 20;");
        header.setAlignment(Pos.CENTER);

        Label title = new Label("💊 Medication Management System");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Track Patient Medications & Monitor Treatment");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        SessionManager session = SessionManager.getInstance();
        Label userLabel = new Label(session.getWelcomeMessage());
        userLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFD700; -fx-font-weight: bold;");

        header.getChildren().addAll(title, subtitle, userLabel);
        return header;
    }

    private VBox createTableSection() {
        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(20));

        Label tableLabel = new Label("📋 Active Medications");
        tableLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableColumn<PatientMedication, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("patientMedicationId"));
        idCol.setPrefWidth(50);

        TableColumn<PatientMedication, String> patientCol = new TableColumn<>("Patient");
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        patientCol.setPrefWidth(150);

        TableColumn<PatientMedication, String> medicationCol = new TableColumn<>("Medication");
        medicationCol.setCellValueFactory(new PropertyValueFactory<>("medicationName"));
        medicationCol.setPrefWidth(150);

        TableColumn<PatientMedication, String> dosageCol = new TableColumn<>("Dosage");
        dosageCol.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        dosageCol.setPrefWidth(100);

        TableColumn<PatientMedication, String> frequencyCol = new TableColumn<>("Frequency");
        frequencyCol.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        frequencyCol.setPrefWidth(150);

        TableColumn<PatientMedication, LocalDate> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startDateCol.setPrefWidth(120);

        TableColumn<PatientMedication, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        TableColumn<PatientMedication, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(cellData -> {
            PatientMedication pm = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(pm.getDurationText());
        });
        durationCol.setPrefWidth(120);

        TableColumn<PatientMedication, String> doctorCol = new TableColumn<>("Prescribed By");
        doctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        doctorCol.setPrefWidth(150);

        medicationTable.getColumns().addAll(idCol, patientCol, medicationCol, dosageCol,
                frequencyCol, startDateCol, durationCol,
                statusCol, doctorCol);

        medicationTable.setStyle("-fx-background-color: white;");

        tableBox.getChildren().addAll(tableLabel, medicationTable);
        return tableBox;
    }

    private HBox createButtonBar(Stage stage) {
        HBox buttonBar = new HBox(15);
        buttonBar.setPadding(new Insets(15));
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setStyle("-fx-background-color: #e0e0e0;");

        Button prescribeBtn = new Button("➕ Prescribe Medication");
        prescribeBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        prescribeBtn.setOnAction(e -> showPrescribeMedicationDialog(stage));

        Button assessmentBtn = new Button("📝 Patient Assessments");
        assessmentBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        assessmentBtn.setOnAction(e -> openPatientAssessments());

        Button viewBtn = new Button("👁️ View Details");
        viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        viewBtn.setOnAction(e -> showMedicationDetails());

        Button statusBtn = new Button("🔄 Change Status");
        statusBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        statusBtn.setOnAction(e -> changeMedicationStatus());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        refreshBtn.setOnAction(e -> refreshMedicationTable());

        Button backBtn = new Button("⬅️ Back");
        backBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        backBtn.setOnAction(e -> stage.close());

        buttonBar.getChildren().addAll(prescribeBtn, assessmentBtn, viewBtn, statusBtn,
                refreshBtn, backBtn);
        return buttonBar;
    }

    private void showPrescribeMedicationDialog(Stage ownerStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Prescribe Medication");
        dialog.initOwner(ownerStage);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        Label titleLabel = new Label("💊 Prescribe New Medication");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #9C27B0;");
        grid.add(titleLabel, 0, 0, 2, 1);

        ComboBox<Patient> patientBox = new ComboBox<>();
        patientBox.setPromptText("Select patient");
        List<Patient> patients = patientDAO.getAllPatients();
        patientBox.getItems().addAll(patients);
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

        ComboBox<Medication> medicationBox = new ComboBox<>();
        medicationBox.setPromptText("Select medication");
        List<Medication> medications = medicationDAO.getAllMedications();
        medicationBox.getItems().addAll(medications);
        medicationBox.setCellFactory(lv -> new ListCell<Medication>() {
            @Override
            protected void updateItem(Medication med, boolean empty) {
                super.updateItem(med, empty);
                setText(empty ? "" : med.getDisplayName() + " - " + med.getCategory());
            }
        });
        medicationBox.setButtonCell(new ListCell<Medication>() {
            @Override
            protected void updateItem(Medication med, boolean empty) {
                super.updateItem(med, empty);
                setText(empty ? "" : med.getDisplayName());
            }
        });

        TextField dosageField = new TextField();
        dosageField.setPromptText("e.g., 10mg, 500mg");

        ComboBox<String> frequencyBox = new ComboBox<>();
        frequencyBox.getItems().addAll(
                "Once daily", "Twice daily", "Three times daily", "Four times daily",
                "Every 6 hours", "Every 8 hours", "Every 12 hours", "As needed",
                "Before meals", "After meals"
        );
        frequencyBox.setPromptText("Select frequency");

        ComboBox<String> routeBox = new ComboBox<>();
        routeBox.getItems().addAll("Oral", "Injection", "Topical", "Inhalation",
                "Sublingual", "IV", "IM", "Subcutaneous");
        routeBox.setValue("Oral");

        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        DatePicker endDatePicker = new DatePicker();

        TextArea instructionsArea = new TextArea();
        instructionsArea.setPromptText("Special instructions for patient...");
        instructionsArea.setPrefRowCount(2);

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Reason for prescribing...");
        reasonArea.setPrefRowCount(2);

        int row = 1;
        grid.add(new Label("Patient:*"), 0, row);
        grid.add(patientBox, 1, row++);

        grid.add(new Label("Medication:*"), 0, row);
        grid.add(medicationBox, 1, row++);

        grid.add(new Label("Dosage:*"), 0, row);
        grid.add(dosageField, 1, row++);

        grid.add(new Label("Frequency:*"), 0, row);
        grid.add(frequencyBox, 1, row++);

        grid.add(new Label("Route:*"), 0, row);
        grid.add(routeBox, 1, row++);

        grid.add(new Label("Start Date:*"), 0, row);
        grid.add(startDatePicker, 1, row++);

        grid.add(new Label("End Date:"), 0, row);
        grid.add(endDatePicker, 1, row++);

        grid.add(new Label("Instructions:"), 0, row);
        grid.add(instructionsArea, 1, row++);

        grid.add(new Label("Reason:"), 0, row);
        grid.add(reasonArea, 1, row++);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveBtn = new Button("💾 Prescribe");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 30;");

        Button cancelBtn = new Button("❌ Cancel");
        cancelBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 10 30;");

        saveBtn.setOnAction(e -> {
            if (patientBox.getValue() == null || medicationBox.getValue() == null ||
                    dosageField.getText().isEmpty() || frequencyBox.getValue() == null ||
                    startDatePicker.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill all required fields (*)");
                return;
            }

            PatientMedication pm = new PatientMedication();
            pm.setPatientId(patientBox.getValue().getPatientId());
            pm.setMedicationId(medicationBox.getValue().getMedicationId());
            pm.setPrescribedBy(SessionManager.getInstance().getCurrentUser().getUserId());
            pm.setDosage(dosageField.getText());
            pm.setFrequency(frequencyBox.getValue());
            pm.setRoute(routeBox.getValue());
            pm.setStartDate(startDatePicker.getValue());
            pm.setEndDate(endDatePicker.getValue());
            pm.setStatus("Active");
            pm.setInstructions(instructionsArea.getText());
            pm.setReason(reasonArea.getText());

            if (medicationDAO.prescribeMedication(pm)) {
                Patient patient = patientDAO.getPatientById(pm.getPatientId());
                if (patient != null && patient.getEmail() != null) {
                    new Thread(() -> {
                        EmailService.sendMedicationPrescribedEmail(
                                patient.getEmail(),
                                patient.getFullName(),
                                medicationBox.getValue().getMedicationName(),
                                dosageField.getText(),
                                frequencyBox.getValue(),
                                instructionsArea.getText()
                        );
                    }).start();
                }
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Medication prescribed successfully!\n7-day assessment scheduled automatically.");
                refreshMedicationTable();
                dialog.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to prescribe medication!");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        grid.add(buttonBox, 0, row, 2, 1);

        Scene scene = new Scene(grid, 500, 650);
        dialog.setScene(scene);
        dialog.show();
    }

    private void showMedicationDetails() {
        PatientMedication selected = medicationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select a medication to view details!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Medication Details");
        alert.setHeaderText("💊 " + selected.getMedicationName());

        String details = String.format(
                "Patient: %s\nDosage: %s\nFrequency: %s\nRoute: %s\n" +
                        "Start Date: %s\nEnd Date: %s\nStatus: %s\nDays Since Start: %d\n" +
                        "Prescribed By: %s\nReason: %s\nInstructions: %s\n\n%s",
                selected.getPatientName(), selected.getDosage(), selected.getFrequency(),
                selected.getRoute(), selected.getStartDate(),
                selected.getEndDate() != null ? selected.getEndDate() : "Ongoing",
                selected.getStatus(), selected.getDaysSinceStart(), selected.getDoctorName(),
                selected.getReason() != null ? selected.getReason() : "N/A",
                selected.getInstructions() != null ? selected.getInstructions() : "N/A",
                selected.isDueForSevenDayAssessment() ? "⚠️ DUE FOR 7-DAY ASSESSMENT!" : ""
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    private void changeMedicationStatus() {
        PatientMedication selected = medicationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select a medication to change status!");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Active",
                "Active", "Completed", "Discontinued");
        dialog.setTitle("Change Medication Status");
        dialog.setHeaderText("Change status for: " + selected.getMedicationName());
        dialog.setContentText("New status:");

        dialog.showAndWait().ifPresent(status -> {
            if (medicationDAO.updateMedicationStatus(selected.getPatientMedicationId(), status)) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Medication status updated to: " + status);
                refreshMedicationTable();
            }
        });
    }

    private void refreshMedicationTable() {
        medicationTable.getItems().clear();
        medicationTable.getItems().addAll(medicationDAO.getAllActiveMedications());
        System.out.println("🔄 Medication table refreshed");
    }

    private void openPatientAssessments() {
        try {
            PatientQuestionnaireController questionnaireController =
                    new PatientQuestionnaireController();
            Stage questionnaireStage = new Stage();
            questionnaireController.start(questionnaireStage);

            System.out.println("📝 Patient assessments opened");
        } catch (Exception e) {
            System.err.println("❌ Error opening assessments: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
