package com.muchemi.expense_tracker.exceptions;

public class ResourceConflictException extends RuntimeException{
    public ResourceConflictException(String message) {
        super(message);
    }
}
