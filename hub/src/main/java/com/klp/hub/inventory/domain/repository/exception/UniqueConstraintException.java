package com.klp.hub.inventory.domain.repository.exception;

public class UniqueConstraintException extends RuntimeException {
    public UniqueConstraintException(String message) {
        super(message);
    }
}
