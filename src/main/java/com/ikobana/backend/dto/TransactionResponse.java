package com.ikobana.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ikobana.backend.entity.DeliveryStatus;
import com.ikobana.backend.entity.PaymentMethod;
import com.ikobana.backend.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for full transaction detail response.
 * Used in: GET /transactions/{id}, POST /transactions
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    /** Internal database identifier. */
    private Long id;

    /** Human-readable transaction identifier, e.g. #TRX-001. */
    private String transactionId;

    /** ID of the user/admin who created or processed the transaction. */
    private Long userId;

    /** Customer full name. */
    private String customerName;

    /** Customer phone number. */
    private String customerPhone;

    /** Customer email address. */
    private String customerEmail;

    /** List of line items included in this transaction. */
    private List<TransactionItemResponse> items;

    /** Total amount of the transaction. */
    private BigDecimal totalAmount;

    /** Payment method used. */
    private PaymentMethod paymentMethod;

    /** Current payment status. */
    private PaymentStatus paymentStatus;

    /** Current delivery status. */
    private DeliveryStatus deliveryStatus;

    /** Optional notes or delivery instructions. */
    private String notes;

    /** Timestamp when the transaction was created. */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /** Timestamp when the transaction was last updated. */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
