package com.sterling.wallet.exception;

/**
 * Thrown when no wallet is found for the given userId or walletNumber.
 */
public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String message) {
        super(message);
    }

    public WalletNotFoundException(Long userId) {
        super("Wallet not found for userId: " + userId);
    }
}
