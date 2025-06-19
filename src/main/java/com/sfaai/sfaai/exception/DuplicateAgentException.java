package com.sfaai.sfaai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create a duplicate agent
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateAgentException extends RuntimeException {

    public DuplicateAgentException(String message) {
        super(message);
    }

    public DuplicateAgentException(String message, Throwable cause) {
        super(message, cause);
    }
}
