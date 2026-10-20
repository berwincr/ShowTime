package com.showtime.exception;

import org.springframework.http.HttpStatus;

/** Thrown by services; the global handler turns it into {"error": "..."} with the right HTTP status. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
