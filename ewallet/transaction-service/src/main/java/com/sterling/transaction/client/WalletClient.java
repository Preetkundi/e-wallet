package com.sterling.transaction.client;

import com.sterling.transaction.dto.WalletTransferRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Feign client for Wallet Service.
 * Spring Cloud Eureka resolves "wallet-service" to the registered instance.
 * No hardcoded URLs — fully dynamic routing!
 */
@FeignClient(name = "wallet-service", fallback = WalletClientFallback.class)
public interface WalletClient {

    @PostMapping("/api/wallets/transfer")
    ResponseEntity<Map<String, String>> transfer(@RequestBody WalletTransferRequest request);

    @PutMapping("/api/wallets/{userId}/debit")
    ResponseEntity<Map<String, Object>> debit(
            @PathVariable("userId") Long userId,
            @RequestParam("amount") BigDecimal amount);

    @PutMapping("/api/wallets/{userId}/credit")
    ResponseEntity<Map<String, Object>> credit(
            @PathVariable("userId") Long userId,
            @RequestParam("amount") BigDecimal amount);

    @GetMapping("/api/wallets/{userId}/balance")
    ResponseEntity<Map<String, BigDecimal>> getBalance(@PathVariable("userId") Long userId);
}
