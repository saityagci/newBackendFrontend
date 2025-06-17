package com.sfaai.sfaai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a webhook validation fails
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class WebhookException extends RuntimeException {

    public WebhookException(String message) {
        super(message);
    }

    public WebhookException(String message, Throwable cause) {
        super(message, cause);
    }
}
