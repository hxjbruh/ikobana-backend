package com.ikobana.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Generic API response wrapper for all REST endpoints in the Ikobana system.
 *
 * <p>Usage examples:
 * <pre>
 *   // Success with data
 *   return ResponseEntity.ok(ApiResponse.success("Produk berhasil diambil", productList));
 *
 *   // Success without data
 *   return ResponseEntity.ok(ApiResponse.success("Produk berhasil dihapus"));
 *
 *   // Error
 *   return ResponseEntity.badRequest().body(ApiResponse.error("Produk tidak ditemukan"));
 * </pre>
 *
 * @param <T> the type of the response data payload
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** Indicates whether the request was processed successfully. */
    private Boolean success;

    /** Human-readable message describing the result. */
    private String message;

    /**
     * The response payload.
     * Null on error responses.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    /** Timestamp of when this response was generated. */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Map of field-level validation errors.
     * Key = field name, Value = error message.
     * Null when there are no validation errors.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> errors;

    // ── Static Factory Methods ─────────────────────────────────────────────

    /**
     * Creates a successful response with data payload.
     *
     * @param message success message
     * @param data    response payload
     * @param <T>     data type
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now(), null);
    }

    /**
     * Creates a successful response without data payload.
     *
     * @param message success message
     * @param <T>     data type
     * @return ApiResponse with success=true and null data
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now(), null);
    }

    /**
     * Creates an error response without data or field errors.
     *
     * @param message error message
     * @param <T>     data type
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now(), null);
    }

    /**
     * Creates an error response with field-level validation errors.
     *
     * @param message error message
     * @param errors  map of field name → error description
     * @param <T>     data type
     * @return ApiResponse with success=false and errors map populated
     */
    public static <T> ApiResponse<T> error(String message, Map<String, String> errors) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now(), errors);
    }
}
