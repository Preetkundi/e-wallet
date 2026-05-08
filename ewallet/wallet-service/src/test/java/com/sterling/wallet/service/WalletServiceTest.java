package com.sterling.wallet.service;

import com.sterling.wallet.dto.AddMoneyRequest;
import com.sterling.wallet.dto.TransferRequest;
import com.sterling.wallet.dto.WalletResponse;
import com.sterling.wallet.entity.Wallet;
import com.sterling.wallet.entity.WalletStatus;
import com.sterling.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Wallet Service Tests")
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet mockWallet;
    private Wallet receiverWallet;

    @BeforeEach
    void setUp() {
        mockWallet = Wallet.builder()
                .id(1L)
                .userId(100L)
                .walletNumber("STRL123456789ABC")
                .balance(new BigDecimal("1000.00"))
                .status(WalletStatus.ACTIVE)
                .build();

        receiverWallet = Wallet.builder()
                .id(2L)
                .userId(200L)
                .walletNumber("STRLDEF456789GHI")
                .balance(new BigDecimal("500.00"))
                .status(WalletStatus.ACTIVE)
                .build();

        // Set timestamps
        try {
            var f1 = Wallet.class.getDeclaredField("createdAt");
            f1.setAccessible(true);
            f1.set(mockWallet, LocalDateTime.now());
            f1.set(receiverWallet, LocalDateTime.now());

            var f2 = Wallet.class.getDeclaredField("updatedAt");
            f2.setAccessible(true);
            f2.set(mockWallet, LocalDateTime.now());
            f2.set(receiverWallet, LocalDateTime.now());
        } catch (Exception ignored) {}
    }

    // ── Create Wallet ────────────────────────────────────────────────────────

    @Test
    @DisplayName("CreateWallet: Success - wallet created for new user")
    void createWallet_Success() {
        when(walletRepository.existsByUserId(100L)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenReturn(mockWallet);

        WalletResponse response = walletService.createWallet(100L);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(100L);
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO.max(response.getBalance()));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("CreateWallet: Fail - wallet already exists")
    void createWallet_AlreadyExists_ThrowsException() {
        when(walletRepository.existsByUserId(100L)).thenReturn(true);

        assertThatThrownBy(() -> walletService.createWallet(100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Wallet already exists");
    }

    // ── Add Money ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AddMoney: Success - balance updated correctly")
    void addMoney_Success() {
        when(walletRepository.findByUserId(100L)).thenReturn(Optional.of(mockWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(mockWallet);

        AddMoneyRequest request = new AddMoneyRequest(new BigDecimal("500.00"));
        WalletResponse response = walletService.addMoney(100L, request);

        assertThat(mockWallet.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        verify(walletRepository).save(mockWallet);
    }

    // ── Transfer ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Transfer: Success - funds moved between wallets")
    void transfer_Success() {
        when(walletRepository.findByUserId(100L)).thenReturn(Optional.of(mockWallet));
        when(walletRepository.findByUserId(200L)).thenReturn(Optional.of(receiverWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferRequest request = TransferRequest.builder()
                .senderUserId(100L)
                .receiverUserId(200L)
                .amount(new BigDecimal("300.00"))
                .build();

        walletService.transfer(request);

        assertThat(mockWallet.getBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
        assertThat(receiverWallet.getBalance()).isEqualByComparingTo(new BigDecimal("800.00"));
        verify(walletRepository, times(2)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Transfer: Fail - insufficient balance")
    void transfer_InsufficientBalance_ThrowsException() {
        when(walletRepository.findByUserId(100L)).thenReturn(Optional.of(mockWallet));
        when(walletRepository.findByUserId(200L)).thenReturn(Optional.of(receiverWallet));

        TransferRequest request = TransferRequest.builder()
                .senderUserId(100L)
                .receiverUserId(200L)
                .amount(new BigDecimal("5000.00"))
                .build();

        assertThatThrownBy(() -> walletService.transfer(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    @DisplayName("Transfer: Fail - same wallet transfer")
    void transfer_SameWallet_ThrowsException() {
        TransferRequest request = TransferRequest.builder()
                .senderUserId(100L)
                .receiverUserId(100L)
                .amount(new BigDecimal("100.00"))
                .build();

        assertThatThrownBy(() -> walletService.transfer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot transfer to the same wallet");
    }

    // ── Debit / Credit ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Debit: Success - balance reduced")
    void debit_Success() {
        when(walletRepository.findByUserId(100L)).thenReturn(Optional.of(mockWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(mockWallet);

        walletService.debit(100L, new BigDecimal("200.00"));

        assertThat(mockWallet.getBalance()).isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    @DisplayName("Credit: Success - balance increased")
    void credit_Success() {
        when(walletRepository.findByUserId(100L)).thenReturn(Optional.of(mockWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(mockWallet);

        walletService.credit(100L, new BigDecimal("250.00"));

        assertThat(mockWallet.getBalance()).isEqualByComparingTo(new BigDecimal("1250.00"));
    }
}
