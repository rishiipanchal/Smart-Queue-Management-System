package com.rishi.taskqueue.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Entity
public class QueueToken {

    @Id
    @NotNull(message = "ID cannot be null")
    private String id;

    @NotNull(message = "Token number is required")
    private Integer tokenNumber;

    private String userId;

    @NotBlank(message = "Status cannot be empty")
    private String status; // WAITING, SERVING, COMPLETED

    private LocalDateTime createdAt;

    public QueueToken() {}

    public QueueToken(String id, int tokenNumber, String userId, String status) {
        this.id = id;
        this.tokenNumber = tokenNumber;
        this.userId = userId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(Integer tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}