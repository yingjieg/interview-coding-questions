package com.example.demo.common.exception;

public class BusinessRuleViolationException extends BusinessException {

    public BusinessRuleViolationException(BusinessRuleCode ruleCode) {
        super("BUSINESS_RULE_VIOLATION", ruleCode.getDefaultMessage(), ruleCode.getCode());
    }

    public BusinessRuleViolationException(BusinessRuleCode ruleCode, String customMessage) {
        super("BUSINESS_RULE_VIOLATION", customMessage, ruleCode.getCode());
    }

}