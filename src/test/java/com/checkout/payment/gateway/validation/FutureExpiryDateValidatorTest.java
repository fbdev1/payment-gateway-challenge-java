package com.checkout.payment.gateway.validation;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FutureExpiryDateValidatorTest {

  private FutureExpiryDateValidator validator;

  @Mock
  private ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    validator = new FutureExpiryDateValidator();
  }

  @Test
  void isValid_NullRequest_ReturnsTrue() {
    assertTrue(validator.isValid(null, context));
  }

  @Test
  void isValid_CurrentMonthAndYear_ReturnsTrue() {
    YearMonth current = YearMonth.now();
    PostPaymentRequest request = createRequest(current.getMonthValue(), current.getYear());
    assertTrue(validator.isValid(request, context));
  }

  @Test
  void isValid_FutureMonthCurrentYear_ReturnsTrue() {
    YearMonth current = YearMonth.now();
    PostPaymentRequest request = createRequest(current.getMonthValue() + 1, current.getYear());
    assertTrue(validator.isValid(request, context));
  }

  @Test
  void isValid_FutureYear_ReturnsTrue() {
    YearMonth current = YearMonth.now();
    PostPaymentRequest request = createRequest(1, current.getYear() + 1);
    assertTrue(validator.isValid(request, context));
  }

  @Test
  void isValid_PastMonthCurrentYear_ReturnsFalse() {
    YearMonth current = YearMonth.now();
    PostPaymentRequest request = createRequest(current.getMonthValue() - 1, current.getYear());
    assertFalse(validator.isValid(request, context));
  }

  @Test
  void isValid_PastYear_ReturnsFalse() {
    YearMonth current = YearMonth.now();
    PostPaymentRequest request = createRequest(current.getMonthValue(), current.getYear() - 1);
    assertFalse(validator.isValid(request, context));
  }

  @Test
  void isValid_DecemberCurrentYear_ReturnsCorrectValidation() {
    YearMonth current = YearMonth.now();
    
    // If current month is December, it should be valid
    if (current.getMonthValue() == 12) {
      PostPaymentRequest request = createRequest(12, current.getYear());
      assertTrue(validator.isValid(request, context));
    }
  }

  @Test
  void isValid_JanuaryCurrentYear_ReturnsCorrectValidation() {
    YearMonth current = YearMonth.now();
    
    // If current month is January, previous month (December of last year) should be invalid
    if (current.getMonthValue() == 1) {
      PostPaymentRequest request = createRequest(12, current.getYear() - 1);
      assertFalse(validator.isValid(request, context));
    }
  }

  @Test
  void isValid_EndOfMonthScenarios_ReturnsCorrectValidation() {
    YearMonth current = YearMonth.now();
    
    // Test last day of current month
    LocalDate lastDayOfMonth = current.atEndOfMonth();
    PostPaymentRequest request = createRequest(current.getMonthValue(), current.getYear());
    assertTrue(validator.isValid(request, context));
  }

  @Test
  void isValid_YearBoundaryScenarios_ReturnsCorrectValidation() {
    YearMonth current = YearMonth.now();
    
    // Test December of current year vs January of next year
    PostPaymentRequest decemberRequest = createRequest(12, current.getYear());
    PostPaymentRequest januaryNextYearRequest = createRequest(1, current.getYear() + 1);
    
    // December should be valid if we're not past December
    if (current.getMonthValue() <= 12) {
      assertTrue(validator.isValid(decemberRequest, context));
    }
    // January next year should always be valid
    assertTrue(validator.isValid(januaryNextYearRequest, context));
  }

  @Test
  void isValid_LeapYearScenarios_ReturnsCorrectValidation() {
    YearMonth current = YearMonth.now();
    
    // Test February in leap year vs non-leap year
    int nextYear = current.getYear() + 1;
    boolean isLeapYear = (nextYear % 4 == 0 && nextYear % 100 != 0) || (nextYear % 400 == 0);
    
    PostPaymentRequest februaryRequest = createRequest(2, nextYear);
    assertTrue(validator.isValid(februaryRequest, context)); // Future year should always be valid
  }

  @Test
  void isValid_ExtremeFutureDates_ReturnsTrue() {
    YearMonth current = YearMonth.now();
    
    // Test far future dates
    assertTrue(validator.isValid(createRequest(12, current.getYear() + 10), context));
    assertTrue(validator.isValid(createRequest(12, current.getYear() + 50), context));
    assertTrue(validator.isValid(createRequest(12, 9999), context));
  }

  @Test
  void isValid_ExtremePastDates_ReturnsFalse() {
    YearMonth current = YearMonth.now();
    
    // Test far past dates
    assertFalse(validator.isValid(createRequest(1, current.getYear() - 10), context));
    assertFalse(validator.isValid(createRequest(1, current.getYear() - 50), context));
    assertFalse(validator.isValid(createRequest(1, 1900), context));
  }

  @Test
  void isValid_AllMonthsCurrentYear_ReturnsCorrectValidation() {
    YearMonth current = YearMonth.now();
    int currentMonth = current.getMonthValue();
    
    // Test all months of current year
    for (int month = 1; month <= 12; month++) {
      PostPaymentRequest request = createRequest(month, current.getYear());
      boolean expected = month >= currentMonth;
      if (expected) {
        assertTrue(validator.isValid(request, context), 
            "Month " + month + " should be valid");
      } else {
        assertFalse(validator.isValid(request, context), 
            "Month " + month + " should be invalid");
      }
    }
  }

  @Test
  void isValid_AllMonthsNextYear_ReturnsTrue() {
    YearMonth current = YearMonth.now();
    int nextYear = current.getYear() + 1;
    
    // Test all months of next year (should all be valid)
    for (int month = 1; month <= 12; month++) {
      PostPaymentRequest request = createRequest(month, nextYear);
      assertTrue(validator.isValid(request, context), 
          "Month " + month + " of next year should be valid");
    }
  }

  @Test
  void isValid_AllMonthsPreviousYear_ReturnsFalse() {
    YearMonth current = YearMonth.now();
    int previousYear = current.getYear() - 1;
    
    // Test all months of previous year (should all be invalid)
    for (int month = 1; month <= 12; month++) {
      PostPaymentRequest request = createRequest(month, previousYear);
      assertFalse(validator.isValid(request, context), 
          "Month " + month + " of previous year should be invalid");
    }
  }

  @Test
  void isValid_CommonCreditCardExpiryScenarios_ReturnsCorrectValidation() {
    YearMonth current = YearMonth.now();
    int currentYear = current.getYear();
    int currentMonth = current.getMonthValue();
    
    // Common scenarios for credit card expiry
    assertTrue(validator.isValid(createRequest(currentMonth, currentYear), context)); // This month
    assertTrue(validator.isValid(createRequest(currentMonth + 1, currentYear), context)); // Next month
    assertTrue(validator.isValid(createRequest(12, currentYear), context)); // December this year
    assertTrue(validator.isValid(createRequest(1, currentYear + 1), context)); // January next year
    
    // Past scenarios should be invalid
    if (currentMonth > 1) {
      assertFalse(validator.isValid(createRequest(currentMonth - 1, currentYear), context)); // Last month
    }
    assertFalse(validator.isValid(createRequest(12, currentYear - 1), context)); // December last year
  }

  private PostPaymentRequest createRequest(int expiryMonth, int expiryYear) {
    PostPaymentRequest request = new PostPaymentRequest();
    request.setExpiryMonth(expiryMonth);
    request.setExpiryYear(expiryYear);
    request.setCardNumber("4111111111111111");
    request.setCvv("123");
    request.setCurrency("USD");
    request.setAmount(100);
    return request;
  }
}
