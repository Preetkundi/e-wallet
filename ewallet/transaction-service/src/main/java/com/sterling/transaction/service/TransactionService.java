package com.sterling.transaction.service;

import com.sterling.transaction.client.WalletClient;
import com.sterling.transaction.dto.*;
import com.sterling.transaction.entity.Transaction;
import com.sterling.transaction.entity.TransactionStatus;
import com.sterling.transaction.entity.TransactionType;
import com.sterling.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Orchestrates all financial transactions.
 * Communicates with Wallet Service via Feign Client for balance operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletClient walletClient;

    /**
     * Execute a P2P wallet transfer.
     * Records the transaction, then calls Wallet Service to move funds.
     */
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        // Create transaction record in PENDING state
        Transaction transaction = Transaction.builder()
                .referenceId(generateReferenceId())
                .senderUserId(request.getSenderUserId())
                .receiverUserId(request.getReceiverUserId())
                .amount(request.getAmount())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description(request.getDescription() != null ? request.getDescription() : "P2P Transfer")
                .build();

        transaction = transactionRepository.save(transaction);

        try {
            // Call Wallet Service via Feign to atomically move funds
            WalletTransferRequest walletRequest = WalletTransferRequest.builder()
                    .senderUserId(request.getSenderUserId())
                    .receiverUserId(request.getReceiverUserId())
                    .amount(request.getAmount())
                    .build();

            walletClient.transfer(walletRequest);

            // Mark as SUCCESS
            transaction.setStatus(TransactionStatus.SUCCESS);
            log.info("Transfer SUCCESS: ref={}, amount={}, from={} to={}",
                    transaction.getReferenceId(), request.getAmount(),
                    request.getSenderUserId(), request.getReceiverUserId());

        } catch (Exception e) {
            // Mark as FAILED — fault isolation: transaction record preserved
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            log.error("Transfer FAILED: ref={}, reason={}", transaction.getReferenceId(), e.getMessage());
        }

        return mapToResponse(transactionRepository.save(transaction));
    }

    /**
     * Process a merchant payment.
     * Debits the user's wallet and records the payment.
     */
    @Transactional
    public TransactionResponse merchantPayment(PaymentRequest request) {
        Transaction transaction = Transaction.builder()
                .referenceId(generateReferenceId())
                .senderUserId(request.getUserId())
                .receiverUserId(request.getMerchantId())
                .amount(request.getAmount())
                .type(TransactionType.MERCHANT_PAYMENT)
                .status(TransactionStatus.PENDING)
                .description("Payment to " + request.getMerchantName()
                        + (request.getDescription() != null ? " - " + request.getDescription() : ""))
                .build();

        transaction = transactionRepository.save(transaction);

        try {
            // Debit user wallet
            walletClient.debit(request.getUserId(), request.getAmount());
            // Credit merchant wallet
            walletClient.credit(request.getMerchantId(), request.getAmount());

            transaction.setStatus(TransactionStatus.SUCCESS);
            log.info("Merchant payment SUCCESS: ref={}, merchant={}, amount={}",
                    transaction.getReferenceId(), request.getMerchantName(), request.getAmount());

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            log.error("Merchant payment FAILED: ref={}, reason={}", transaction.getReferenceId(), e.getMessage());
        }

        return mapToResponse(transactionRepository.save(transaction));
    }

    /**
     * Get full transaction history for a user (sent and received).
     */
    public List<TransactionResponse> getTransactionHistory(Long userId) {
        return transactionRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a transaction by its reference ID.
     */
    public TransactionResponse getByReferenceId(String referenceId) {
        Transaction transaction = transactionRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + referenceId));
        return mapToResponse(transaction);
    }

    /**
     * Get a transaction by ID.
     */
    public TransactionResponse getById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        return mapToResponse(transaction);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String generateReferenceId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .referenceId(t.getReferenceId())
                .senderUserId(t.getSenderUserId())
                .receiverUserId(t.getReceiverUserId())
                .amount(t.getAmount())
                .type(t.getType().name())
                .status(t.getStatus().name())
                .description(t.getDescription())
                .failureReason(t.getFailureReason())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
