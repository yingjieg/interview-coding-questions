package com.example.demo.payment.exception;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.BusinessRuleCode;

public class StripePaymentException extends BusinessException {

    public StripePaymentException(String message) {
        super(BusinessRuleCode.PAYMENT_PROCESSING_FAILED.getCode(), message);
    }

    public StripePaymentException(String message, Throwable cause) {
        super(BusinessRuleCode.PAYMENT_PROCESSING_FAILED.getCode(), message, cause);
    }
}