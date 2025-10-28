package com.healthcare.controller;

import com.healthcare.model.User;
import com.healthcare.service.EmailService;
import com.healthcare.database.AssessmentDAO;
import com.healthcare.database.SessionManager;
import com.healthcare.model.AssessmentResponse;
import com.healthcare.model.MedicationAssessment;
import com.healthcare.controller.AIAnalysisEngine.AnalysisResult;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

/**
 * DoctorDashboardController - AI-Powered Assessment Review Dashboard
 * Doctors review patient assessments with AI analysis and insights
 */
public class DoctorDashboardController extends Application {

    private AssessmentDAO assessmentDAO = new AssessmentDAO();
    private TableView<MedicationAssessment> assessmentTable = new TableView<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("👨‍⚕️ Doctor Dashboard - AI Assessment Review");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        VBox header = createHeader();
        mainLayout.setTop(header);

        // Center: Assessment table
        VBox tableSection = createTableSection();
        mainLayout.setCenter(tableSection);

        // Bottom: Action buttons
        HBox buttonBar = createButtonBar(primaryStage);
        mainLayout.setBottom(buttonBar);

        // Load assessments
        refreshAssessmentTable();

        Scene scene = new Scene(mainLayout, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("👨‍⚕️ Doctor Dashboard launched");
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%);");
        header.setAlignment(Pos.CENTER);

        Label title = new Label("👨‍⚕️ Doctor Dashboard - AI Assessment Review");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Review Patient Medication Assessments with AI-Powered Insights");
        subtitle.setFont(Font.font("System", 15));
        subtitle.setTextFill(Color.web("#FFD700"));

        SessionManager session = SessionManager.getInstance();
        Label userLabel = new Label(session.getWelcomeMessage());
        userLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        userLabel.setTextFill(Color.web("#FFD700"));

