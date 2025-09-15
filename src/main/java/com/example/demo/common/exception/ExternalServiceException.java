package com.example.demo.common.exception;

public class ExternalServiceException extends BusinessException {

    public ExternalServiceException(String serviceName, String message) {
        super("EXTERNAL_SERVICE_ERROR",
              String.format("External service %s error: %s", serviceName, message),
              serviceName);
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR",
              String.format("External service %s error: %s", serviceName, message),
              cause, serviceName);
    }
}