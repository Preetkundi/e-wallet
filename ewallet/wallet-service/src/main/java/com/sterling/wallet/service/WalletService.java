package com.sterling.wallet.service;

import com.sterling.wallet.dto.AddMoneyRequest;
import com.sterling.wallet.dto.TransferRequest;
import com.sterling.wallet.dto.WalletResponse;
import com.sterling.wallet.entity.Wallet;
import com.sterling.wallet.entity.WalletStatus;
import com.sterling.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Core business logic for wallet operations:
 * - Create wallet for a new user
 * - Add money (top-up)
 * - Debit / Credit for transfers
 * - Balance inquiry
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;

    /**
     * Create a new wallet for a user on registration.
     * Called by Transaction Service or directly by User Service via REST.
     */
    @Transactional
    public WalletResponse createWallet(Long userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new IllegalStateException("Wallet already exists for userId: " + userId);
        }

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .walletNumber(generateWalletNumber())
                .balance(BigDecimal.ZERO)
                .status(WalletStatus.ACTIVE)
                .build();

        Wallet saved = walletRepository.save(wallet);
        log.info("Wallet created for userId: {}, walletNumber: {}", userId, saved.getWalletNumber());
        return mapToResponse(saved);
    }

    /**
     * Retrieve wallet details by user ID.
     */
    public WalletResponse getWalletByUserId(Long userId) {
        Wallet wallet = findActiveWalletByUserId(userId);
        return mapToResponse(wallet);
    }

    /**
     * Get current balance for a user.
     */
    public BigDecimal getBalance(Long userId) {
        return findActiveWalletByUserId(userId).getBalance();
    }

    /**
     * Add money to the wallet (top-up from bank/card).
     */
    @Transactional
    public WalletResponse addMoney(Long userId, AddMoneyRequest request) {
        Wallet wallet = findActiveWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        Wallet updated = walletRepository.save(wallet);
        log.info("Added ₹{} to wallet of userId: {}. New balance: ₹{}",
                request.getAmount(), userId, updated.getBalance());
        return mapToResponse(updated);
    }

    /**
     * Transfer funds between two wallets atomically.
     * Debits sender, credits receiver in a single transaction.
     */
    @Transactional
    public void transfer(TransferRequest request) {
        if (request.getSenderUserId().equals(request.getReceiverUserId())) {
            throw new IllegalArgumentException("Cannot transfer to the same wallet");
        }

        Wallet sender = findActiveWalletByUserId(request.getSenderUserId());
        Wallet receiver = findActiveWalletByUserId(request.getReceiverUserId());

        validateSufficientBalance(sender, request.getAmount());

        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        receiver.setBalance(receiver.getBalance().add(request.getAmount()));

        walletRepository.save(sender);
        walletRepository.save(receiver);

        log.info("Transfer of ₹{} from userId:{} to userId:{} completed",
                request.getAmount(), request.getSenderUserId(), request.getReceiverUserId());
    }

    /**
     * Debit a wallet (for merchant payments).
     */
    @Transactional
    public WalletResponse debit(Long userId, BigDecimal amount) {
        Wallet wallet = findActiveWalletByUserId(userId);
        validateSufficientBalance(wallet, amount);
        wallet.setBalance(wallet.getBalance().subtract(amount));
        return mapToResponse(walletRepository.save(wallet));
    }

    /**
     * Credit a wallet (for refunds or incoming payments).
     */
    @Transactional
    public WalletResponse credit(Long userId, BigDecimal amount) {
        Wallet wallet = findActiveWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        return mapToResponse(walletRepository.save(wallet));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Wallet findActiveWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for userId: " + userId));
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Wallet is not active for userId: " + userId);
        }
        return wallet;
    }

    private void validateSufficientBalance(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Insufficient balance. Available: ₹" + wallet.getBalance() + ", Required: ₹" + amount);
        }
    }

    private String generateWalletNumber() {
        return "STRL" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private WalletResponse mapToResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .walletNumber(wallet.getWalletNumber())
                .balance(wallet.getBalance())
                .status(wallet.getStatus().name())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}
