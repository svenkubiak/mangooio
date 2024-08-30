package io.mangoo.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Error {
    private LocalDateTime timestamp;
    private int status;
    private String message;

    public Error(String message, int status) {
        this.timestamp = LocalDateTime.now();
        this.message = Objects.requireNonNull(message, "message can not be null");
        this.status = status;
    }

    public static Error of(String message, int status) {
        return new Error(message, status);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
