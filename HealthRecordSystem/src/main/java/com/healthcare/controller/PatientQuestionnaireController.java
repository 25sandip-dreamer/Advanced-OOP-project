package com.healthcare.controller;

import com.healthcare.model.PatientMedication;
import com.healthcare.model.User;
import com.healthcare.service.EmailService;
import com.healthcare.database.MedicationDAO;
import com.healthcare.database.UserDAO;
import com.healthcare.database.PatientDAO;
import com.healthcare.database.AssessmentDAO;
import com.healthcare.model.AssessmentResponse;
import com.healthcare.model.MedicationAssessment;
import com.healthcare.controller.QuestionnaireGenerator.Question;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * PatientQuestionnaireController - Patient Medication Assessment Interface
 * Patients complete questionnaires about their medication experience
 */
public class PatientQuestionnaireController extends Application {

    private AssessmentDAO assessmentDAO = new AssessmentDAO();
    private MedicationAssessment currentAssessment;
    private List<Question> questions;
    private List<ResponseCapture> responseCaptures = new ArrayList<>();

    /**
     * Helper class to capture responses
     */
    private static class ResponseCapture {
        Question question;
        TextArea textArea;
        Slider scoreSlider;
        Label scoreLabel;

        ResponseCapture(Question question, TextArea textArea, Slider scoreSlider, Label scoreLabel) {
            this.question = question;
            this.textArea = textArea;
            this.scoreSlider = scoreSlider;
            this.scoreLabel = scoreLabel;
        }

        String getResponseText() {
            return textArea.getText();
        }

        Integer getScore() {
            return scoreSlider != null ? (int) scoreSlider.getValue() : null;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("📝 Medication Assessment Questionnaire");

        // For testing, we'll show assessment selection
        // In real app, this would be called with specific assessment ID
        showAssessmentSelection(primaryStage);
    }

    /**
     * Show assessment selection screen (for testing)
     */
    private void showAssessmentSelection(Stage stage) {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(600);
        contentBox.setPadding(new Insets(40));
        contentBox.setStyle("-fx-background-color: white; -fx-background-radius: 15;");

        Label titleLabel = new Label("📝 Medication Assessment");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #667eea;");

        Label subtitleLabel = new Label("Select an assessment to complete");
        subtitleLabel.setFont(Font.font("System", 16));
        subtitleLabel.setStyle("-fx-text-fill: #666;");

        // Get pending assessments
        List<MedicationAssessment> pendingAssessments = assessmentDAO.getPendingAssessments();

        if (pendingAssessments.isEmpty()) {
            Label noAssessments = new Label("✅ No pending assessments at this time!\nGreat job staying on top of your medication monitoring!");
            noAssessments.setFont(Font.font("System", 14));
            noAssessments.setStyle("-fx-text-fill: #4CAF50; -fx-text-alignment: center;");
            noAssessments.setWrapText(true);

            Button closeBtn = new Button("Close");
            closeBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-padding: 10 30;");
            closeBtn.setOnAction(e -> stage.close());

            contentBox.getChildren().addAll(titleLabel, subtitleLabel, noAssessments, closeBtn);
        } else {
            ListView<MedicationAssessment> assessmentList = new ListView<>();
            assessmentList.setPrefHeight(300);

            for (MedicationAssessment assessment : pendingAssessments) {
                assessmentList.getItems().add(assessment);
            }

            assessmentList.setCellFactory(lv -> new ListCell<MedicationAssessment>() {
                @Override
                protected void updateItem(MedicationAssessment assessment, boolean empty) {
                    super.updateItem(assessment, empty);
                    if (empty || assessment == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        VBox vbox = new VBox(5);
                        Label medLabel = new Label("💊 " + assessment.getMedicationName() + " " + assessment.getDosage());
                        medLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

                        Label patientLabel = new Label("Patient: " + assessment.getPatientName());
                        Label dayLabel = new Label("Day " + assessment.getAssessmentDay() + " Assessment");
                        Label dateLabel = new Label("Due: " + assessment.getAssessmentDate());

                        if (assessment.isOverdue()) {
                            dateLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            dateLabel.setText("⚠️ OVERDUE: " + assessment.getAssessmentDate());
                        }

                        vbox.getChildren().addAll(medLabel, patientLabel, dayLabel, dateLabel);
                        setGraphic(vbox);
                    }
                }
            });

            Button startBtn = new Button("▶️ Start Assessment");
            startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                    "-fx-font-size: 16px; -fx-padding: 15 40; -fx-font-weight: bold;");
            startBtn.setDisable(true);

            assessmentList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                startBtn.setDisable(newVal == null);
            });

