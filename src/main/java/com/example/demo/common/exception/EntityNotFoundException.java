package com.example.demo.common.exception;

public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(String entityType, Object id) {
        super("ENTITY_NOT_FOUND",
              String.format("Entity %s not found with id: %s", entityType, id),
              entityType, id);
    }

    public EntityNotFoundException(String entityType, String field, Object value) {
        super("ENTITY_NOT_FOUND",
              String.format("Entity %s not found with %s: %s", entityType, field, value),
              entityType, field, value);
    }
}