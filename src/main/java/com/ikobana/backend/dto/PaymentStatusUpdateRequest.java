package com.ikobana.backend.dto;

import com.ikobana.backend.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for updating the payment status of a transaction by admin/staff.
 * Used as the request body for PATCH /transactions/{id}/payment-status.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusUpdateRequest {

    /** New payment status. */
    @NotNull(message = "Status pembayaran tidak boleh kosong")
    private PaymentStatus paymentStatus;

    /** Optional notes or reason for status update. */
    @Length(max = 200, message = "Catatan maksimal 200 karakter")
    private String notes;
}
