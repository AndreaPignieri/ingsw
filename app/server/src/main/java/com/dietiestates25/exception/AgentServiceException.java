package com.dietiestates25.exception;

public class AgentServiceException extends RuntimeException {
    public AgentServiceException(String message) {
        super(message);
    }

    public AgentServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
