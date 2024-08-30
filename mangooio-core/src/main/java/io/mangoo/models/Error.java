package io.mangoo.models;

import java.util.Objects;

public class Error {
    private String message;

    public Error(String message) {
        this.message = Objects.requireNonNull(message, "message can not be null");
    }

    public static Error of(String message) {
        return new Error(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
