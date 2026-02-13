package com.checkout.payment.gateway.validation;

import static java.time.LocalDate.now;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FutureExpiryDateValidator implements
    ConstraintValidator<FutureExpiryDate, PostPaymentRequest> {

  private static final Logger LOG = LoggerFactory.getLogger(FutureExpiryDateValidator.class);

  @Override
  public boolean isValid(PostPaymentRequest postPaymentRequest,
      ConstraintValidatorContext context) {
    if (postPaymentRequest == null) {
      return true;
    }

    boolean valid = isNotExpired(postPaymentRequest.getExpiryMonth(),
        postPaymentRequest.getExpiryYear());

    if (!valid) {
      LOG.warn("Validating expiry date failed for: month={}, year={}, valid={}",
          postPaymentRequest.getExpiryMonth(),
          postPaymentRequest.getExpiryYear(),
          valid);
    }
    return valid;
  }

  private static boolean isNotExpired(int expiryMonth, int expiryYear) {
    var currentMonth = now().getMonthValue();
    var currentYear = now().getYear();

    if (expiryYear > currentYear) {
      return true;
    }

    return expiryYear == currentYear && expiryMonth >= currentMonth;
  }
}
