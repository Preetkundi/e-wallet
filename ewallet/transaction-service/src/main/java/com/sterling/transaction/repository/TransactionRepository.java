package com.sterling.transaction.repository;

import com.sterling.transaction.entity.Transaction;
import com.sterling.transaction.entity.TransactionStatus;
import com.sterling.transaction.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // All transactions where user is sender or receiver
    @Query("SELECT t FROM Transaction t WHERE t.senderUserId = :userId OR t.receiverUserId = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findAllByUserId(Long userId);

    // Sent transactions
    List<Transaction> findBySenderUserIdOrderByCreatedAtDesc(Long senderUserId);

    // Received transactions
    List<Transaction> findByReceiverUserIdOrderByCreatedAtDesc(Long receiverUserId);

    // By type and user
    List<Transaction> findBySenderUserIdAndTypeOrderByCreatedAtDesc(Long senderUserId, TransactionType type);

    // By status
    List<Transaction> findByStatus(TransactionStatus status);

    // Lookup by reference ID
    Optional<Transaction> findByReferenceId(String referenceId);
}