        header.getChildren().addAll(title, subtitle, userLabel);
        return header;
    }

    private VBox createTableSection() {
        VBox tableBox = new VBox(15);
        tableBox.setPadding(new Insets(20));

        // Stats summary
        HBox statsBox = createStatsBox();

        Label tableLabel = new Label("📋 Completed Patient Assessments");
        tableLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Configure table columns
        TableColumn<MedicationAssessment, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("monitoringId"));
        idCol.setPrefWidth(50);

        TableColumn<MedicationAssessment, String> patientCol = new TableColumn<>("Patient");
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        patientCol.setPrefWidth(150);

        TableColumn<MedicationAssessment, String> medicationCol = new TableColumn<>("Medication");
        medicationCol.setCellValueFactory(new PropertyValueFactory<>("medicationName"));
        medicationCol.setPrefWidth(150);

        TableColumn<MedicationAssessment, String> dosageCol = new TableColumn<>("Dosage");
        dosageCol.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        dosageCol.setPrefWidth(100);

        TableColumn<MedicationAssessment, Integer> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(new PropertyValueFactory<>("assessmentDay"));
        dayCol.setPrefWidth(60);

        TableColumn<MedicationAssessment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        // Custom column for AI analysis status
        TableColumn<MedicationAssessment, String> aiCol = new TableColumn<>("AI Analysis");
        aiCol.setCellValueFactory(cellData -> {
            boolean completed = cellData.getValue().isAiAnalysisCompleted();
            return new javafx.beans.property.SimpleStringProperty(
                    completed ? "✅ Completed" : "⏳ Pending"
            );
        });
        aiCol.setPrefWidth(120);

        TableColumn<MedicationAssessment, String> reviewCol = new TableColumn<>("Quick Status");
        reviewCol.setCellValueFactory(cellData -> {
            MedicationAssessment assessment = cellData.getValue();
            if (assessment.isQuestionnaireCompleted()) {
                return new javafx.beans.property.SimpleStringProperty("📝 Ready for Review");
            }
            return new javafx.beans.property.SimpleStringProperty("⏳ Not Completed");
        });
        reviewCol.setPrefWidth(150);

        assessmentTable.getColumns().addAll(idCol, patientCol, medicationCol,
                dosageCol, dayCol, statusCol, aiCol, reviewCol);

        assessmentTable.setStyle("-fx-background-color: white;");
        assessmentTable.setRowFactory(tv -> {
            TableRow<MedicationAssessment> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showDetailedAnalysis(row.getItem());
                }
            });
            return row;
        });

        Label hint = new Label("💡 Tip: Double-click any row to see detailed AI analysis");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic;");

        tableBox.getChildren().addAll(statsBox, tableLabel, assessmentTable, hint);
        return tableBox;
    }

    private HBox createStatsBox() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        List<MedicationAssessment> assessments = assessmentDAO.getCompletedAssessments();

        VBox totalBox = createStatCard("📊 Total Assessments",
                String.valueOf(assessments.size()), "#2196F3");

        long completed = assessments.stream()
                .filter(a -> a.isQuestionnaireCompleted())
                .count();
        VBox completedBox = createStatCard("✅ Completed",
                String.valueOf(completed), "#4CAF50");

        long pending = assessments.stream()
                .filter(a -> !a.isAiAnalysisCompleted())
                .count();
        VBox pendingBox = createStatCard("⏳ Pending Review",
                String.valueOf(pending), "#FF9800");

        statsBox.getChildren().addAll(totalBox, completedBox, pendingBox);
        return statsBox;
    }

    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: " + color + "20; -fx-background-radius: 8; " +
                "-fx-border-color: " + color + "; -fx-border-radius: 8; -fx-border-width: 2;");
        card.setPrefWidth(200);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");

        Label labelText = new Label(label);
        labelText.setFont(Font.font("System", FontWeight.BOLD, 14));
        labelText.setStyle("-fx-text-fill: #333;");

        card.getChildren().addAll(valueLabel, labelText);
        return card;
    }

    private HBox createButtonBar(Stage stage) {
        HBox buttonBar = new HBox(15);
        buttonBar.setPadding(new Insets(15));
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setStyle("-fx-background-color: white; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, -2);");

        Button analyzeBtn = new Button("🤖 Run AI Analysis");
        analyzeBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20; -fx-font-weight: bold;");
        analyzeBtn.setOnAction(e -> runAIAnalysisOnSelected());

        Button exportPdfBtn = new Button("📄 Export PDF");
        exportPdfBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20; -fx-font-weight: bold;");
        exportPdfBtn.setOnAction(e -> exportPDFReport());

        Button viewBtn = new Button("👁️ View Report");
        viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        viewBtn.setOnAction(e -> showDetailedAnalysis(assessmentTable.getSelectionModel().getSelectedItem()));

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        refreshBtn.setOnAction(e -> refreshAssessmentTable());

        Button backBtn = new Button("⬅️ Back");
        backBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        backBtn.setOnAction(e -> stage.close());

        buttonBar.getChildren().addAll(analyzeBtn, exportPdfBtn, viewBtn, refreshBtn, backBtn);
        return buttonBar;
    }

    private void exportPDFReport() {
        MedicationAssessment selected = assessmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select an assessment to export!");
            return;
        }

        if (!selected.isQuestionnaireCompleted()) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Assessment",
                    "This assessment hasn't been completed yet!");
            return;
        }

        // Get responses and run analysis
        List<AssessmentResponse> responses = assessmentDAO.getResponsesForAssessment(
                selected.getMonitoringId()
        );

        if (responses.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "No Data", "No responses found!");
            return;
        }

        AnalysisResult result = AIAnalysisEngine.analyzeResponses(responses);

        // Generate PDF with better path handling
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "AI_Report_" + selected.getPatientName().replace(" ", "_") + "_" + timestamp + ".pdf";

        // Try multiple paths
        String[] possiblePaths = {
                System.getProperty("user.home") + "/Desktop/" + filename,
                System.getProperty("user.home") + "/" + filename,
                System.getProperty("user.dir") + "/" + filename
        };

        Alert progress = new Alert(Alert.AlertType.INFORMATION);
        progress.setTitle("Generating PDF");
        progress.setHeaderText("📄 Creating PDF Report...");
        progress.setContentText("Please wait...");
        progress.show();

        java.io.File pdfFile = null;
        String errorMessage = "";

        // Try each path until one works
        for (String path : possiblePaths) {
            try {
                pdfFile = PDFReportGenerator.generateAIAnalysisReport(
                        selected, result, responses, selected.getPatientName(), path
                );

                if (pdfFile != null && pdfFile.exists()) {
                    break; // Success!
                }
            } catch (Exception e) {
                errorMessage = e.getMessage();
                System.err.println("Failed to create PDF at: " + path);
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        progress.close();

        if (pdfFile != null && pdfFile.exists()) {
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("PDF Generated!");
            success.setHeaderText("✅ Report Exported Successfully!");
            success.setContentText("PDF saved to:\n" + pdfFile.getAbsolutePath() +
                    "\n\nFile size: " + pdfFile.length() + " bytes");

            // Add button to open folder
            ButtonType openFolderBtn = new ButtonType("Open Folder");
            ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            success.getButtonTypes().setAll(openFolderBtn, closeBtn);

            java.io.File finalPdfFile = pdfFile;
            success.showAndWait().ifPresent(response -> {
                if (response == openFolderBtn) {
                    try {
                        // Open the folder containing the PDF
                        java.awt.Desktop.getDesktop().open(finalPdfFile.getParentFile());
                    } catch (Exception e) {
                        System.err.println("Could not open folder: " + e.getMessage());
                    }
                }
            });

            // Try to open the PDF
            try {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(pdfFile);
                }
            } catch (Exception e) {
                System.err.println("Could not auto-open PDF: " + e.getMessage());
            }
        } else {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("PDF Generation Failed");
            error.setHeaderText("Failed to generate PDF report!");
            error.setContentText("Error details:\n" + errorMessage +
                    "\n\nPlease check the console for more information.");
            error.showAndWait();
        }
    }

    private void runAIAnalysisOnSelected() {
        MedicationAssessment selected = assessmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select an assessment to analyze!");
            return;
        }

        if (!selected.isQuestionnaireCompleted()) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Assessment",
                    "Patient hasn't completed this assessment yet!");
            return;
        }

        // Get responses
        List<AssessmentResponse> responses = assessmentDAO.getResponsesForAssessment(
                selected.getMonitoringId()
        );

        if (responses.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "No Data",
                    "No responses found for this assessment!");
            return;
        }

        // Show progress
        Alert progress = new Alert(Alert.AlertType.INFORMATION);
        progress.setTitle("AI Analysis");
        progress.setHeaderText("🤖 Running AI Analysis...");
        progress.setContentText("Analyzing " + responses.size() + " patient responses...");
        progress.show();

        // Run AI analysis
        AnalysisResult result = AIAnalysisEngine.analyzeResponses(responses);

        // Mark as analyzed
        assessmentDAO.markAIAnalysisCompleted(selected.getMonitoringId(),
                result.getRiskLevel());

        progress.close();

        // Show results
        showAnalysisResults(selected, result);

        // Refresh table
        refreshAssessmentTable();
    }

    private void showDetailedAnalysis(MedicationAssessment assessment) {
        if (assessment == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select an assessment to view!");
            return;
        }

        if (!assessment.isQuestionnaireCompleted()) {
            showAlert(AlertType.INFORMATION, "Incomplete",
                    "This assessment hasn't been completed by the patient yet.");
            return;
        }

        // Get responses and run analysis
        List<AssessmentResponse> responses = assessmentDAO.getResponsesForAssessment(
                assessment.getMonitoringId()
        );

        if (responses.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "No Data", "No responses found!");
            return;
        }

        AnalysisResult result = AIAnalysisEngine.analyzeResponses(responses);

        showAnalysisResults(assessment, result);
    }

    private void showAnalysisResults(MedicationAssessment assessment, AnalysisResult result) {
        Stage reportStage = new Stage();
        reportStage.setTitle("📊 AI Analysis Report - " + assessment.getPatientName());

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");

        VBox reportBox = new VBox(20);
        reportBox.setPadding(new Insets(30));
        reportBox.setStyle("-fx-background-color: white;");

        // Header
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(20));
        headerBox.setStyle("-fx-background-color: " +
                AIAnalysisEngine.getRiskColor(result.getRiskLevel()) +
                "; -fx-background-radius: 10;");

        Label reportTitle = new Label("🤖 AI ANALYSIS REPORT");
        reportTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        reportTitle.setTextFill(Color.WHITE);

        Label riskLabel = new Label("Risk Level: " + result.getRiskLevel());
        riskLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        riskLabel.setTextFill(Color.WHITE);

        if (result.isRequiresImmediateAttention()) {
            Label urgentLabel = new Label("⚠️ REQUIRES IMMEDIATE ATTENTION");
            urgentLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            urgentLabel.setTextFill(Color.YELLOW);
            headerBox.getChildren().add(urgentLabel);
        }
        // Send critical alert if risk is high
        if (result.isRequiresImmediateAttention()) {
            new Thread(() -> {
                try {
                    SessionManager session = SessionManager.getInstance();
                    User currentUser = session.getCurrentUser();

                    if (currentUser != null && currentUser.getEmail() != null) {
                        // Build concerns summary
                        StringBuilder concerns = new StringBuilder();
                        for (String category : result.getKeyFindings().keySet()) {
                            concerns.append("• ").append(result.getKeyFindings().get(category)).append("\n");
                        }

                        EmailService.sendCriticalAlertEmail(
                                currentUser.getEmail(),
                                currentUser.getFullName(),
                                assessment.getPatientName(),
                                assessment.getMedicationName(),
                                result.getRiskLevel(),
                                concerns.toString()
                        );
                    }
                } catch (Exception e) {
                    System.err.println("Error sending critical alert email: " + e.getMessage());
                }
            }).start();
        }

        headerBox.getChildren().addAll(reportTitle, riskLabel);

        // Patient Info
        VBox patientInfo = createReportSection("👤 Patient Information",
                "Patient: " + assessment.getPatientName() + "\n" +
                        "Medication: " + assessment.getMedicationName() + " " + assessment.getDosage() + "\n" +
                        "Assessment Day: " + assessment.getAssessmentDay() + "\n" +
                        "Overall Sentiment: " + result.getOverallSentiment()
        );

        // Score Cards
        HBox scoreCards = new HBox(15);
        scoreCards.setAlignment(Pos.CENTER);

        scoreCards.getChildren().addAll(
                createScoreCard("💊 Adherence", result.getAdherenceScore(),
                        result.getAdherenceScore() >= 70),
                createScoreCard("⚕️ Side Effects", result.getSideEffectScore(),
                        result.getSideEffectScore() < 50),
                createScoreCard("✅ Efficacy", result.getEfficacyScore(),
                        result.getEfficacyScore() >= 60)
        );

        // Key Findings
        VBox findingsBox = createReportSection("🔍 Key Findings", "");
        for (String category : result.getKeyFindings().keySet()) {
            Label finding = new Label("• " + result.getKeyFindings().get(category));
            finding.setFont(Font.font("System", 14));
            finding.setWrapText(true);
            finding.setPadding(new Insets(5, 0, 5, 20));
            findingsBox.getChildren().add(finding);
        }

        // Recommendations
        VBox recommendationBox = createReportSection("💡 Clinical Recommendations",
                result.getRecommendation());

        // Action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(20, 0, 0, 0));

        Button viewResponsesBtn = new Button("📝 View All Patient Responses");
        viewResponsesBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-font-size: 14px;");
        viewResponsesBtn.setOnAction(e -> showAllResponses(assessment));

        Button closeBtn = new Button("✅ Close Report");
        closeBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-font-size: 14px;");
        closeBtn.setOnAction(e -> reportStage.close());

        actionButtons.getChildren().addAll(viewResponsesBtn, closeBtn);

        reportBox.getChildren().addAll(headerBox, patientInfo, scoreCards,
                findingsBox, recommendationBox, actionButtons);

        scrollPane.setContent(reportBox);

        Scene scene = new Scene(scrollPane, 800, 700);
        reportStage.setScene(scene);
        reportStage.show();
    }

    private VBox createReportSection(String title, String content) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8; " +
                "-fx-border-color: #ddd; -fx-border-radius: 8;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        if (!content.isEmpty()) {
            Label contentLabel = new Label(content);
            contentLabel.setFont(Font.font("System", 14));
            contentLabel.setWrapText(true);
            section.getChildren().addAll(titleLabel, contentLabel);
        } else {
            section.getChildren().add(titleLabel);
        }

        return section;
    }

    private VBox createScoreCard(String title, double score, boolean isGood) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);

        String color = isGood ? "#4CAF50" : (score > 50 ? "#FF9800" : "#f44336");
        card.setStyle("-fx-background-color: " + color + "20; -fx-background-radius: 10; " +
                "-fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 10;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label scoreLabel = new Label(String.format("%.0f%%", score));
        scoreLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        scoreLabel.setStyle("-fx-text-fill: " + color + ";");

        ProgressBar progressBar = new ProgressBar(score / 100.0);
        progressBar.setPrefWidth(180);
        progressBar.setStyle("-fx-accent: " + color + ";");

        card.getChildren().addAll(titleLabel, scoreLabel, progressBar);
        return card;
    }

    private void showAllResponses(MedicationAssessment assessment) {
        List<AssessmentResponse> responses = assessmentDAO.getResponsesForAssessment(
                assessment.getMonitoringId()
        );

        Stage responseStage = new Stage();
        responseStage.setTitle("Patient Responses - " + assessment.getPatientName());

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        VBox responsesBox = new VBox(15);
        responsesBox.setPadding(new Insets(20));
        responsesBox.setStyle("-fx-background-color: white;");

        Label header = new Label("📝 All Patient Responses");
        header.setFont(Font.font("System", FontWeight.BOLD, 20));

        responsesBox.getChildren().add(header);

        int qNum = 1;
        for (AssessmentResponse response : responses) {
            VBox responseBox = new VBox(5);
            responseBox.setPadding(new Insets(15));
            responseBox.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 5;");

            Label question = new Label("Q" + qNum++ + ": " + response.getQuestionText());
            question.setFont(Font.font("System", FontWeight.BOLD, 14));
            question.setWrapText(true);

            Label answer = new Label("A: " + response.getResponseText());
            answer.setFont(Font.font("System", 13));
            answer.setWrapText(true);
            answer.setPadding(new Insets(5, 0, 0, 15));

            responseBox.getChildren().add(question);

            if (response.getResponseScore() != null) {
                Label score = new Label("Rating: " + response.getResponseScore() + "/10");
                score.setFont(Font.font("System", FontWeight.BOLD, 13));
                score.setStyle("-fx-text-fill: #667eea;");
                responseBox.getChildren().add(score);
            }

            responseBox.getChildren().add(answer);

            if (response.getSentiment() != null) {
                Label sentiment = new Label("Sentiment: " + response.getSentiment());
                sentiment.setFont(Font.font("System", 12));
                sentiment.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
                responseBox.getChildren().add(sentiment);
            }

            responsesBox.getChildren().add(responseBox);
        }

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-padding: 10 30;");
        closeBtn.setOnAction(e -> responseStage.close());

        HBox buttonBox = new HBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        responsesBox.getChildren().add(buttonBox);

        scrollPane.setContent(responsesBox);

        Scene scene = new Scene(scrollPane, 700, 600);
        responseStage.setScene(scene);
        responseStage.show();
    }

    private void refreshAssessmentTable() {
        assessmentTable.getItems().clear();
        assessmentTable.getItems().addAll(assessmentDAO.getCompletedAssessments());
        System.out.println("🔄 Assessment table refreshed");
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