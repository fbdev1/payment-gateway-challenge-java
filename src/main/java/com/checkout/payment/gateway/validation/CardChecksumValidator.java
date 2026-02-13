package com.checkout.payment.gateway.validation;

import static java.lang.Character.getNumericValue;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CardChecksumValidator implements ConstraintValidator<CardChecksumCheck, String> {

  private static final Logger LOG = LoggerFactory.getLogger(CardChecksumValidator.class.getName());
  @Override
  public boolean isValid(String cardNumber, ConstraintValidatorContext context) {
    if (!isNumeric(cardNumber)) return false;

    boolean valid = passesLuhnCheck(cardNumber);
    if(!valid){
      String lastFour = cardNumber.length() >= 4 ?
          cardNumber.substring(cardNumber.length() - 4) : cardNumber;
      LOG.warn("Card number ****{} is not valid", lastFour);
    }
    return valid;
  }

  //https://en.wikipedia.org/wiki/Luhn_algorithm
  private boolean passesLuhnCheck(String cardNumber) {
    if (isEmpty(cardNumber) || !isNumeric(cardNumber)) return false;

    var sum = 0;
    var isEvenPositionDigit = false;
    for (int i = cardNumber.length() - 1; i >= 0; i--) {
      var digit = getNumericValue(cardNumber.charAt(i));
      if (isEvenPositionDigit) {
        digit *= 2;
      }
      sum += digit / 10;
      sum += digit % 10;
      isEvenPositionDigit = !isEvenPositionDigit;
    }
    return sum % 10 == 0;
  }

}
