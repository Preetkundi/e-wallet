package com.sterling.wallet.exception;

/**
 * Thrown when a wallet operation fails due to business rules
 * (e.g. insufficient balance, inactive wallet).
 */
public class WalletException extends RuntimeException {

    public WalletException(String message) {
        super(message);
    }
}
