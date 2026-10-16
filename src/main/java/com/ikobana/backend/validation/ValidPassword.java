package com.ikobana.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level constraint annotation that validates {@code password} and
 * {@code confirmPassword} fields in {@link com.ikobana.backend.dto.RegisterRequest}.
 *
 * <p>Usage:
 * <pre>
 *   {@literal @}ValidPassword
 *   public class RegisterRequest { ... }
 * </pre>
 *
 * <p>Validation is performed by {@link PasswordValidator}.
 */
@Constraint(validatedBy = PasswordValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    /** Error message returned when passwords do not match. */
    String message() default "Password dan Confirm Password tidak sama";

    /** Constraint groups. */
    Class<?>[] groups() default {};

    /** Constraint payload. */
    Class<? extends Payload>[] payload() default {};
}
