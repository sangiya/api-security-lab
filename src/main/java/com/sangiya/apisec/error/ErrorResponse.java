package com.sangiya.apisec.error;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<Map<String, String>> fieldErrors;

    public ErrorResponse(Instant timestamp, int status, String error, String message,
                         String path, List<Map<String, String>> fieldErrors) {
        this.timestamp = Objects.requireNonNull(timestamp);
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public List<Map<String, String>> getFieldErrors() {
        return fieldErrors;
    }
}
