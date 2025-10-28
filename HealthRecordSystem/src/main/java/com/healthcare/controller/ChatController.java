package com.healthcare.controller;

import com.healthcare.database.ChatDAO;
import com.healthcare.database.PatientDAO;
import com.healthcare.database.SessionManager;
import com.healthcare.database.UserDAO;
import com.healthcare.model.ChatConversation;
import com.healthcare.model.ChatMessage;
import com.healthcare.model.Patient;
import com.healthcare.model.User;
import javafx.application.Application;
import javafx.application.Platform;
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
import java.util.Timer;
import java.util.TimerTask;

/**
 * ChatController - Simple Doctor-Patient Chat System
 * Location: src/com/healthcare/controller/ChatController.java
 */
public class ChatController extends Application {

    private ChatDAO chatDAO = new ChatDAO();
    private PatientDAO patientDAO = new PatientDAO();
    private UserDAO userDAO = new UserDAO();

    private User currentUser;
    private ListView<ChatConversation> conversationList;
    private ListView<ChatMessage> messageList;
    private TextField messageField;
    private Label chatHeader;

    private ChatConversation currentConversation;
    private Timer refreshTimer;

    public ChatController() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("💬 Chat - " + currentUser.getFullName());

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Left side - Conversation list
        VBox leftPanel = createConversationPanel();
        leftPanel.setPrefWidth(300);

        // Right side - Chat messages
        VBox rightPanel = createChatPanel();

        root.setLeft(leftPanel);
        root.setCenter(rightPanel);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load conversations
        loadConversations();

        // Start auto-refresh (every 3 seconds)
        startAutoRefresh();

