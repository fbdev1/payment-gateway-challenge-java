package com.checkout.payment.gateway.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Year;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PostPaymentRequestTest {

    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void validatePostPaymentRequest_AllValidFields_PassesValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();

        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void validateCardNumber_NullCardNumber_FailsValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setCardNumber(null);

        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertEquals(2, violations.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123", "12345678901234567890"})
    void validateCardNumber_InvalidLength_FailsValidation(String cardNumber) {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setCardNumber(cardNumber);

        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getMessage().contains("between 14 and 19 characters")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678901234", "123456789012345", "1234567890123456", "12345678901234567", "123456789012345678"})
    void validateCardNumber_NonNumeric_FailsValidation(String cardNumber) {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setCardNumber(cardNumber.replace("1", "a"));
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getMessage().contains("numeric characters")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"4532015112830366", "6011111111111117", "371449635398431", "5555555555554444"})
    void validateCardNumber_ValidLuhnCheck_PassesValidation(String cardNumber) {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setCardNumber(cardNumber);
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().noneMatch(v -> 
            v.getMessage().contains("failed Luhn check")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"4532015112830367", "6011111111111118", "371449635398432", "5555555555554445"})
    void validateCardNumber_InvalidLuhnCheck_FailsValidation(String cardNumber) {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setCardNumber(cardNumber);

        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getMessage().contains("failed Luhn check")));
    }

    @Test
    void validateExpiryMonth_ValidMonth_PassesValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setExpiryMonth(6);

        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("expiryMonth")));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 13, -1, 100})
    void validateExpiryMonth_InvalidMonth_FailsValidation(int expiryMonth) {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setExpiryMonth(expiryMonth);

        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("expiryMonth") &&
            v.getMessage().contains("range 1-12")));
    }

    @Test
    void validateExpiryYear_CurrentYear_PassesValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setExpiryYear(Year.now().getValue());

        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("expiryYear")));
    }

    @Test
    void validateExpiryYear_FutureYear_PassesValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setExpiryYear(Year.now().getValue() + 1);
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("expiryYear")));
    }

    @Test
    void validateExpiryYear_PastYear_FailsValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setExpiryYear(Year.now().getValue() - 1);
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("expiryYear")));
    }

    @Test
    void validateCurrency_ValidCurrency_PassesValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setCurrency("USD");

        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("currency")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GBP", "USD", "EUR", "usd", "eur"})
    void validateCurrency_SupportedCurrencies_PassesValidation(String currency) {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setCurrency(currency);
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
        
        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("currency")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"JPY", "CAD", "AUD", "CNY"})
    void validateCurrency_UnsupportedCurrencies_FailsValidation(String currency) {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setCurrency(currency);
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("currency") &&
            v.getMessage().contains("not supported")));
    }

    @Test
    void validateCurrency_NullCurrency_FailsValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setCurrency(null);
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("currency") &&
            v.getMessage().contains("must not be empty")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"US", "USDA", "GBPUSD"})
    void validateCurrency_InvalidLengthOrCase_FailsValidation(String currency) {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setCurrency(currency);

        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void validateAmount_ValidAmount_PassesValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setAmount(1000);
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
        
        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void validateAmount_ZeroAmount_FailsValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setAmount(0);
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("amount") &&
            v.getMessage().contains("more than 0")));
    }

    @Test
    void validateAmount_NegativeAmount_FailsValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setAmount(-100);
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void validateAmount_MaxAmount_PassesValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setAmount(Integer.MAX_VALUE);

        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
        
        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void validateCvv_ValidCvv_PassesValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setCvv("123");

        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("cvv")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "4567", "7890"})
    void validateCvv_ValidLengthCvv_PassesValidation(String cvv) {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setCvv(cvv);
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("cvv")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12", "12345", "abc", "12a", "a12"})
    void validateCvv_InvalidCvv_FailsValidation(String cvv) {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setCvv(cvv);
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("cvv")));
    }

    @Test
    void validateCvv_NullCvv_FailsValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setCvv(null);
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("cvv") &&
            v.getMessage().contains("must not be empty")));
    }

    @Test
    void validateExpiryDate_CurrentMonthYear_PassesValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setExpiryMonth(java.time.LocalDate.now().getMonthValue());
        request.setExpiryYear(java.time.LocalDate.now().getYear());
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().noneMatch(v -> 
            v.getMessage().contains("Expiry date is not valid")));
    }

    @Test
    void validateExpiryDate_FutureMonthYear_PassesValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setExpiryMonth(java.time.LocalDate.now().plusMonths(1).getMonthValue());
        request.setExpiryYear(java.time.LocalDate.now().plusMonths(1).getYear());
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
        
        assertTrue(violations.stream().noneMatch(v -> 
            v.getMessage().contains("Expiry date is not valid")));
    }

    @Test
    void validateExpiryDate_PastMonthYear_FailsValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setExpiryMonth(java.time.LocalDate.now().minusMonths(1).getMonthValue());
        request.setExpiryYear(java.time.LocalDate.now().minusMonths(1).getYear());
        
        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getMessage().contains("Expiry date is not valid")));
    }

    @Test
    void validateExpiryDate_PastMonthCurrentYear_FailsValidation() {
        PostPaymentRequest request = createValidPostPaymentRequest();
        request.setExpiryMonth(java.time.LocalDate.now().minusMonths(1).getMonthValue());
        request.setExpiryYear(java.time.LocalDate.now().getYear());

        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getMessage().contains("Expiry date is not valid")));
    }

    @Test
    void validateMultipleFields_MultipleErrors_FailsValidation() {
        PostPaymentRequest request = new PostPaymentRequest();
        request.setCardNumber("invalid");
        request.setExpiryMonth(0);
        request.setExpiryYear(2020);
        request.setCurrency("XXX");
        request.setAmount(-1);
        request.setCvv("ab");

        Set<ConstraintViolation<PostPaymentRequest>> violations = validator.validate(request);

        assertTrue(violations.size() > 1, "Should have multiple violations");
    }

    private PostPaymentRequest createValidPostPaymentRequest() {
        PostPaymentRequest request = new PostPaymentRequest();
        request.setCardNumber("4532015112830366"); // Valid Luhn check
        request.setExpiryMonth(12);
        request.setExpiryYear(Year.now().getValue() + 1);
        request.setCurrency("USD");
        request.setAmount(1000);
        request.setCvv("123");
        return request;
    }
}
