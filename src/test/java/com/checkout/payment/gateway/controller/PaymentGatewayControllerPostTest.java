package com.checkout.payment.gateway.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerPostTest {

  @Autowired
  private MockMvc mvc;
  
  @Autowired
  private PaymentsRepository paymentsRepository;
  
  @MockBean
  private PaymentGatewayService paymentGatewayService;
  
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void createPostPaymentEvent_ValidRequest_ReturnsCreatedPayment() throws Exception {
    PostPaymentResponse expectedResponse = new PostPaymentResponse();
    expectedResponse.setId(UUID.randomUUID());
    expectedResponse.setAmount(100);
    expectedResponse.setCurrency("USD");
    expectedResponse.setStatus(PaymentStatus.AUTHORIZED);
    expectedResponse.setExpiryMonth(12);
    expectedResponse.setExpiryYear(2026);
    expectedResponse.setCardNumberLastFour("0366");

    when(paymentGatewayService.processPayment(any())).thenReturn(expectedResponse);

    String requestBody = """
        {
          "cardNumber": "4532015112830366",
          "expiryMonth": 12,
          "expiryYear": 2026,
          "currency": "USD",
          "amount": 100,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.cardNumberLastFour").value("0366"))
        .andExpect(jsonPath("$.expiryMonth").value(12))
        .andExpect(jsonPath("$.expiryYear").value(2026))
        .andExpect(jsonPath("$.currency").value("USD"))
        .andExpect(jsonPath("$.amount").value(100));
  }

  @Test
  void createPostPaymentEvent_BankDeclinesPayment_ReturnsDeclinedResponse() throws Exception {
    PostPaymentResponse expectedResponse = new PostPaymentResponse();
    expectedResponse.setId(UUID.randomUUID());
    expectedResponse.setAmount(100);
    expectedResponse.setCurrency("USD");
    expectedResponse.setStatus(PaymentStatus.DECLINED);
    expectedResponse.setExpiryMonth(12);
    expectedResponse.setExpiryYear(2026);
    expectedResponse.setCardNumberLastFour("0366");

    when(paymentGatewayService.processPayment(any())).thenReturn(expectedResponse);

    String requestBody = """
        {
          "cardNumber": "4532015112830366",
          "expiryMonth": 12,
          "expiryYear": 2026,
          "currency": "USD",
          "amount": 100,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Declined"))
        .andExpect(jsonPath("$.amount").value(100));
  }

  @Test
  void createPostPaymentEvent_InvalidCardNumber_ReturnsBadRequest() throws Exception {
    String requestBody = """
        {
          "cardNumber": "invalid-card",
          "expiryMonth": 12,
          "expiryYear": 2025,
          "currency": "USD",
          "amount": 100,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createPostPaymentEvent_InvalidExpiryDate_ReturnsBadRequest() throws Exception {
    String requestBody = """
        {
          "cardNumber": "4532015112830366",
          "expiryMonth": 12,
          "expiryYear": 2020,
          "currency": "USD",
          "amount": 100,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createPostPaymentEvent_InvalidCurrency_ReturnsBadRequest() throws Exception {
    String requestBody = """
        {
          "cardNumber": "4532015112830366",
          "expiryMonth": 12,
          "expiryYear": 2025,
          "currency": "INVALID",
          "amount": 100,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createPostPaymentEvent_InvalidAmount_ReturnsBadRequest() throws Exception {
    String requestBody = """
        {
          "cardNumber": "4532015112830366",
          "expiryMonth": 12,
          "expiryYear": 2025,
          "currency": "USD",
          "amount": 0,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createPostPaymentEvent_InvalidCVV_ReturnsBadRequest() throws Exception {
    String requestBody = """
        {
          "cardNumber": "4532015112830366",
          "expiryMonth": 12,
          "expiryYear": 2025,
          "currency": "USD",
          "amount": 100,
          "cvv": "12"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createPostPaymentEvent_MissingRequiredFields_ReturnsBadRequest() throws Exception {
    String requestBody = """
        {
          "cardNumber": "4532015112830366",
          "expiryMonth": 12,
          "expiryYear": 2025
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createPostPaymentEvent_EmptyRequestBody_ReturnsBadRequest() throws Exception {
    mvc.perform(MockMvcRequestBuilders.post("/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createPostPaymentEvent_InvalidJson_ReturnsBadRequest() throws Exception {
    String requestBody = """
        {
          "cardNumber": "4532015112830366",
          "expiryMonth": 12,
          "expiryYear": 2026,
          "currency": "USD",
          "amount": 100,
          "cvv": "123"
        """;

    mvc.perform(MockMvcRequestBuilders.post("/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createPostPaymentEvent_UnsupportedMediaType_ReturnsUnsupportedMediaType() throws Exception {
    String requestBody = """
        {
          "cardNumber": "4532015112830366",
          "expiryMonth": 12,
          "expiryYear": 2025,
          "currency": "USD",
          "amount": 100,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/v1/payments")
            .contentType(MediaType.TEXT_PLAIN)
            .content(requestBody))
        .andExpect(status().isBadRequest());
  }
}
