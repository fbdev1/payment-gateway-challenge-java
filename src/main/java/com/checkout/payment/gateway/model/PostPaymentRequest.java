package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.validation.CardChecksumCheck;
import com.checkout.payment.gateway.validation.CurrencyCode;
import com.checkout.payment.gateway.validation.CurrentOrFutureYear;
import com.checkout.payment.gateway.validation.FutureExpiryDate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@FutureExpiryDate(message = "Expiry date is not valid")
public class PostPaymentRequest implements Serializable {

  @NotNull(message = "Card number must not be empty")
  @Size(min = 14, max = 19, message = "Card number must be between 14 and 19 characters long")
  @Pattern(regexp = "^\\d+$", message = "Card number must be 14-19 numeric characters long")
  @CardChecksumCheck
  private String cardNumber;

  @NotNull(message = "Expiry month must not be empty")
  @Min(value = 1, message = "Invalid value of the expiry month, value should be in a range 1-12")
  @Max(value = 12, message = "Invalid value of the expiry month, value should be in a range 1-12")
  private int expiryMonth;

  @NotNull(message = "Expiry year must not be empty")
  @CurrentOrFutureYear
  private int expiryYear;

  @NotNull(message = "Currency must not be empty")
  @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters long")
  @CurrencyCode
  private String currency;

  @NotNull(message = "Amount must not be empty")
  @Min(value = 1, message = "Invalid value of the amount, value must be more than 0")
  @Max(value = Integer.MAX_VALUE-1, message = "Invalid value of the amount, value must be less than " + Integer.MAX_VALUE)
  private int amount;

  @NotNull(message = "CVV must not be empty")
  @Size(min = 3, max = 4, message = "CVV must be 3 or 4 characters long")
  @Pattern(regexp = "^\\d+$", message = "CVV must only contain numeric characters")
  private String cvv;

  public String getExpiryDate() {
    return String.format("%d/%d", expiryMonth, expiryYear);
  }

  @Override
  public String toString() {
    return "PostPaymentRequest{" +
        "cardNumberLastFour=" +  getCardNumberLastFour()+
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        ", cvv=" + cvv +
        '}';
  }

  public String getCardNumberLastFour() {
    if (cardNumber == null || cardNumber.length() < 4) {
      return "";
    }
    return cardNumber.substring(cardNumber.length() - 4);
  }

  public int getExpiryMonth() {
    return expiryMonth;
  }

  public int getExpiryYear() {
    return expiryYear;
  }

  public String getCurrency() {
    return currency;
  }

  public int getAmount() {
    return amount;
  }

  public String getCvv() {
    return cvv;
  }

  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  public void setExpiryMonth(int expiryMonth) {
    this.expiryMonth = expiryMonth;
  }

  public void setExpiryYear(int expiryYear) {
    this.expiryYear = expiryYear;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public void setCvv(String cvv) {
    this.cvv = cvv;
  }
}
