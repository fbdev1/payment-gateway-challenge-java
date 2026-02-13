package com.checkout.payment.gateway.model;


public class ErrorResponseStatus extends ErrorResponse{

  private final String status;
  private final String message;

  public ErrorResponseStatus(String status, String message) {
    this.status = status;
    this.message = message;
  }

  @Override
  public String toString() {
    return "ErrorResponse{" +
        "status='" + status + '\'' +
        ", message='" + message + '\'' +
        '}';
  }

  public String getMessage() {
    return message;
  }

  public String getStatus() {
    return status;
  }
}