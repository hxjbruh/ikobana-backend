package com.ikobana.backend.dto;

import com.ikobana.backend.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * DTO for creating a new transaction / order.
 * Used as the request body for POST /transactions.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    /** Full name of the customer. */
    @NotBlank(message = "Nama customer tidak boleh kosong")
    @Length(min = 3, max = 100, message = "Nama customer harus antara 3 dan 100 karakter")
    private String customerName;

    /** Contact phone number of the customer. */
    @NotBlank(message = "Nomor telepon customer tidak boleh kosong")
    @Length(min = 10, max = 20, message = "Nomor telepon customer harus antara 10 dan 20 karakter")
    private String customerPhone;

    /** Optional email address of the customer. */
    @Email(message = "Format email tidak valid")
    private String customerEmail;

    /** List of line items to order. */
    @NotNull(message = "Daftar item tidak boleh kosong")
    @NotEmpty(message = "Daftar item minimal harus memiliki 1 produk")
    @Valid
    private List<TransactionItemRequest> items;

    /** Selected payment method. */
    @NotNull(message = "Metode pembayaran tidak boleh kosong")
    private PaymentMethod paymentMethod;

    /** Optional notes or delivery instructions. */
    @Length(max = 500, message = "Catatan maksimal 500 karakter")
    private String notes;
}
