package com.example.demo.common.exception;

public class BusinessRuleViolationException extends BusinessException {

    public BusinessRuleViolationException(String rule, String message) {
        super("BUSINESS_RULE_VIOLATION", message, rule);
    }

    public BusinessRuleViolationException(String rule, String message, Object... args) {
        super("BUSINESS_RULE_VIOLATION", message, rule, args);
    }
}