package com.ikobana.backend.dto;

import com.ikobana.backend.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for user registration request.
 *
 * <p>Uses {@link ValidPassword} class-level annotation to validate that
 * {@code password} and {@code confirmPassword} match before persisting.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidPassword
public class RegisterRequest {

    /** User's email address — must be unique in the system. */
    @NotBlank(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    private String email;

    /** Desired username — must be unique, 3–50 characters. */
    @NotBlank(message = "Username tidak boleh kosong")
    @Length(min = 3, max = 50, message = "Username harus antara 3 dan 50 karakter")
    private String username;

    /** Plaintext password — will be hashed before storage. */
    @NotBlank(message = "Password tidak boleh kosong")
    @Length(min = 8, max = 255, message = "Password harus antara 8 dan 255 karakter")
    private String password;

    /** Must match {@code password} exactly (validated by {@link ValidPassword}). */
    @NotBlank(message = "Konfirmasi password tidak boleh kosong")
    private String confirmPassword;

    /** User's full name. */
    @NotBlank(message = "Nama lengkap tidak boleh kosong")
    @Length(max = 100, message = "Nama lengkap maksimal 100 karakter")
    private String fullName;

    /** User's phone number. */
    @NotBlank(message = "Nomor telepon tidak boleh kosong")
    @Length(max = 20, message = "Nomor telepon maksimal 20 karakter")
    private String phone;

    // Tidak ada field role: registrasi publik selalu CUSTOMER (diatur di AuthService).
}
