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
class CurrencyCodeValidatorTest {

  private CurrencyCodeValidator validator;

  @Mock
  private ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    validator = new CurrencyCodeValidator();
  }

  @Test
  void isValid_SupportedCurrencies_ReturnsTrue() {
    // Test supported currencies in different cases
    assertTrue(validator.isValid("GBP", context));
    assertTrue(validator.isValid("gbp", context));
    assertTrue(validator.isValid("Gbp", context));
    assertTrue(validator.isValid("USD", context));
    assertTrue(validator.isValid("usd", context));
    assertTrue(validator.isValid("Usd", context));
    assertTrue(validator.isValid("EUR", context));
    assertTrue(validator.isValid("eur", context));
    assertTrue(validator.isValid("Eur", context));
  }

  @Test
  void isValid_UnsupportedButValidCurrencies_ReturnsFalse() {
    // These are valid ISO 4217 currency codes but not supported
    assertFalse(validator.isValid("JPY", context)); // Japanese Yen
    assertFalse(validator.isValid("CAD", context)); // Canadian Dollar
    assertFalse(validator.isValid("AUD", context)); // Australian Dollar
    assertFalse(validator.isValid("CHF", context)); // Swiss Franc
    assertFalse(validator.isValid("CNY", context)); // Chinese Yuan
    assertFalse(validator.isValid("INR", context)); // Indian Rupee
    assertFalse(validator.isValid("BRL", context)); // Brazilian Real
    assertFalse(validator.isValid("RUB", context)); // Russian Ruble
    assertFalse(validator.isValid("ZAR", context)); // South African Rand
    assertFalse(validator.isValid("MXN", context)); // Mexican Peso
  }

  @Test
  void isValid_InvalidCurrencyCodes_ReturnsFalse() {
    // Invalid currency codes
    assertFalse(validator.isValid("AAA", context)); // Valid format but not real currency
    assertFalse(validator.isValid("XYZ", context)); // Valid format but not real currency
    assertFalse(validator.isValid("QQQ", context)); // Valid format but not real currency
    assertFalse(validator.isValid("GB", context));  // Too short
    assertFalse(validator.isValid("GBPP", context)); // Too long
    assertFalse(validator.isValid("G1P", context));  // Contains number
    assertFalse(validator.isValid("G@P", context));  // Contains special character
    assertFalse(validator.isValid("G P", context));  // Contains space
  }

  @Test
  void isValid_NullAndEmptyInputs_ReturnsFalse() {
    assertFalse(validator.isValid(null, context));
    assertFalse(validator.isValid("", context));
    assertFalse(validator.isValid(" ", context));
  }

  @Test
  void isValid_EdgeCaseInputs_ReturnsFalse() {
    // Test various edge cases
    assertFalse(validator.isValid("gb", context));    // 2 characters
    assertFalse(validator.isValid("gbp ", context));  // Trailing space
    assertFalse(validator.isValid(" gbp", context));  // Leading space
    assertFalse(validator.isValid("gb p", context));  // Middle space
    assertFalse(validator.isValid("GBP\n", context)); // With newline
    assertFalse(validator.isValid("GBP\t", context)); // With tab
  }

  @Test
  void isValid_CaseInsensitiveValidation_WorksCorrectly() {
    // Test various case combinations
    assertTrue(validator.isValid("GBP", context));  // All uppercase
    assertTrue(validator.isValid("gbp", context));  // All lowercase
    assertTrue(validator.isValid("GbP", context));  // Mixed case
    assertTrue(validator.isValid("gBp", context));  // Mixed case
    assertTrue(validator.isValid("GBP", context));  // All uppercase
    assertTrue(validator.isValid("usd", context));  // All lowercase
    assertTrue(validator.isValid("UsD", context));  // Mixed case
    assertTrue(validator.isValid("EUR", context));  // All uppercase
    assertTrue(validator.isValid("eur", context));  // All lowercase
  }

  @Test
  void isValid_NonStandardCurrencyCodes_ReturnsFalse() {
    // Test with non-standard but potentially valid currency codes
    assertFalse(validator.isValid("BTC", context)); // Bitcoin (not in standard Currency)
    assertFalse(validator.isValid("ETH", context)); // Ethereum (not in standard Currency)
    assertFalse(validator.isValid("LTC", context)); // Litecoin (not in standard Currency)
  }

  @Test
  void isValid_NumericAndSpecialCharacterInputs_ReturnsFalse() {
    // Test with numbers and special characters
    assertFalse(validator.isValid("123", context));
    assertFalse(validator.isValid("GB1", context));
    assertFalse(validator.isValid("1GB", context));
    assertFalse(validator.isValid("G!P", context));
    assertFalse(validator.isValid("G#P", context));
    assertFalse(validator.isValid("G$P", context));
    assertFalse(validator.isValid("G%P", context));
  }

  @Test
  void isValid_WhitespaceVariations_ReturnsFalse() {
    // Test with various whitespace combinations
    assertFalse(validator.isValid(" GBP", context));  // Leading space
    assertFalse(validator.isValid("GBP ", context));  // Trailing space
    assertFalse(validator.isValid(" GB P", context)); // Multiple spaces
    assertFalse(validator.isValid("\tGBP", context)); // Leading tab
    assertFalse(validator.isValid("GBP\t", context)); // Trailing tab
    assertFalse(validator.isValid("\nGBP", context)); // Leading newline
    assertFalse(validator.isValid("GBP\n", context)); // Trailing newline
  }
}
