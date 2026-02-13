package com.checkout.payment.gateway.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CurrentOrFutureYearValidatorTest {

  private CurrentOrFutureYearValidator validator;

  @Mock
  private ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    validator = new CurrentOrFutureYearValidator();
  }

  @Test
  void isValid_CurrentYear_ReturnsTrue() {
    int currentYear = Year.now().getValue();
    assertTrue(validator.isValid(currentYear, context));
  }

  @Test
  void isValid_FutureYear_ReturnsTrue() {
    int currentYear = Year.now().getValue();
    assertTrue(validator.isValid(currentYear + 1, context));
    assertTrue(validator.isValid(currentYear + 5, context));
    assertTrue(validator.isValid(currentYear + 10, context));
  }

  @Test
  void isValid_PastYear_ReturnsFalse() {
    int currentYear = Year.now().getValue();
    assertFalse(validator.isValid(currentYear - 1, context));
    assertFalse(validator.isValid(currentYear - 5, context));
    assertFalse(validator.isValid(currentYear - 10, context));
  }

  @Test
  void isValid_NullYear_ReturnsFalse() {
    assertFalse(validator.isValid(null, context));
  }

  @Test
  void isValid_EdgeCaseYears_ReturnsCorrectValidation() {
    int currentYear = Year.now().getValue();
    
    // Test boundary conditions
    assertTrue(validator.isValid(currentYear, context)); // Current year
    assertTrue(validator.isValid(currentYear + 1, context)); // Next year
    assertFalse(validator.isValid(currentYear - 1, context)); // Previous year
  }

  @Test
  void isValid_ExtremeFutureYears_ReturnsTrue() {
    int currentYear = Year.now().getValue();
    
    // Test far future years (should still be valid)
    assertTrue(validator.isValid(currentYear + 50, context));
    assertTrue(validator.isValid(currentYear + 100, context));
    assertTrue(validator.isValid(9999, context)); // Maximum reasonable year
  }

  @Test
  void isValid_ExtremePastYears_ReturnsFalse() {
    int currentYear = Year.now().getValue();
    
    // Test far past years
    assertFalse(validator.isValid(currentYear - 50, context));
    assertFalse(validator.isValid(currentYear - 100, context));
    assertFalse(validator.isValid(1900, context)); // Historical year
    assertFalse(validator.isValid(0, context)); // Year 0
  }

  @Test
  void isValid_NegativeYears_ReturnsFalse() {
    // Test negative years (BC years)
    assertFalse(validator.isValid(-1, context));
    assertFalse(validator.isValid(-100, context));
    assertFalse(validator.isValid(-999, context));
  }

  @Test
  void isValid_Year2100_ReturnsTrue() {
    // Year 2100 should always be valid (future year)
    assertTrue(validator.isValid(2100, context));
  }

  @Test
  void isValid_Year1999_ReturnsFalse() {
    int currentYear = Year.now().getValue();
    
    // Year 1999 should be invalid unless we're somehow in the past (unlikely)
    if (currentYear > 1999) {
      assertFalse(validator.isValid(1999, context));
    }
  }

  @Test
  void isValid_CommonExpiryYears_ReturnsCorrectValidation() {
    int currentYear = Year.now().getValue();
    
    // Test common credit card expiry year scenarios
    assertTrue(validator.isValid(currentYear, context)); // This year
    assertTrue(validator.isValid(currentYear + 1, context)); // Next year
    assertTrue(validator.isValid(currentYear + 2, context)); // Two years ahead
    assertTrue(validator.isValid(currentYear + 3, context)); // Three years ahead
    assertTrue(validator.isValid(currentYear + 4, context)); // Four years ahead
    assertTrue(validator.isValid(currentYear + 5, context)); // Five years ahead
    
    // Past years should be invalid
    assertFalse(validator.isValid(currentYear - 1, context)); // Last year
    assertFalse(validator.isValid(currentYear - 2, context)); // Two years ago
  }

  @Test
  void isValid_MinimumAndMaximumIntegers_ReturnsCorrectValidation() {
    // Test extreme integer values
    assertFalse(validator.isValid(Integer.MIN_VALUE, context));
    assertTrue(validator.isValid(Integer.MAX_VALUE, context)); // Far future year
  }
}