        // Stop refresh on close
        primaryStage.setOnCloseRequest(e -> stopAutoRefresh());
    }

    /**
     * Create left panel with conversation list
     */
    private VBox createConversationPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");

        // Header
        Label header = new Label("💬 Conversations");
        header.setFont(Font.font("System", FontWeight.BOLD, 18));
        header.setStyle("-fx-text-fill: #333;");

        // New conversation button (for doctors)
        Button newChatBtn = new Button("➕ New Chat");
        newChatBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 8 15;");
        newChatBtn.setMaxWidth(Double.MAX_VALUE);
        newChatBtn.setOnAction(e -> showNewConversationDialog());

        // Conversation list
        conversationList = new ListView<>();
        conversationList.setCellFactory(lv -> new ConversationCell());
        conversationList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                openConversation(newVal);
            }
        });
        VBox.setVgrow(conversationList, Priority.ALWAYS);

        // Refresh button
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8 15;");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        refreshBtn.setOnAction(e -> loadConversations());

        panel.getChildren().addAll(header, newChatBtn, conversationList, refreshBtn);
        return panel;
    }

    /**
     * Create right panel with chat messages
     */
    private VBox createChatPanel() {
        VBox panel = new VBox();
        panel.setStyle("-fx-background-color: #f9f9f9;");

        // Chat header
        HBox headerBox = new HBox(15);
        headerBox.setPadding(new Insets(15));
        headerBox.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        headerBox.setAlignment(Pos.CENTER_LEFT);

        chatHeader = new Label("Select a conversation");
        chatHeader.setFont(Font.font("System", FontWeight.BOLD, 16));
        chatHeader.setStyle("-fx-text-fill: #333;");

        headerBox.getChildren().add(chatHeader);

        // Message list
        messageList = new ListView<>();
        messageList.setCellFactory(lv -> new MessageCell());
        messageList.setStyle("-fx-background-color: #f9f9f9;");
        VBox.setVgrow(messageList, Priority.ALWAYS);

        // Message input area
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(15));
        inputBox.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");

        messageField = new TextField();
        messageField.setPromptText("Type your message...");
        messageField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
        messageField.setDisable(true);
        HBox.setHgrow(messageField, Priority.ALWAYS);

        Button sendBtn = new Button("📤 Send");
        sendBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 25;");
        sendBtn.setDisable(true);
        sendBtn.setOnAction(e -> sendMessage());

        // Enable send on Enter key
        messageField.setOnAction(e -> {
            if (!messageField.isDisabled()) {
                sendMessage();
            }
        });

        // Enable/disable send button based on text
        messageField.textProperty().addListener((obs, old, val) -> {
            sendBtn.setDisable(val.trim().isEmpty());
        });

        inputBox.getChildren().addAll(messageField, sendBtn);

        panel.getChildren().addAll(headerBox, messageList, inputBox);
        return panel;
    }

    /**
     * Load conversations for current user
     */
    private void loadConversations() {
        String userType = currentUser.getRole().toString().equals("DOCTOR") ? "DOCTOR" : "PATIENT";
        List<ChatConversation> conversations = chatDAO.getConversationsForUser(currentUser.getUserId(), userType);

        Platform.runLater(() -> {
            conversationList.getItems().clear();
            conversationList.getItems().addAll(conversations);

            if (conversations.isEmpty()) {
                conversationList.setPlaceholder(new Label("No conversations yet\nClick 'New Chat' to start"));
            }
        });
    }

    /**
     * Open a conversation
     */
    private void openConversation(ChatConversation conversation) {
        currentConversation = conversation;

        // Update header
        String otherPerson = currentUser.getRole().toString().equals("DOCTOR")
                ? conversation.getPatientName()
                : "Dr. " + conversation.getDoctorName();
        chatHeader.setText("💬 " + otherPerson);

        // Load messages
        loadMessages();

        // Enable input
        messageField.setDisable(false);
        messageField.clear();
        messageField.requestFocus();

        // Mark messages as read
        chatDAO.markMessagesAsRead(conversation.getConversationId(), currentUser.getUserId());
    }

    /**
     * Load messages for current conversation
     */
    private void loadMessages() {
        if (currentConversation == null) return;

        List<ChatMessage> messages = chatDAO.getMessages(currentConversation.getConversationId());

        Platform.runLater(() -> {
            messageList.getItems().clear();
            messageList.getItems().addAll(messages);

            // Scroll to bottom
            if (!messages.isEmpty()) {
                messageList.scrollTo(messages.size() - 1);
            }

            if (messages.isEmpty()) {
                messageList.setPlaceholder(new Label("No messages yet\nStart the conversation!"));
            }
        });
    }

    /**
     * Send a message
     */
    private void sendMessage() {
        if (currentConversation == null || messageField.getText().trim().isEmpty()) {
            return;
        }

        String messageText = messageField.getText().trim();
        String senderType = currentUser.getRole().toString().equals("DOCTOR") ? "DOCTOR" : "PATIENT";

        ChatMessage message = new ChatMessage(
                currentConversation.getConversationId(),
                currentUser.getUserId(),
                senderType,
                messageText
        );

        if (chatDAO.sendMessage(message)) {
            messageField.clear();
            loadMessages();
            System.out.println("✅ Message sent!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to send message!");
        }
    }

    /**
     * Show dialog to start new conversation (for doctors)
     */
    private void showNewConversationDialog() {
        if (!currentUser.getRole().toString().equals("DOCTOR")) {
            showAlert(Alert.AlertType.WARNING, "Not Available", "Only doctors can start new conversations.");
            return;
        }

        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle("New Conversation");
        dialog.setHeaderText("Select a patient to chat with");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        ComboBox<Patient> patientBox = new ComboBox<>();
        List<Patient> patients = patientDAO.getAllPatients();
        patientBox.getItems().addAll(patients);
        patientBox.setPromptText("Select patient...");
        patientBox.setPrefWidth(300);

        patientBox.setCellFactory(lv -> new ListCell<Patient>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                setText(empty ? "" : patient.getFullName() + " - " + patient.getEmail());
            }
        });
        patientBox.setButtonCell(new ListCell<Patient>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                setText(empty ? "" : patient.getFullName());
            }
        });

        content.getChildren().add(patientBox);
        dialog.getDialogPane().setContent(content);

        ButtonType startButton = new ButtonType("Start Chat", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(startButton, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == startButton) {
                return patientBox.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(patient -> {
            // Create or get conversation
            int convId = chatDAO.getOrCreateConversation(patient.getPatientId(), currentUser.getUserId());
            if (convId > 0) {
                loadConversations();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Chat started with " + patient.getFullName());
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create conversation!");
            }
        });
    }

    /**
     * Start auto-refresh for new messages
     */
    private void startAutoRefresh() {
        refreshTimer = new Timer("ChatRefreshTimer", true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentConversation != null) {
                    loadMessages();
                }
                loadConversations();
            }
        }, 3000, 3000); // Refresh every 3 seconds
    }

    /**
     * Stop auto-refresh
     */
    private void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
    }

    /**
     * Custom cell for conversation list
     */
    private class ConversationCell extends ListCell<ChatConversation> {
        @Override
        protected void updateItem(ChatConversation conv, boolean empty) {
            super.updateItem(conv, empty);

            if (empty || conv == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox box = new VBox(5);
                box.setPadding(new Insets(10));

                String otherPerson = currentUser.getRole().toString().equals("DOCTOR")
                        ? conv.getPatientName()
                        : "Dr. " + conv.getDoctorName();

                Label nameLabel = new Label(otherPerson);
                nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

                Label messageLabel = new Label(conv.getLastMessage() != null ? conv.getLastMessage() : "No messages");
                messageLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

                if (conv.getLastMessage() != null && conv.getLastMessage().length() > 30) {
                    messageLabel.setText(conv.getLastMessage().substring(0, 30) + "...");
                }

                Label timeLabel = new Label(formatTime(conv.getLastMessageAt()));
                timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

                box.getChildren().addAll(nameLabel, messageLabel, timeLabel);

                if (conv.getUnreadCount() > 0) {
                    Label badge = new Label(String.valueOf(conv.getUnreadCount()));
                    badge.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                            "-fx-padding: 2 6; -fx-background-radius: 10; -fx-font-size: 11px;");
                    box.getChildren().add(badge);
                }

                setGraphic(box);
            }
        }
    }

    /**
     * Custom cell for message list
     */
    private class MessageCell extends ListCell<ChatMessage> {
        @Override
        protected void updateItem(ChatMessage msg, boolean empty) {
            super.updateItem(msg, empty);

            if (empty || msg == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox box = new VBox(3);
                box.setPadding(new Insets(5, 10, 5, 10));

                boolean isMyMessage = msg.getSenderId() == currentUser.getUserId();

                // Message bubble
                Label messageLabel = new Label(msg.getMessageText());
                messageLabel.setWrapText(true);
                messageLabel.setMaxWidth(400);
                messageLabel.setPadding(new Insets(10));

                if (isMyMessage) {
                    messageLabel.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                            "-fx-background-radius: 15; -fx-font-size: 13px;");
                    box.setAlignment(Pos.CENTER_RIGHT);
                } else {
                    messageLabel.setStyle("-fx-background-color: white; -fx-text-fill: #333; " +
                            "-fx-background-radius: 15; -fx-font-size: 13px; " +
                            "-fx-border-color: #e0e0e0; -fx-border-width: 1;");
                    box.setAlignment(Pos.CENTER_LEFT);
                }

                // Time label
                Label timeLabel = new Label(msg.getFormattedTime());
                timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 10px;");

                box.getChildren().addAll(messageLabel, timeLabel);
                setGraphic(box);
            }
        }
    }

    /**
     * Format timestamp for display
     */
    private String formatTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate messageDate = dateTime.toLocalDate();

        if (messageDate.equals(today)) {
            return dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
        } else if (messageDate.equals(today.minusDays(1))) {
            return "Yesterday";
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd"));
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