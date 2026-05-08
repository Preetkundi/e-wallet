package com.sterling.transaction.controller;

import com.sterling.transaction.dto.PaymentRequest;
import com.sterling.transaction.dto.TransactionResponse;
import com.sterling.transaction.dto.TransferRequest;
import com.sterling.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for transaction operations.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * POST /api/transactions/transfer
     * Initiate a P2P wallet transfer.
     */
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.transfer(request);
        HttpStatus status = "SUCCESS".equals(response.getStatus()) ? HttpStatus.OK : HttpStatus.PAYMENT_REQUIRED;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * POST /api/transactions/payment
     * Make a merchant payment.
     */
    @PostMapping("/payment")
    public ResponseEntity<TransactionResponse> merchantPayment(@Valid @RequestBody PaymentRequest request) {
        TransactionResponse response = transactionService.merchantPayment(request);
        HttpStatus status = "SUCCESS".equals(response.getStatus()) ? HttpStatus.OK : HttpStatus.PAYMENT_REQUIRED;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * GET /api/transactions/history/{userId}
     * Get full transaction history for a user.
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<TransactionResponse>> getHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(userId));
    }

    /**
     * GET /api/transactions/{id}
     * Get a transaction by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    /**
     * GET /api/transactions/ref/{referenceId}
     * Get a transaction by reference ID.
     */
    @GetMapping("/ref/{referenceId}")
    public ResponseEntity<TransactionResponse> getByReferenceId(@PathVariable String referenceId) {
        return ResponseEntity.ok(transactionService.getByReferenceId(referenceId));
    }
}
