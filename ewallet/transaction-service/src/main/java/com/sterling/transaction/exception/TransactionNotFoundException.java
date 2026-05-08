package com.sterling.transaction.exception;

/**
 * Thrown when a transaction is not found by ID or reference ID.
 */
public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(String message) {
        super(message);
    }

    public TransactionNotFoundException(Long id) {
        super("Transaction not found with id: " + id);
    }
}