            startBtn.setOnAction(e -> {
                MedicationAssessment selected = assessmentList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showQuestionnaire(stage, selected);
                }
            });

            Button cancelBtn = new Button("Cancel");
            cancelBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-padding: 10 30;");
            cancelBtn.setOnAction(e -> stage.close());

            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().addAll(startBtn, cancelBtn);

            contentBox.getChildren().addAll(titleLabel, subtitleLabel, assessmentList, buttonBox);
        }

        layout.getChildren().add(contentBox);

        Scene scene = new Scene(layout, 700, 600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Show the questionnaire form
     */
    private void showQuestionnaire(Stage stage, MedicationAssessment assessment) {
        this.currentAssessment = assessment;
        this.questions = QuestionnaireGenerator.generateQuestionnaire(
                assessment.getMedicationName(),
                assessment.getAssessmentDay()
        );

        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        VBox header = createQuestionnaireHeader(assessment);
        mainLayout.setTop(header);

        // Questions form
        ScrollPane scrollPane = createQuestionsForm();
        mainLayout.setCenter(scrollPane);

        // Submit button
        HBox buttonBar = createSubmitButtonBar(stage);
        mainLayout.setBottom(buttonBar);

        Scene scene = new Scene(mainLayout, 900, 700);
        stage.setScene(scene);
    }

    /**
     * Create questionnaire header
     */
    private VBox createQuestionnaireHeader(MedicationAssessment assessment) {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #667eea;");
        header.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("📝 Day " + assessment.getAssessmentDay() + " Medication Assessment");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: white;");

        Label medLabel = new Label("💊 " + assessment.getMedicationName() + " " + assessment.getDosage());
        medLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        medLabel.setStyle("-fx-text-fill: #FFD700;");

        Label patientLabel = new Label("Patient: " + assessment.getPatientName());
        patientLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Label instructionLabel = new Label("Please answer all questions honestly to help your doctor monitor your treatment");
        instructionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-style: italic;");
        instructionLabel.setWrapText(true);
        instructionLabel.setMaxWidth(600);
        instructionLabel.setAlignment(Pos.CENTER);

        header.getChildren().addAll(titleLabel, medLabel, patientLabel, instructionLabel);
        return header;
    }

    /**
     * Create questions form
     */
    private ScrollPane createQuestionsForm() {
        VBox formBox = new VBox(25);
        formBox.setPadding(new Insets(30));
        formBox.setStyle("-fx-background-color: white;");

        responseCaptures.clear();

        int questionNumber = 1;
        for (Question question : questions) {
            VBox questionBox = createQuestionBox(question, questionNumber++);
            formBox.getChildren().add(questionBox);
        }

        ScrollPane scrollPane = new ScrollPane(formBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");

        return scrollPane;
    }

    /**
     * Create individual question box
     */
    private VBox createQuestionBox(Question question, int questionNumber) {
        VBox questionBox = new VBox(10);
        questionBox.setPadding(new Insets(20));
        questionBox.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        // Question label
        Label questionLabel = new Label(questionNumber + ". " + question.getQuestionText());
        questionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-text-fill: #333;");

        // Response area
        TextArea responseArea = new TextArea();
        responseArea.setPromptText("Type your response here...");
        responseArea.setPrefRowCount(3);
        responseArea.setWrapText(true);
        responseArea.setStyle("-fx-font-size: 13px;");

        Slider scoreSlider = null;
        Label scoreLabel = null;

        // Add rating slider if question requires score
        if (question.requiresScore()) {
            Label ratingLabel = new Label("Rating (1-10):");
            ratingLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

            HBox sliderBox = new HBox(15);
            sliderBox.setAlignment(Pos.CENTER_LEFT);

            scoreSlider = new Slider(1, 10, 5);
            scoreSlider.setShowTickLabels(true);
            scoreSlider.setShowTickMarks(true);
            scoreSlider.setMajorTickUnit(1);
            scoreSlider.setMinorTickCount(0);
            scoreSlider.setBlockIncrement(1);
            scoreSlider.setSnapToTicks(true);
            scoreSlider.setPrefWidth(400);

            scoreLabel = new Label("5");
            scoreLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            scoreLabel.setStyle("-fx-text-fill: #667eea;");

            Label finalScoreLabel = scoreLabel;
            scoreSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                finalScoreLabel.setText(String.valueOf(newVal.intValue()));
            });

            sliderBox.getChildren().addAll(scoreSlider, scoreLabel);

            questionBox.getChildren().addAll(questionLabel, responseArea, ratingLabel, sliderBox);
        } else {
            questionBox.getChildren().addAll(questionLabel, responseArea);
        }

        // Store for later retrieval
        responseCaptures.add(new ResponseCapture(question, responseArea, scoreSlider, scoreLabel));

        return questionBox;
    }

    /**
     * Create submit button bar
     */
    private HBox createSubmitButtonBar(Stage stage) {
        HBox buttonBar = new HBox(15);
        buttonBar.setPadding(new Insets(20));
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setStyle("-fx-background-color: #e0e0e0;");

        Button submitBtn = new Button("✅ Submit Assessment");
        submitBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 15 40; -fx-font-weight: bold;");
        submitBtn.setOnAction(e -> submitAssessment(stage));

        Button cancelBtn = new Button("❌ Cancel");
        cancelBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 30;");
        cancelBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Cancel Assessment");
            confirm.setHeaderText("Are you sure?");
            confirm.setContentText("Your responses will not be saved.");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    showAssessmentSelection(stage);
                }
            });
        });

        buttonBar.getChildren().addAll(submitBtn, cancelBtn);
        return buttonBar;
    }

    /**
     * Submit assessment responses
     */
    private void submitAssessment(Stage stage) {
        // Validate - check if at least some questions are answered
        long answeredCount = responseCaptures.stream()
                .filter(rc -> rc.getResponseText() != null && !rc.getResponseText().trim().isEmpty())
                .count();

        if (answeredCount < questions.size() / 2) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete Assessment");
            alert.setHeaderText("Please answer more questions");
            alert.setContentText("We need your responses to at least half the questions to provide meaningful feedback to your doctor.");
            alert.showAndWait();
            return;
        }

        // Show progress
        Alert progress = new Alert(Alert.AlertType.INFORMATION);
        progress.setTitle("Submitting");
        progress.setHeaderText("Saving your responses...");
        progress.setContentText("Please wait...");
        progress.show();

        // Save all responses
        int savedCount = 0;
        for (ResponseCapture capture : responseCaptures) {
            String responseText = capture.getResponseText();

            if (responseText != null && !responseText.trim().isEmpty()) {
                AssessmentResponse response = new AssessmentResponse();
                response.setMonitoringId(currentAssessment.getMonitoringId());
                response.setQuestionText(capture.question.getQuestionText());
                response.setResponseText(responseText);
                response.setResponseScore(capture.getScore());

                // Basic sentiment analysis (we'll improve this with AI later)
                response.setSentiment(analyzeSentiment(responseText, capture.getScore()));

                if (assessmentDAO.saveResponse(response)) {
                    savedCount++;
                }
            }
        }

        // Mark assessment as completed
        assessmentDAO.markQuestionnaireCompleted(currentAssessment.getMonitoringId());
        // Notify doctor that assessment is completed
        new Thread(() -> {
            try {
                // Get patient info
                PatientDAO patientDAO = new PatientDAO();
                // You'll need to add a method to get patient by name or store patient ID in assessment

                // Get doctor who prescribed the medication
                UserDAO userDAO = new UserDAO();
                MedicationDAO medicationDAO = new MedicationDAO();

                // Get the patient medication to find the prescribing doctor
                List<PatientMedication> medications = medicationDAO.getAllActiveMedications();
                for (PatientMedication med : medications) {
                    if (med.getPatientMedicationId() == currentAssessment.getPatientMedicationId()) {
                        User doctor = userDAO.getUserById(med.getPrescribedBy());
                        if (doctor != null && doctor.getEmail() != null) {
                            EmailService.sendAssessmentCompletedEmail(
                                    doctor.getEmail(),
                                    doctor.getFullName(),
                                    currentAssessment.getPatientName(),
                                    currentAssessment.getMedicationName(),
                                    currentAssessment.getAssessmentDay()
                            );
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error sending assessment completion email: " + e.getMessage());
            }
        }).start();

        progress.close();

        // Show success message
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Assessment Completed!");
        success.setHeaderText("✅ Thank you for completing your assessment!");
        success.setContentText(
                "Your responses have been saved successfully.\n\n" +
                        "Responses saved: " + savedCount + " out of " + questions.size() + " questions\n\n" +
                        "Your doctor will review your feedback and may contact you if needed.\n" +
                        "Continue taking your medication as prescribed."
        );
        success.showAndWait();

        // Return to selection screen
        showAssessmentSelection(stage);
    }

    /**
     * Basic sentiment analysis
     * (Will be enhanced with real AI in next phase)
     */
    private String analyzeSentiment(String text, Integer score) {
        if (text == null) text = "";
        text = text.toLowerCase();

        // If score is available, use it primarily
        if (score != null) {
            if (score >= 7) return "Positive";
            if (score <= 4) return "Negative";
            return "Neutral";
        }

        // Simple keyword-based sentiment analysis
        String[] positiveWords = {"good", "better", "great", "improved", "effective",
                "helpful", "satisfied", "working", "relief"};
        String[] negativeWords = {"bad", "worse", "terrible", "pain", "severe",
                "uncomfortable", "problem", "concern", "worry"};

        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : positiveWords) {
            if (text.contains(word)) positiveCount++;
        }

        for (String word : negativeWords) {
            if (text.contains(word)) negativeCount++;
        }

        if (positiveCount > negativeCount) return "Positive";
        if (negativeCount > positiveCount) return "Negative";
        return "Neutral";
    }

    public static void main(String[] args) {
        launch(args);
    }
}