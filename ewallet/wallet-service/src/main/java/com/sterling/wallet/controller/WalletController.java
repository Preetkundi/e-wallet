package com.sterling.wallet.controller;

import com.sterling.wallet.dto.AddMoneyRequest;
import com.sterling.wallet.dto.TransferRequest;
import com.sterling.wallet.dto.WalletResponse;
import com.sterling.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST endpoints for wallet management.
 * Called directly by clients and internally by Transaction Service via Feign.
 */
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * POST /api/wallets/create/{userId}
     * Create a new wallet for a user (called after registration).
     */
    @PostMapping("/create/{userId}")
    public ResponseEntity<WalletResponse> createWallet(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createWallet(userId));
    }

    /**
     * GET /api/wallets/{userId}
     * Fetch wallet details for a user.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    /**
     * GET /api/wallets/{userId}/balance
     * Get just the balance.
     */
    @GetMapping("/{userId}/balance")
    public ResponseEntity<Map<String, BigDecimal>> getBalance(@PathVariable Long userId) {
        BigDecimal balance = walletService.getBalance(userId);
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    /**
     * PUT /api/wallets/{userId}/add
     * Add money to the wallet (top-up).
     */
    @PutMapping("/{userId}/add")
    public ResponseEntity<WalletResponse> addMoney(
            @PathVariable Long userId,
            @Valid @RequestBody AddMoneyRequest request) {
        return ResponseEntity.ok(walletService.addMoney(userId, request));
    }

    /**
     * POST /api/wallets/transfer
     * Transfer between two wallets — called by Transaction Service via Feign.
     */
    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transfer(@Valid @RequestBody TransferRequest request) {
        walletService.transfer(request);
        return ResponseEntity.ok(Map.of("message", "Transfer successful"));
    }

    /**
     * PUT /api/wallets/{userId}/debit
     * Debit wallet — used by Transaction Service for payments.
     */
    @PutMapping("/{userId}/debit")
    public ResponseEntity<WalletResponse> debit(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(walletService.debit(userId, amount));
    }

    /**
     * PUT /api/wallets/{userId}/credit
     * Credit wallet — used for refunds or incoming payments.
     */
    @PutMapping("/{userId}/credit")
    public ResponseEntity<WalletResponse> credit(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(walletService.credit(userId, amount));
    }
}
