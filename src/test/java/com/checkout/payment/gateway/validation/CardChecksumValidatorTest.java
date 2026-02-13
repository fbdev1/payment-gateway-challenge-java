package com.checkout.payment.gateway.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CardChecksumValidatorTest {

  private CardChecksumValidator validator;

  @Mock
  private ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    validator = new CardChecksumValidator();
  }

  @Test
  void isValid_ValidCardNumbers_ReturnsTrue() {
    // Valid card numbers that pass Luhn check
    assertTrue(validator.isValid("4532015112830366", context)); // Visa
    assertTrue(validator.isValid("5555555555554444", context)); // Mastercard
    assertTrue(validator.isValid("378282246310005", context));  // American Express
    assertTrue(validator.isValid("6011111111111117", context)); // Discover
    assertTrue(validator.isValid("30569309025904", context));  // Diner's Club
    assertTrue(validator.isValid("3530111333300000", context)); // JCB
  }

  @Test
  void isValid_InvalidCardNumbers_ReturnsFalse() {
    // Invalid card numbers that fail Luhn check
    assertFalse(validator.isValid("4532015112830367", context)); // Visa - last digit changed
    assertFalse(validator.isValid("5555555555554445", context)); // Mastercard - last digit changed
    assertFalse(
        validator.isValid("378282246310006", context));  // American Express - last digit changed
    assertFalse(validator.isValid("1234567890123456", context)); // Random invalid number
    assertFalse(validator.isValid("1111111111111111", context)); // All same digits
  }

  @Test
  void isValid_NullCardNumber_ReturnsFalse() {
    assertFalse(validator.isValid(null, context));
  }

  @Test
  void isValid_EmptyCardNumber_ReturnsFalse() {
    assertFalse(validator.isValid("", context));
  }

  @Test
  void isValid_NonNumericCardNumber_ReturnsFalse() {
    assertFalse(validator.isValid("4532-0151-1283-0366", context)); // With dashes
    assertFalse(validator.isValid("4532 0151 1283 0366", context));  // With spaces
    assertFalse(validator.isValid("453201511283036a", context));   // With letter
    assertFalse(validator.isValid("abcdefghijk", context));        // All letters
    assertFalse(validator.isValid("4532-0151-1283-0366!", context)); // With special chars
  }

  @Test
  void isValid_ShortCardNumbers_ReturnsCorrectValidation() {
    // Valid short numbers
    assertTrue(validator.isValid("30569309025904", context));  // 14 digits - valid

    // Invalid short numbers
    assertFalse(validator.isValid("123456789012", context));   // 12 digits - fails Luhn
    assertFalse(validator.isValid("123", context));            // 3 digits - fails Luhn
  }

  @Test
  void isValid_LongCardNumbers_ReturnsCorrectValidation() {
    // 19 digit card numbers (max allowed by validation)
    assertFalse(validator.isValid("6011000990139424464", context)); // InValid 19-digit
    assertTrue(validator.isValid("6011000990139424462", context)); // Valid 19-digit
  }

  @Test
  void isValid_LeadingZeros_HandlesCorrectly() {
    assertTrue(validator.isValid("0000000000000000", context)); // All zeros - passes Luhn
    assertFalse(
        validator.isValid("0000000000000001", context)); // Leading zeros with invalid checksum
  }

  @Test
  void passesLuhnCheck_KnownValidNumbers_ReturnsTrue() {
    // These are known valid test card numbers from payment processor documentation
    assertTrue(validator.isValid("4111111111111111", context)); // Visa test number
    assertTrue(validator.isValid("4000056655665556", context)); // Visa test number
    assertTrue(validator.isValid("4000000000000077", context)); // Visa test number
    assertTrue(validator.isValid("5555555555554444", context)); // Mastercard test number
    assertTrue(validator.isValid("2223000048400011", context)); // Mastercard test number
    assertTrue(validator.isValid("378282246310005", context));  // Amex test number
    assertTrue(validator.isValid("371449635398431", context));  // Amex test number
    assertTrue(validator.isValid("6011111111111117", context)); // Discover test number
  }

  @Test
  void passesLuhnCheck_KnownInvalidNumbers_ReturnsFalse() {
    // These are variations of valid numbers with changed last digit
    assertFalse(validator.isValid("4111111111111112", context)); // Visa test number +1
    assertFalse(validator.isValid("4000056655665557", context)); // Visa test number +1
    assertFalse(validator.isValid("5555555555554445", context)); // Mastercard test number +1
    assertFalse(validator.isValid("378282246310006", context)); // Amex test number +1
    assertFalse(validator.isValid("6011111111111118", context)); // Discover test number +1
  }
}
