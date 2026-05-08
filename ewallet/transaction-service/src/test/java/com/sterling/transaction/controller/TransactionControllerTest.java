package com.sterling.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sterling.transaction.dto.PaymentRequest;
import com.sterling.transaction.dto.TransactionResponse;
import com.sterling.transaction.dto.TransferRequest;
import com.sterling.transaction.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@DisplayName("Transaction Controller Tests")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private TransactionResponse successResponse(String type) {
        return TransactionResponse.builder()
                .id(1L)
                .referenceId("TXN1234567890ABCD")
                .senderUserId(100L)
                .receiverUserId(200L)
                .amount(new BigDecimal("500.00"))
                .type(type)
                .status("SUCCESS")
                .description("Test")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/transactions/transfer - 200 OK on success")
    void transfer_Returns200() throws Exception {
        when(transactionService.transfer(any(TransferRequest.class)))
                .thenReturn(successResponse("TRANSFER"));

        TransferRequest request = TransferRequest.builder()
                .senderUserId(100L)
                .receiverUserId(200L)
                .amount(new BigDecimal("500.00"))
                .description("Test transfer")
                .build();

        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.referenceId").value("TXN1234567890ABCD"))
                .andExpect(jsonPath("$.type").value("TRANSFER"));
    }

    @Test
    @DisplayName("POST /api/transactions/transfer - 400 Bad Request when amount missing")
    void transfer_Returns400_WhenAmountMissing() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .senderUserId(100L)
                .receiverUserId(200L)
                .build(); // no amount

        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/transactions/payment - 200 OK on merchant payment")
    void merchantPayment_Returns200() throws Exception {
        when(transactionService.merchantPayment(any(PaymentRequest.class)))
                .thenReturn(successResponse("MERCHANT_PAYMENT"));

        PaymentRequest request = PaymentRequest.builder()
                .userId(100L)
                .merchantId(300L)
                .amount(new BigDecimal("250.00"))
                .merchantName("Starbucks")
                .build();

        mockMvc.perform(post("/api/transactions/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("MERCHANT_PAYMENT"));
    }

    @Test
    @DisplayName("GET /api/transactions/history/{userId} - 200 OK returns list")
    void getHistory_Returns200() throws Exception {
        when(transactionService.getTransactionHistory(100L))
                .thenReturn(List.of(successResponse("TRANSFER")));

        mockMvc.perform(get("/api/transactions/history/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderUserId").value(100));
    }

    @Test
    @DisplayName("GET /api/transactions/ref/{refId} - 200 OK returns transaction")
    void getByReferenceId_Returns200() throws Exception {
        when(transactionService.getByReferenceId("TXN1234567890ABCD"))
                .thenReturn(successResponse("TRANSFER"));

        mockMvc.perform(get("/api/transactions/ref/TXN1234567890ABCD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceId").value("TXN1234567890ABCD"));
    }
}
