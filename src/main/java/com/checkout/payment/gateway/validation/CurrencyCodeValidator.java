package com.checkout.payment.gateway.validation;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Currency;
import java.util.List;

public class CurrencyCodeValidator implements ConstraintValidator<CurrencyCode, String> {

  private static final Logger LOG = LoggerFactory.getLogger(CurrencyCodeValidator.class);

  private final List<Currency> SUPPORTED_CURRENCIES = List.of(
      Currency.getInstance("GBP"),
      Currency.getInstance("USD"),
      Currency.getInstance("EUR")
  );

  @Override
  public boolean isValid(String currencyCode, ConstraintValidatorContext constraintValidatorContext) {
    if (isEmpty(currencyCode)) return false;
    try {
      var currency = Currency.getInstance(currencyCode.toUpperCase());
      boolean contains = SUPPORTED_CURRENCIES.contains(currency);
      if(!contains){
        LOG.warn("Currency code {} is not supported", currencyCode);
      }
      return contains;
    } catch (Exception e) {
      return false;
    }
  }
}
