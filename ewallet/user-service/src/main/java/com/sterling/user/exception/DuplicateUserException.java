package com.sterling.user.exception;

/**
 * Thrown when registration is attempted with an already-used email or phone.
 */
public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String message) {
        super(message);
    }
}
