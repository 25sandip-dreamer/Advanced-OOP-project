package com.healthcare.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private int messageId;
    private int conversationId;
    private int senderId;
    private String senderType;
    private String messageText;
    private LocalDateTime sentAt;
    private boolean isRead;
    private String senderName;

    public ChatMessage() {
    }

    public ChatMessage(int conversationId, int senderId, String senderType, String messageText) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.messageText = messageText;
    }

    // Getters and Setters
    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    // Helper methods
    public String getFormattedTime() {
        if (sentAt == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return sentAt.format(formatter);
    }
}