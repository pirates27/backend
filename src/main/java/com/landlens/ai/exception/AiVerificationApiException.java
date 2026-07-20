package com.landlens.ai.exception;

public class AiVerificationApiException extends RuntimeException {
    public AiVerificationApiException(String message) {
        super(message);
    }

    public AiVerificationApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
