package com.ikobana.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for user login request.
 * Contains credentials required to authenticate a user.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /** User's registered email address. */
    @NotBlank(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    private String email;

    /** User's plaintext password (will be matched against stored hash). */
    @NotBlank(message = "Password tidak boleh kosong")
    @Length(min = 6, max = 255, message = "Password harus antara 6 dan 255 karakter")
    private String password;
}
