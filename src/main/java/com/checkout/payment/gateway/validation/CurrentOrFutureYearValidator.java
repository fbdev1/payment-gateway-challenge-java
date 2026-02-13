package com.checkout.payment.gateway.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrentOrFutureYearValidator implements ConstraintValidator<CurrentOrFutureYear, Integer> {

  private static final Logger LOG = LoggerFactory.getLogger(CurrentOrFutureYearValidator.class);

  @Override
  public boolean isValid(Integer expiryYear, ConstraintValidatorContext context) {
    if (expiryYear == null) return false;

    var currentYear = java.time.Year.now().getValue();
    boolean valid = expiryYear >= currentYear;
    if(!valid){
      LOG.warn("Expiry year {} is not valid", expiryYear);
    }
    return valid;
  }
}
