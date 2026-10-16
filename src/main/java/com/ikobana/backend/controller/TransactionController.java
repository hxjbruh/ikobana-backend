package com.ikobana.backend.controller;

import com.ikobana.backend.dto.ApiResponse;
import com.ikobana.backend.dto.DeliveryStatusUpdateRequest;
import com.ikobana.backend.dto.PaymentStatusUpdateRequest;
import com.ikobana.backend.dto.TransactionRequest;
import com.ikobana.backend.dto.TransactionResponse;
import com.ikobana.backend.dto.TransactionSearchResponse;
import com.ikobana.backend.entity.User;
import com.ikobana.backend.exception.InsufficientStockException;
import com.ikobana.backend.exception.ProductNotFoundException;
import com.ikobana.backend.exception.TransactionNotFoundException;
import com.ikobana.backend.exception.UnauthorizedException;
import com.ikobana.backend.exception.UserNotFoundException;
import com.ikobana.backend.repository.UserRepository;
import com.ikobana.backend.security.JwtTokenProvider;
import com.ikobana.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for transaction creation, order status management, search, and reporting.
 */
@RestController
@RequestMapping("/api/transactions")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Autowired
    public TransactionController(
            TransactionService transactionService,
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository
    ) {
        this.transactionService = transactionService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new transaction / order.
     * Requires authenticated user.
     *
     * @param userDetails authenticated user principal
     * @param request     transaction details and item list
     * @return ApiResponse containing created TransactionResponse
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid TransactionRequest request
    ) {
        Long userId = extractUserId(userDetails);
        log.info("Processing transaction creation for userId: {}, customer: {}", userId, request.getCustomerName());

        try {
            TransactionResponse response = transactionService.createTransaction(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Transaction created", response));
        } catch (InsufficientStockException ex) {
            log.warn("Transaction creation failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Stock tidak cukup: " + ex.getMessage()));
        } catch (ProductNotFoundException ex) {
            log.warn("Transaction creation failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Product tidak ditemukan"));
        }
    }

    /**
     * Retrieves detail of a specific transaction.
     * Verifies that the requester is the owner of the transaction or has ADMIN role.
     *
     * @param id          transaction database ID
     * @param userDetails authenticated user principal
     * @return ApiResponse containing TransactionResponse
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Fetching transaction detail for ID: {}", id);
        try {
            TransactionResponse response = transactionService.getTransaction(id);
            Long currentUserId = extractUserId(userDetails);

            boolean isOwner = response.getUserId() != null && response.getUserId().equals(currentUserId);
            boolean isAdmin = isAdmin(userDetails);

            if (!isOwner && !isAdmin) {
                log.warn("Access denied: User ID {} is not authorized to view transaction ID {}", currentUserId, id);
                throw new UnauthorizedException("Anda tidak berhak mengakses transaksi ini");
            }

            return ResponseEntity.ok(ApiResponse.success("Transaction detail", response));
        } catch (TransactionNotFoundException ex) {
            log.warn("Transaction not found with ID {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Transaction not found"));
        } catch (UnauthorizedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Retrieves paginated transactions belonging to the currently authenticated user.
     *
     * @param userDetails authenticated user principal
     * @param page        page index (default 0)
     * @param size        items per page (default 10)
     * @return ApiResponse containing Page of TransactionResponse
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getUserTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = extractUserId(userDetails);
        log.info("Fetching transactions for user ID {}: page={}, size={}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponse> response = transactionService.getUserTransactions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("User transactions", response));
    }

    /**
     * Searches transactions by customer name or phone number.
     * Requires ADMIN role.
     *
     * @param keyword  customer search term
     * @param page     page index (default 0)
     * @param size     items per page (default 20)
     * @return ApiResponse containing Page of TransactionSearchResponse
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        log.info("Admin fetching all transactions: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TransactionResponse> response = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(ApiResponse.success("All transactions", response));
    }

    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<TransactionSearchResponse>>> searchTransactions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Admin searching transactions: keyword='{}', page={}, size={}", keyword, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionSearchResponse> response = transactionService.searchTransactions(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search results", response));
    }

    /**
     * Updates payment status of a transaction.
     * Requires ADMIN role.
     *
     * @param id      transaction ID
     * @param request payment status update data
     * @return ApiResponse containing updated TransactionResponse
     */
    @PutMapping("/{id}/payment-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TransactionResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody @Valid PaymentStatusUpdateRequest request
    ) {
        log.info("Updating payment status for transaction ID {} to {}", id, request.getPaymentStatus());
        try {
            TransactionResponse response = transactionService.updatePaymentStatus(id, request.getPaymentStatus());
            return ResponseEntity.ok(ApiResponse.success("Payment status updated", response));
        } catch (TransactionNotFoundException ex) {
            log.warn("Payment status update rejected: Transaction not found with ID {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Transaction not found"));
        } catch (IllegalStateException ex) {
            log.warn("Invalid payment status transition for transaction ID {}: {}", id, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Updates delivery status of a transaction.
     * Requires ADMIN role.
     *
     * @param id      transaction ID
     * @param request delivery status update data
     * @return ApiResponse containing updated TransactionResponse
     */
    @PutMapping("/{id}/delivery-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestBody @Valid DeliveryStatusUpdateRequest request
    ) {
        log.info("Updating delivery status for transaction ID {} to {}", id, request.getDeliveryStatus());
        try {
            TransactionResponse response = transactionService.updateDeliveryStatus(id, request.getDeliveryStatus());
            return ResponseEntity.ok(ApiResponse.success("Delivery status updated", response));
        } catch (TransactionNotFoundException ex) {
            log.warn("Delivery status update rejected: Transaction not found with ID {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Transaction not found"));
        } catch (IllegalStateException ex) {
            log.warn("Invalid delivery status transition for transaction ID {}: {}", id, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Retrieves transactions created within a specified date range for admin reporting.
     * Requires ADMIN role.
     *
     * @param startDate start datetime
     * @param endDate   end datetime
     * @return ApiResponse containing list of TransactionResponse
     */
    @GetMapping("/report/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("Fetching transaction report from {} to {}", startDate, endDate);
        List<TransactionResponse> response = transactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Transaction report", response));
    }

    // ── Helper Methods ────────────────────────────────────────────────────────

    private Long extractUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedException("User tidak terotentikasi");
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .or(() -> userRepository.findByUsername(userDetails.getUsername()))
                .orElseThrow(() -> new UserNotFoundException("User tidak ditemukan: " + userDetails.getUsername()));
        return user.getId();
    }

    private boolean isAdmin(UserDetails userDetails) {
        if (userDetails == null) return false;
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
    }
}
