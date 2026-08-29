package com.sangiya.apisec.transfer.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AccountNumberValidator implements ConstraintValidator<ValidAccountNumber, String> {

    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[A-Z]{3}-\\d{5}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return ACCOUNT_PATTERN.matcher(value).matches();
    }
}
