package com.sterling.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Transaction Service — handles fund transfers and merchant payments.
 * Uses Feign Client to communicate with Wallet Service via Eureka.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }
}
