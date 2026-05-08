package com.sterling.transaction.service;

import com.sterling.transaction.client.WalletClient;
import com.sterling.transaction.dto.*;
import com.sterling.transaction.entity.Transaction;
import com.sterling.transaction.entity.TransactionStatus;
import com.sterling.transaction.entity.TransactionType;
import com.sterling.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction Service Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletClient walletClient;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction mockTransaction;
    private TransferRequest transferRequest;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        mockTransaction = Transaction.builder()
                .id(1L)
                .referenceId("TXN1234567890ABCD")
                .senderUserId(100L)
                .receiverUserId(200L)
                .amount(new BigDecimal("500.00"))
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .description("P2P Transfer")
                .build();

        setTimestamps(mockTransaction);

        transferRequest = TransferRequest.builder()
                .senderUserId(100L)
                .receiverUserId(200L)
                .amount(new BigDecimal("500.00"))
                .description("Test transfer")
                .build();

        paymentRequest = PaymentRequest.builder()
                .userId(100L)
                .merchantId(300L)
                .amount(new BigDecimal("250.00"))
                .merchantName("Starbucks")
                .description("Coffee")
                .build();
    }

    // ── Transfer Tests ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Transfer: Success - funds moved, status = SUCCESS")
    void transfer_Success() {
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> {
                    Transaction t = inv.getArgument(0);
                    t.setId(1L);
                    setTimestamps(t);
                    return t;
                });
        when(walletClient.transfer(any(WalletTransferRequest.class)))
                .thenReturn(ResponseEntity.ok(Map.of("message", "Transfer successful")));

        TransactionResponse response = transactionService.transfer(transferRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getSenderUserId()).isEqualTo(100L);
        assertThat(response.getReceiverUserId()).isEqualTo(200L);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.getType()).isEqualTo("TRANSFER");

        verify(walletClient).transfer(any(WalletTransferRequest.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class)); // PENDING then SUCCESS
    }

    @Test
    @DisplayName("Transfer: Wallet Service fails - status = FAILED, transaction still saved")
    void transfer_WalletServiceFails_StatusFailed() {
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> {
                    Transaction t = inv.getArgument(0);
                    t.setId(1L);
                    setTimestamps(t);
                    return t;
                });
        when(walletClient.transfer(any(WalletTransferRequest.class)))
                .thenThrow(new RuntimeException("Wallet Service unavailable"));

        TransactionResponse response = transactionService.transfer(transferRequest);

        assertThat(response.getStatus()).isEqualTo("FAILED");
        assertThat(response.getFailureReason()).contains("Wallet Service unavailable");

        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Transfer: Reference ID is generated and unique")
    void transfer_GeneratesReferenceId() {
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> {
                    Transaction t = inv.getArgument(0);
                    t.setId(1L);
                    setTimestamps(t);
                    return t;
                });
        when(walletClient.transfer(any()))
                .thenReturn(ResponseEntity.ok(Map.of("message", "OK")));

        TransactionResponse r1 = transactionService.transfer(transferRequest);
        TransactionResponse r2 = transactionService.transfer(transferRequest);

        assertThat(r1.getReferenceId()).startsWith("TXN");
        assertThat(r2.getReferenceId()).startsWith("TXN");
        assertThat(r1.getReferenceId()).isNotEqualTo(r2.getReferenceId());
    }

    // ── Merchant Payment Tests ───────────────────────────────────────────────

    @Test
    @DisplayName("MerchantPayment: Success - wallet debited and merchant credited")
    void merchantPayment_Success() {
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> {
                    Transaction t = inv.getArgument(0);
                    t.setId(2L);
                    setTimestamps(t);
                    return t;
                });
        when(walletClient.debit(anyLong(), any(BigDecimal.class)))
                .thenReturn(ResponseEntity.ok(Map.of()));
        when(walletClient.credit(anyLong(), any(BigDecimal.class)))
                .thenReturn(ResponseEntity.ok(Map.of()));

        TransactionResponse response = transactionService.merchantPayment(paymentRequest);

        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getType()).isEqualTo("MERCHANT_PAYMENT");
        assertThat(response.getSenderUserId()).isEqualTo(100L);
        assertThat(response.getReceiverUserId()).isEqualTo(300L);
        assertThat(response.getDescription()).contains("Starbucks");

        verify(walletClient).debit(eq(100L), eq(new BigDecimal("250.00")));
        verify(walletClient).credit(eq(300L), eq(new BigDecimal("250.00")));
    }

    @Test
    @DisplayName("MerchantPayment: Wallet debit fails - status = FAILED")
    void merchantPayment_DebitFails_StatusFailed() {
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> {
                    Transaction t = inv.getArgument(0);
                    t.setId(2L);
                    setTimestamps(t);
                    return t;
                });
        when(walletClient.debit(anyLong(), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("Insufficient balance"));

        TransactionResponse response = transactionService.merchantPayment(paymentRequest);

        assertThat(response.getStatus()).isEqualTo("FAILED");
        assertThat(response.getFailureReason()).contains("Insufficient balance");

        // Credit should NOT be called if debit fails
        verify(walletClient, never()).credit(anyLong(), any());
    }

    // ── History & Lookup Tests ───────────────────────────────────────────────

    @Test
    @DisplayName("GetTransactionHistory: Returns list for userId")
    void getTransactionHistory_ReturnsList() {
        Transaction t2 = Transaction.builder()
                .id(2L).referenceId("TXN2").senderUserId(200L).receiverUserId(100L)
                .amount(new BigDecimal("100.00")).type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS).description("Refund").build();
        setTimestamps(t2);

        when(transactionRepository.findAllByUserId(100L))
                .thenReturn(List.of(mockTransaction, t2));

        List<TransactionResponse> history = transactionService.getTransactionHistory(100L);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getReferenceId()).isEqualTo("TXN1234567890ABCD");
    }

    @Test
    @DisplayName("GetByReferenceId: Returns correct transaction")
    void getByReferenceId_Success() {
        when(transactionRepository.findByReferenceId("TXN1234567890ABCD"))
                .thenReturn(Optional.of(mockTransaction));

        TransactionResponse response = transactionService.getByReferenceId("TXN1234567890ABCD");

        assertThat(response.getReferenceId()).isEqualTo("TXN1234567890ABCD");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("GetByReferenceId: Not found throws exception")
    void getByReferenceId_NotFound_ThrowsException() {
        when(transactionRepository.findByReferenceId(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getByReferenceId("TXNINVALID"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Transaction not found");
    }

    @Test
    @DisplayName("GetById: Not found throws exception")
    void getById_NotFound_ThrowsException() {
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Transaction not found");
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private void setTimestamps(Transaction t) {
        try {
            var f1 = Transaction.class.getDeclaredField("createdAt");
            f1.setAccessible(true);
            f1.set(t, LocalDateTime.now());
            var f2 = Transaction.class.getDeclaredField("updatedAt");
            f2.setAccessible(true);
            f2.set(t, LocalDateTime.now());
        } catch (Exception ignored) {}
    }
}
