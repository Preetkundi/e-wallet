package com.sterling.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sterling.wallet.dto.AddMoneyRequest;
import com.sterling.wallet.dto.WalletResponse;
import com.sterling.wallet.entity.WalletStatus;
import com.sterling.wallet.service.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
@DisplayName("Wallet Controller Tests")
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletService walletService;

    private WalletResponse mockWalletResponse() {
        return WalletResponse.builder()
                .id(1L)
                .userId(100L)
                .walletNumber("STRL000000000001")
                .balance(new BigDecimal("5000.00"))
                .status(WalletStatus.ACTIVE.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/wallets/create/{userId} - 201 Created")
    void createWallet_Returns201() throws Exception {
        when(walletService.createWallet(100L)).thenReturn(mockWalletResponse());

        mockMvc.perform(post("/api/wallets/create/100"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(100))
                .andExpect(jsonPath("$.walletNumber").value("STRL000000000001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/wallets/{userId} - 200 OK")
    void getWallet_Returns200() throws Exception {
        when(walletService.getWalletByUserId(100L)).thenReturn(mockWalletResponse());

        mockMvc.perform(get("/api/wallets/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(5000.00));
    }

    @Test
    @DisplayName("GET /api/wallets/{userId}/balance - 200 OK returns balance map")
    void getBalance_Returns200() throws Exception {
        when(walletService.getBalance(100L)).thenReturn(new BigDecimal("5000.00"));

        mockMvc.perform(get("/api/wallets/100/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(5000.00));
    }

    @Test
    @DisplayName("PUT /api/wallets/{userId}/add - 200 OK with valid amount")
    void addMoney_Returns200() throws Exception {
        when(walletService.addMoney(eq(100L), any(AddMoneyRequest.class)))
                .thenReturn(mockWalletResponse());

        AddMoneyRequest request = new AddMoneyRequest(new BigDecimal("1000.00"));

        mockMvc.perform(put("/api/wallets/100/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/wallets/{userId}/add - 400 Bad Request when amount is zero")
    void addMoney_Returns400_WhenAmountZero() throws Exception {
        AddMoneyRequest request = new AddMoneyRequest(BigDecimal.ZERO);

        mockMvc.perform(put("/api/wallets/100/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
