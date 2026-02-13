package com.checkout.payment.gateway.client.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class BankPaymentRequestTest {

  @Test
  void toStringShouldMaskSensitiveData() {
    BankPaymentRequest request = new BankPaymentRequest(
        "1234567890123456",
        "12/25",
        "123",
        "USD",
        10000
    );

    String result = request.toString();

    assertEquals("BankPaymentRequest[cardNumber=**** **** **** 3456, expiryDate=12/25, cvv=***, currency=USD, amount=10000]", result);
  }

  @Test
  void toStringShouldHandleNullCardNumber() {
    BankPaymentRequest request = new BankPaymentRequest(
        null,
        "12/25",
        "123",
        "USD",
        10000
    );

    String result = request.toString();

    assertEquals("BankPaymentRequest[cardNumber=null, expiryDate=12/25, cvv=***, currency=USD, amount=10000]", result);
  }

  @Test
  void toStringShouldHandleEmptyBlankCardNumber() {
    BankPaymentRequest request = new BankPaymentRequest(
        "",
        "12/25",
        "123",
        "USD",
        10000
    );

    String result = request.toString();

    assertEquals("BankPaymentRequest[cardNumber=null, expiryDate=12/25, cvv=***, currency=USD, amount=10000]", result);
  }

  @Test
  void toStringShouldHandleShortCardNumber() {
    BankPaymentRequest request = new BankPaymentRequest(
        "1234",
        "12/25",
        "123",
        "USD",
        10000
    );

    String result = request.toString();

    assertEquals("BankPaymentRequest[cardNumber=****, expiryDate=12/25, cvv=***, currency=USD, amount=10000]", result);
  }

  @Test
  void toStringShouldHandleCardNumberWithNonDigits() {
    BankPaymentRequest request = new BankPaymentRequest(
        "1234-5678-9012-3456",
        "12/25",
        "123",
        "USD",
        10000
    );

    String result = request.toString();

    assertEquals("BankPaymentRequest[cardNumber=**** **** **** 3456, expiryDate=12/25, cvv=***, currency=USD, amount=10000]", result);
  }
}