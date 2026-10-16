package com.ikobana.backend.repository;

import com.ikobana.backend.entity.DeliveryStatus;
import com.ikobana.backend.entity.PaymentStatus;
import com.ikobana.backend.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link Transaction} entity.
 * Provides data access operations for transaction management, filtering, and reporting.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find a transaction by its unique transaction identifier (e.g., #TRX-001).
     *
     * @param transactionId unique transaction code
     * @return Optional containing the Transaction if found, or empty Optional
     */
    Optional<Transaction> findByTransactionId(String transactionId);

    /**
     * Find all transactions processed by a specific user.
     *
     * @param userId ID of the user/admin
     * @return list of matching transactions
     */
    List<Transaction> findByUserId(Long userId);

    /**
     * Find transactions processed by a specific user with pagination, ordered newest first.
     *
     * @param userId ID of the user/admin
     * @param pageable pagination parameters
     * @return page of transactions
     */
    Page<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find transactions by payment status.
     *
     * @param paymentStatus payment status enum (PENDING, COMPLETED, FAILED, REFUNDED)
     * @return list of matching transactions
     */
    List<Transaction> findByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * Find transactions by delivery status.
     *
     * @param deliveryStatus delivery status enum (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
     * @return list of matching transactions
     */
    List<Transaction> findByDeliveryStatus(DeliveryStatus deliveryStatus);

    /**
     * Find transactions within a specified creation date range, ordered newest first.
     *
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of transactions within the date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN ?1 AND ?2 ORDER BY t.createdAt DESC")
    List<Transaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Search transactions by matching keyword against customer name or phone number.
     *
     * @param keyword search keyword
     * @return list of matching transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.customerName LIKE %?1% OR t.customerPhone LIKE %?1%")
    List<Transaction> searchByCustomer(String keyword);

    /**
     * Find transactions by user ID and payment status.
     *
     * @param userId user ID
     * @param paymentStatus payment status enum
     * @return list of matching transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.userId = ?1 AND t.paymentStatus = ?2")
    List<Transaction> findByUserIdAndPaymentStatus(Long userId, PaymentStatus paymentStatus);

    /**
     * Find all transactions with pagination support.
     *
     * @param pageable pagination parameters
     * @return page of transactions
     */
    Page<Transaction> findAll(Pageable pageable);

    /**
     * Find transactions filtered by both payment status and delivery status with pagination.
     *
     * @param paymentStatus payment status enum
     * @param deliveryStatus delivery status enum
     * @param pageable pagination parameters
     * @return page of matching transactions
     */
    Page<Transaction> findByPaymentStatusAndDeliveryStatus(
            PaymentStatus paymentStatus,
            DeliveryStatus deliveryStatus,
            Pageable pageable
    );
}
