package com.ikobana.backend.validation;

import com.ikobana.backend.dto.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for the {@link ValidPassword} annotation.
 *
 * <p>Checks that the {@code password} and {@code confirmPassword} fields
 * in a {@link RegisterRequest} are non-null and equal to each other.
 *
 * <p>When validation fails, the default constraint message is suppressed and
 * a field-level error is added to the {@code confirmPassword} property node,
 * so the client receives a meaningful, targeted error message.
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, RegisterRequest> {

    /**
     * Initializes the validator. No setup needed for this implementation.
     *
     * @param constraintAnnotation the annotation instance
     */
    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // No initialization required
    }

    /**
     * Validates that {@code password} equals {@code confirmPassword}.
     *
     * @param value   the {@link RegisterRequest} object being validated
     * @param context the constraint validator context
     * @return {@code true} if passwords match; {@code false} otherwise
     */
    @Override
    public boolean isValid(RegisterRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Null object is handled by @NotNull on the field level
        }

        String password        = value.getPassword();
        String confirmPassword = value.getConfirmPassword();

        // Both null → considered matching (other validators handle @NotBlank)
        if (password == null && confirmPassword == null) {
            return true;
        }

        boolean matches = password != null && password.equals(confirmPassword);

        if (!matches) {
            // Disable the default class-level error message
            context.disableDefaultConstraintViolation();

            // Add a property-node error pointing to the confirmPassword field
            context.buildConstraintViolationWithTemplate(
                    "Password dan Confirm Password tidak sama"
            )
            .addPropertyNode("confirmPassword")
            .addConstraintViolation();
        }

        return matches;
    }
}
