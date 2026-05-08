package com.sterling.wallet.config;

import com.sterling.wallet.entity.Wallet;
import com.sterling.wallet.entity.WalletStatus;
import com.sterling.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds demo wallets into the H2 database on startup.
 * Wallets correspond to the demo users seeded in User Service.
 * UserIDs: admin=1, simran=2, preetinder=3, allen=4
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final WalletRepository walletRepository;

    @Override
    public void run(String... args) {
        if (walletRepository.count() > 0) {
            return;
        }

        walletRepository.save(Wallet.builder()
                .userId(1L)
                .walletNumber("STRL000000000001")
                .balance(new BigDecimal("10000.00"))
                .status(WalletStatus.ACTIVE)
                .build());

        walletRepository.save(Wallet.builder()
                .userId(2L)
                .walletNumber("STRL000000000002")
                .balance(new BigDecimal("5000.00"))
                .status(WalletStatus.ACTIVE)
                .build());

        walletRepository.save(Wallet.builder()
                .userId(3L)
                .walletNumber("STRL000000000003")
                .balance(new BigDecimal("3000.00"))
                .status(WalletStatus.ACTIVE)
                .build());

        walletRepository.save(Wallet.builder()
                .userId(4L)
                .walletNumber("STRL000000000004")
                .balance(new BigDecimal("0.00"))
                .status(WalletStatus.ACTIVE)
                .build());

        log.info("===================================================");
        log.info("  Demo wallets seeded:");
        log.info("  userId=1  STRL000000000001  Balance: ₹10,000");
        log.info("  userId=2  STRL000000000002  Balance: ₹5,000");
        log.info("  userId=3  STRL000000000003  Balance: ₹3,000");
        log.info("  userId=4  STRL000000000004  Balance: ₹0");
        log.info("===================================================");
    }
}
