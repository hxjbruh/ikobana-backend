package com.ikobana.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ikobana.backend.entity.DeliveryStatus;
import com.ikobana.backend.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lightweight DTO for transaction list, search, and admin dashboard views.
 * Excludes detailed items and customer email to optimize payload size.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSearchResponse {

    /** Internal database identifier. */
    private Long id;

    /** Human-readable transaction identifier, e.g. #TRX-001. */
    private String transactionId;

    /** Customer full name. */
    private String customerName;

    /** Customer phone number. */
    private String customerPhone;

    /** Total transaction amount. */
    private BigDecimal totalAmount;

    /** Current payment status. */
    private PaymentStatus paymentStatus;

    /** Current delivery status. */
    private DeliveryStatus deliveryStatus;

    /** Timestamp when the transaction was created. */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
