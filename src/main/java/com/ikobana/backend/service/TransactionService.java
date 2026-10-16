package com.ikobana.backend.service;

import com.ikobana.backend.dto.TransactionItemRequest;
import com.ikobana.backend.dto.TransactionItemResponse;
import com.ikobana.backend.dto.TransactionRequest;
import com.ikobana.backend.dto.TransactionResponse;
import com.ikobana.backend.dto.TransactionSearchResponse;
import com.ikobana.backend.entity.DeliveryStatus;
import com.ikobana.backend.entity.PaymentMethod;
import com.ikobana.backend.entity.PaymentStatus;
import com.ikobana.backend.entity.Product;
import com.ikobana.backend.entity.Transaction;
import com.ikobana.backend.entity.TransactionItem;
import com.ikobana.backend.entity.User;
import com.ikobana.backend.exception.InsufficientStockException;
import com.ikobana.backend.exception.ProductNotFoundException;
import com.ikobana.backend.exception.TransactionNotFoundException;
import com.ikobana.backend.exception.UserNotFoundException;
import com.ikobana.backend.repository.ProductRepository;
import com.ikobana.backend.repository.TransactionRepository;
import com.ikobana.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service handling sales transaction processing, order lifecycle, and delivery/payment status updates.
 */
@Service
@Transactional
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Value("${transaction.id.prefix:#TRX}")
    private String transactionIdPrefix;

    @Autowired
    public TransactionService(
            TransactionRepository transactionRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            ModelMapper modelMapper
    ) {
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Creates a new transaction with line items and atomically reduces inventory stock.
     *
     * @param userId  ID of the user/admin processing the order
     * @param request order details and item list
     * @return full TransactionResponse DTO
     * @throws UserNotFoundException      if user does not exist
     * @throws ProductNotFoundException   if an ordered product does not exist
     * @throws InsufficientStockException if product stock is insufficient
     */
    public TransactionResponse createTransaction(Long userId, TransactionRequest request) {
        log.info("Creating new transaction for userId: {}, customer: {}", userId, request.getCustomerName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User dengan ID " + userId + " tidak ditemukan"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Daftar item belanja tidak boleh kosong");
        }

        // Fetch and validate all products and check stock availability
        Map<Long, Product> productMap = new HashMap<>();
        for (TransactionItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Produk dengan ID " + itemReq.getProductId() + " tidak ditemukan"));

            int currentStock = product.getStock() != null ? product.getStock() : 0;
            if (currentStock < itemReq.getQuantity()) {
                log.error("Insufficient stock for product ID {}: available={}, requested={}",
                        product.getId(), currentStock, itemReq.getQuantity());
                throw new InsufficientStockException("Stok produk '" + product.getName() + "' tidak mencukupi (Tersedia: "
                        + currentStock + ", Diminta: " + itemReq.getQuantity() + ")");
            }

            productMap.put(product.getId(), product);
        }

        // Create Transaction entity
        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setUserId(user.getId());
        transaction.setCustomerName(request.getCustomerName());
        transaction.setCustomerPhone(request.getCustomerPhone());
        transaction.setCustomerEmail(request.getCustomerEmail());
        transaction.setPaymentMethod(request.getPaymentMethod().name());
        transaction.setPaymentStatus(PaymentStatus.PENDING.name());
        transaction.setDeliveryStatus(DeliveryStatus.PENDING.name());
        transaction.setNotes(request.getNotes());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        // Process line items and reduce stock
        List<Product> productsToSave = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (TransactionItemRequest itemReq : request.getItems()) {
            Product product = productMap.get(itemReq.getProductId());

            TransactionItem item = new TransactionItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(itemReq.getQuantity());
            item.setPricePerUnit(product.getPriceRetail());

            BigDecimal subtotal = product.getPriceRetail().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            item.setSubtotal(subtotal);

            transaction.addItem(item);
            totalAmount = totalAmount.add(subtotal);

            // Deduct stock
            product.setStock(product.getStock() - itemReq.getQuantity());
            product.setUpdatedAt(LocalDateTime.now());
            productsToSave.add(product);
        }

        transaction.setTotalAmount(totalAmount);

        // Save stock adjustments and transaction entity (cascades line items)
        productRepository.saveAll(productsToSave);
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Successfully created transaction ID: {} (code: {}) with {} items, total: {}",
                savedTransaction.getId(), savedTransaction.getTransactionId(),
                savedTransaction.getItems().size(), savedTransaction.getTotalAmount());

        return convertToTransactionResponse(savedTransaction);
    }

    /**
     * Retrieves full transaction details by database ID.
     *
     * @param transactionId database identifier
     * @return TransactionResponse DTO
     * @throws TransactionNotFoundException if transaction not found
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaksi dengan ID " + transactionId + " tidak ditemukan"));
        return convertToTransactionResponse(transaction);
    }

    /**
     * Retrieves full transaction details by formatted code (e.g., #TRX-001).
     *
     * @param transactionId formatted transaction identifier
     * @return TransactionResponse DTO
     * @throws TransactionNotFoundException if transaction not found
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByTransactionId(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaksi dengan kode '" + transactionId + "' tidak ditemukan"));
        return convertToTransactionResponse(transaction);
    }

    /**
     * Retrieves paginated transactions for a specific user, ordered newest first.
     *
     * @param userId   ID of user
     * @param pageable pagination parameters
     * @return page of TransactionResponse DTOs
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToTransactionResponse);
    }

    /**
     * Retrieves all transactions (every user), for the admin panel.
     *
     * @param pageable pagination parameters (sort is applied by caller)
     * @return page of TransactionResponse DTOs
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(this::convertToTransactionResponse);
    }

    /**
     * Searches transactions by customer name or phone number with pagination.
     *
     * @param keyword  customer search term
     * @param pageable pagination parameters
     * @return page of lightweight TransactionSearchResponse DTOs
     */
    @Transactional(readOnly = true)
    public Page<TransactionSearchResponse> searchTransactions(String keyword, Pageable pageable) {
        List<Transaction> matches = transactionRepository.searchByCustomer(keyword != null ? keyword : "");
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), matches.size());

        List<TransactionSearchResponse> content = (start <= matches.size())
                ? matches.subList(start, end).stream()
                        .map(this::convertToTransactionSearchResponse)
                        .collect(Collectors.toList())
                : List.of();

        return new PageImpl<>(content, pageable, matches.size());
    }

    /**
     * Updates the payment status of a transaction.
     *
     * @param transactionId ID of transaction to update
     * @param newStatus     target PaymentStatus
     * @return updated TransactionResponse DTO
     * @throws TransactionNotFoundException if transaction not found
     */
    public TransactionResponse updatePaymentStatus(Long transactionId, PaymentStatus newStatus) {
        log.info("Updating payment status for transaction ID {} to {}", transactionId, newStatus);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaksi dengan ID " + transactionId + " tidak ditemukan"));

        String currentStatusStr = transaction.getPaymentStatus();
        PaymentStatus currentStatus = PaymentStatus.valueOf(currentStatusStr);

        // Validate state transitions
        if (currentStatus == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Transaksi yang sudah di-refund tidak dapat diubah status pembayarannya");
        }

        transaction.setPaymentStatus(newStatus.name());
        transaction.setUpdatedAt(LocalDateTime.now());
        Transaction updated = transactionRepository.save(transaction);
        log.info("Payment status for transaction ID {} updated from {} to {}", transactionId, currentStatus, newStatus);

        return convertToTransactionResponse(updated);
    }

    /**
     * Updates the delivery status of a transaction.
     *
     * @param transactionId ID of transaction to update
     * @param newStatus     target DeliveryStatus
     * @return updated TransactionResponse DTO
     * @throws TransactionNotFoundException if transaction not found
     */
    public TransactionResponse updateDeliveryStatus(Long transactionId, DeliveryStatus newStatus) {
        log.info("Updating delivery status for transaction ID {} to {}", transactionId, newStatus);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaksi dengan ID " + transactionId + " tidak ditemukan"));

        String currentStatusStr = transaction.getDeliveryStatus();
        DeliveryStatus currentStatus = DeliveryStatus.valueOf(currentStatusStr);

        if (currentStatus == DeliveryStatus.CANCELLED) {
            throw new IllegalStateException("Transaksi yang sudah dibatalkan tidak dapat diubah status pengirimannya");
        }

        transaction.setDeliveryStatus(newStatus.name());
        transaction.setUpdatedAt(LocalDateTime.now());
        Transaction updated = transactionRepository.save(transaction);
        log.info("Delivery status for transaction ID {} updated from {} to {}", transactionId, currentStatus, newStatus);

        return convertToTransactionResponse(updated);
    }

    /**
     * Retrieves transactions created within a given date range.
     *
     * @param startDate start timestamp
     * @param endDate   end timestamp
     * @return list of TransactionResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByDateRange(startDate, endDate).stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Generates a unique, formatted transaction ID (e.g., #TRX-001, #TRX-002).
     */
    private String generateTransactionId() {
        long sequence = transactionRepository.count() + 1;
        String candidate = String.format("%s-%03d", transactionIdPrefix, sequence);

        while (transactionRepository.findByTransactionId(candidate).isPresent()) {
            sequence++;
            candidate = String.format("%s-%03d", transactionIdPrefix, sequence);
        }
        return candidate;
    }

    /**
     * Calculates the total amount for a list of transaction item requests.
     */
    private BigDecimal calculateTransactionTotal(List<TransactionItemRequest> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (TransactionItemRequest item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Produk dengan ID " + item.getProductId() + " tidak ditemukan"));
            BigDecimal subtotal = product.getPriceRetail().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
        }
        return total;
    }

    /**
     * Converts a Transaction entity to a full TransactionResponse DTO including line item details.
     */
    public TransactionResponse convertToTransactionResponse(Transaction transaction) {
        TransactionResponse response = modelMapper.map(transaction, TransactionResponse.class);

        if (transaction.getPaymentMethod() != null) {
            try {
                response.setPaymentMethod(PaymentMethod.valueOf(transaction.getPaymentMethod()));
            } catch (IllegalArgumentException ignored) {}
        }

        if (transaction.getPaymentStatus() != null) {
            try {
                response.setPaymentStatus(PaymentStatus.valueOf(transaction.getPaymentStatus()));
            } catch (IllegalArgumentException ignored) {}
        }

        if (transaction.getDeliveryStatus() != null) {
            try {
                response.setDeliveryStatus(DeliveryStatus.valueOf(transaction.getDeliveryStatus()));
            } catch (IllegalArgumentException ignored) {}
        }

        if (transaction.getItems() != null) {
            List<TransactionItemResponse> itemResponses = transaction.getItems().stream()
                    .map(this::convertToTransactionItemResponse)
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
        }

        return response;
    }

    /**
     * Converts a TransactionItem entity to a TransactionItemResponse DTO.
     */
    public TransactionItemResponse convertToTransactionItemResponse(TransactionItem item) {
        return modelMapper.map(item, TransactionItemResponse.class);
    }

    /**
     * Converts a Transaction entity to a lightweight TransactionSearchResponse DTO.
     */
    public TransactionSearchResponse convertToTransactionSearchResponse(Transaction transaction) {
        TransactionSearchResponse response = modelMapper.map(transaction, TransactionSearchResponse.class);

        if (transaction.getPaymentStatus() != null) {
            try {
                response.setPaymentStatus(PaymentStatus.valueOf(transaction.getPaymentStatus()));
            } catch (IllegalArgumentException ignored) {}
        }

        if (transaction.getDeliveryStatus() != null) {
            try {
                response.setDeliveryStatus(DeliveryStatus.valueOf(transaction.getDeliveryStatus()));
            } catch (IllegalArgumentException ignored) {}
        }

        return response;
    }
}
