package com.sterling.transaction.entity;

public enum TransactionType {
    TRANSFER,           // P2P wallet transfer
    MERCHANT_PAYMENT,   // Payment to a merchant
    ADD_MONEY,          // Top-up from bank/card
    REFUND              // Refund to wallet
}
