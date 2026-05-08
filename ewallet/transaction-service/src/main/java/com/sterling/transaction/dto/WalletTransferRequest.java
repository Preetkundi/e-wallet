package com.sterling.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO passed to Wallet Service via Feign for transfer operations.
 * Mirrors wallet-service's TransferRequest.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransferRequest {
    private Long senderUserId;
    private Long receiverUserId;
    private BigDecimal amount;
}
