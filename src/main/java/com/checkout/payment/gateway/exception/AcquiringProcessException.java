package com.checkout.payment.gateway.exception;

public class AcquiringProcessException extends RuntimeException{
  public AcquiringProcessException(String message) {
    super(message);
  }
}