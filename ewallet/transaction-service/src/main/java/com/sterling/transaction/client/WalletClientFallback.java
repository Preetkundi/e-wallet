package com.sterling.transaction.client;

import com.sterling.transaction.dto.WalletTransferRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Fallback implementation for WalletClient.
 * Invoked when Wallet Service is unavailable, providing fault isolation.
 */
@Component
@Slf4j
public class WalletClientFallback implements WalletClient {

    @Override
    public ResponseEntity<Map<String, String>> transfer(WalletTransferRequest request) {
        log.error("Wallet Service unavailable for transfer: {}", request);
        throw new RuntimeException("Wallet Service is currently unavailable. Please try again later.");
    }

    @Override
    public ResponseEntity<Map<String, Object>> debit(Long userId, BigDecimal amount) {
        log.error("Wallet Service unavailable for debit. userId={}, amount={}", userId, amount);
        throw new RuntimeException("Wallet Service is currently unavailable. Please try again later.");
    }

    @Override
    public ResponseEntity<Map<String, Object>> credit(Long userId, BigDecimal amount) {
        log.error("Wallet Service unavailable for credit. userId={}, amount={}", userId, amount);
        throw new RuntimeException("Wallet Service is currently unavailable. Please try again later.");
    }

    @Override
    public ResponseEntity<Map<String, BigDecimal>> getBalance(Long userId) {
        log.error("Wallet Service unavailable for balance check. userId={}", userId);
        throw new RuntimeException("Wallet Service is currently unavailable. Please try again later.");
    }
}
