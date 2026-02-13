package com.checkout.payment.gateway.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BankPaymentRequest(@JsonProperty("card_number") String cardNumber,
                                 @JsonProperty("expiry_date") String expiryDate,
                                 String cvv, String currency, int amount) {

  private static String maskPan(String pan) {
    if (pan == null || pan.isBlank()) return "null";
    String digitsOnly = pan.replaceAll("\\D+", "");
    if (digitsOnly.length() < 8) return "****";
    String last4 = digitsOnly.substring(digitsOnly.length() - 4);
    return "**** **** **** " + last4;
  }

  @Override
  public String toString() {
    return "BankPaymentRequest[" +
        "cardNumber=" + maskPan(cardNumber) +
        ", expiryDate=" + expiryDate +
        ", cvv=***" +
        ", currency=" + currency +
        ", amount=" + amount +
        "]";
  }
}

