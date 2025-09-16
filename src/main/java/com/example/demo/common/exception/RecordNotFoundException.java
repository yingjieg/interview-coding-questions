package com.example.demo.common.exception;

public class RecordNotFoundException extends BusinessException {

    public RecordNotFoundException(String recordType, Object id) {
        super("RECORD_NOT_FOUND",
              String.format("%s record not found with id: %s", recordType, id),
              recordType, id);
    }

    public RecordNotFoundException(String recordType, String field, Object value) {
        super("RECORD_NOT_FOUND",
              String.format("%s record not found with %s: %s", recordType, field, value),
              recordType, field, value);
    }
}