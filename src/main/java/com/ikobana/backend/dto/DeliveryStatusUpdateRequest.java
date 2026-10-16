package com.ikobana.backend.dto;

import com.ikobana.backend.entity.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for updating the delivery status of a transaction.
 * Used as the request body for PATCH /transactions/{id}/delivery-status.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryStatusUpdateRequest {

    /** New delivery status. */
    @NotNull(message = "Status pengiriman tidak boleh kosong")
    private DeliveryStatus deliveryStatus;

    /** Optional notes, tracking code, or reason for status update. */
    @Length(max = 200, message = "Catatan maksimal 200 karakter")
    private String notes;
}
