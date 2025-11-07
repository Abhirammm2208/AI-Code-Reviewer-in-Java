package com.yourorg.aicode.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    
    // At least one uppercase, one lowercase, one digit, one special character
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    
    @Override
    public void initialize(ValidPassword constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        context.disableDefaultConstraintViolation();
        
        if (password.length() < MIN_LENGTH) {
            context.buildConstraintViolationWithTemplate(
                "Password must be at least " + MIN_LENGTH + " characters long"
            ).addConstraintViolation();
            return false;
        }
        
        if (password.length() > MAX_LENGTH) {
            context.buildConstraintViolationWithTemplate(
                "Password must not exceed " + MAX_LENGTH + " characters"
            ).addConstraintViolation();
            return false;
        }
        
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one uppercase letter"
            ).addConstraintViolation();
            return false;
        }
        
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one lowercase letter"
            ).addConstraintViolation();
            return false;
        }
        
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one digit"
            ).addConstraintViolation();
            return false;
        }
        
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one special character (!@#$%^&*()_+-=[]{};"
                    + "':\"\\|,.<>/?)"
            ).addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
