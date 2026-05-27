package com.unihub.shared.exception;

public class SecurityViolationException extends RuntimeException {
    public SecurityViolationException(String message) {
        super(message);
    }
}
