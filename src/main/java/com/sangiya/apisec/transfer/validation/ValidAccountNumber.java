package com.sangiya.apisec.transfer.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validates an account reference number in the format
 * XXX-NNNNN (3 letters, dash, 5 digits), e.g. USR-10001.
 */
@Documented
@Constraint(validatedBy = AccountNumberValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAccountNumber {

    String message() default "recipient account number must match format XXX-#####";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
