# Payment Gateway Challenge

A comprehensive Java Spring Boot payment gateway application with validation, error handling, and bank integration.

## Table of Contents

- [Architecture](#architecture)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Validation](#validation)
- [Logging](#logging)
- [Luhn Algorithm Implementation](#luhn-algorithm-implementation)
- [Getting Started](#getting-started)
- [Testing](#testing)
- [Future Improvements](#future-improvements)
- [Development Guidelines](#development-guidelines)

## Architecture

The application follows a layered architecture pattern:

```
┌─────────────────────────────────────────────────────────────────┐
│                   Controller Layer                    │
│  ┌─────────────────────────────────────────────┐      │
│  │ PaymentGatewayController               │      │
│  │ - REST API endpoints                 │      │
│  │ - Request/Response validation           │      │
│  └─────────────────────────────────────────────┘      │
│                      │                          │
│                      ▼                          │
│  ┌─────────────────────────────────────────────┐      │
│  │ Service Layer                         │      │
│  │ PaymentGatewayService                 │      │
│  │ - Business logic                     │      │
│  │ - Bank integration                  │      │
│  └─────────────────────────────────────────────┘      │
│                      │                          │
│                      ▼                          │
│  ┌─────────────────────────────────────────────┐      │
│  │ Repository Layer                      │      │
│  │ PaymentsRepository                   │      │
│  │ - In-memory storage                  │      │
│  └─────────────────────────────────────────────┘      │
│                      │                          │
│                      ▼                          │
│  ┌─────────────────────────────────────────────┐      │
│  │ External Integration                   │      │
│  │ BankSimulatorInterface               │      │
│  │ - External bank calls                 │      │
│  └─────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────────┘
```

### Key Components

- **Controller Layer**: REST API endpoints with validation
- **Service Layer**: Business logic and external integration
- **Repository Layer**: In-memory data storage
- **Exception Handling**: Centralized error handling
- **Validation**: Custom validators for business rules

## Features

### Core Functionality
- **Payment Processing**: Submit and process credit card payments
- **Payment Retrieval**: Retrieve payment details by ID
- **Bank Integration**: External bank simulator for payment authorization
- **Validation**: Comprehensive input validation
- **Error Handling**: Centralized exception handling

### Validation Features

#### Card Validation
- **Luhn Algorithm Check**: Validates card number using Luhn checksum
- **Format Validation**: Ensures proper card number format (14-19 digits)
- **CVV Validation**: 3-4 digit numeric CVV
- **Expiry Date Validation**: Future date validation with month/year combination

#### Currency Validation
- **Supported Currencies**: GBP, USD, EUR only
- **ISO 4217 Compliance**: Standard currency codes
- **Case Insensitive**: Handles upper/lower case input

#### Date Validation
- **Current/Future Year**: Expiry year must be current year or future
- **Logical Date Validation**: Expiry month/year combination must be valid

## Technology Stack

### Backend
- **Java 17**: Modern Java with latest features
- **Spring Boot 3.1.5**: Web framework and dependency injection
- **Maven/Gradle**: Build and dependency management
- **Jackson**: JSON serialization/deserialization

### Validation
- **Jakarta Validation**: Standard validation annotations
- **Custom Validators**: Business rule validation
- **Luhn Algorithm**: Card number checksum validation

### Testing
- **JUnit 5**: Modern testing framework
- **MockMvc**: Spring MVC testing
- **Mockito**: Mocking framework for unit tests
- **Postman collection**: postman/Payment Gateway Challenge.postman_collection.json

### Infrastructure
- **Docker**: Containerized deployment
- **Docker Compose**: Multi-service orchestration
- **Bank Simulator**: External payment processor simulation

## Project Structure

```
src/
├── main/java/com/checkout/payment/gateway/
│   ├── controller/
│   │   └── PaymentGatewayController.java     # REST API endpoints
│   ├── service/
│   │   └── PaymentGatewayService.java       # Business logic
│   ├── repository/
│   │   └── PaymentsRepository.java        # Data storage
│   ├── model/
│   │   ├── PostPaymentRequest.java         # Payment request model
│   │   ├── PostPaymentResponse.java        # Payment response model
│   │   └── ErrorResponse.java             # Error response model
│   ├── validation/
│   │   ├── CardChecksumValidator.java       # Luhn algorithm validation
│   │   ├── CurrencyCodeValidator.java       # Currency validation
│   │   ├── CurrentOrFutureYearValidator.java # Year validation
│   │   ├── FutureExpiryDateValidator.java  # Date validation
│   │   ├── CardChecksumCheck.java          # Card checksum annotation
│   │   ├── CurrencyCode.java              # Currency annotation
│   │   ├── CurrentOrFutureYear.java       # Year annotation
│   │   └── FutureExpiryDate.java         # Date annotation
│   ├── exception/
│   │   ├── EntityNotFoundException.java     # Custom exception
│   │   ├── AcquiringProcessException.java  # Bank integration exception
│   │   └── handler/
│   │       └── CommonExceptionHandler.java  # Global exception handler
│   ├── enums/
│   │   └── PaymentStatus.java              # Payment status enum
│   └── client/
│       ├── BankSimulatorInterface.java      # Bank client interface
│       └── BankSimulatorDefaultImpl.java   # Bank client implementation
└── test/java/com/checkout/payment/gateway/
    ├── controller/
    │   ├── PaymentGatewayControllerTest.java    # Controller tests
    │   ├── PaymentGatewayControllerGetTest.java # GET endpoint tests
    │   └── PaymentGatewayControllerPostTest.java # POST endpoint tests
    ├── service/
    │   └── PaymentGatewayServiceTest.java    # Service tests
    ├── validation/
    │   ├── CardChecksumValidatorTest.java     # Card validation tests
    │   ├── CurrencyCodeValidatorTest.java     # Currency validation tests
    │   ├── CurrentOrFutureYearValidatorTest.java # Year validation tests
    │   └── FutureExpiryDateValidatorTest.java  # Date validation tests
    └── exception/
        └── handler/
            └── CommonExceptionHandlerTest.java   # Exception handler tests
```

## API Documentation

### Base URL
```
http://localhost:8090/v1/payments
```

### Endpoints

#### POST /v1/payments
Process a new payment

**Headers:**
```
Content-Type: application/json
```

**Authorised Request:**
```json
{
  "cardNumber": "378282246310005",
  "expiryMonth": 4,
  "expiryYear": 2026,
  "cvv": "123",
  "currency": "GBP",
  "amount": 100
}
```

**Authorised Response:**
```json
{
  "id": "fd36c62e-8237-4397-8e0c-cb3bbf232035",
  "status": "Authorized",
  "cardNumberLastFour": "0005",
  "expiryMonth": 4,
  "expiryYear": 2026,
  "currency": "GBP",
  "amount": 100
}
```

**Declined Request:**
```json
{
  "cardNumber": "4532015112830366",
  "expiryMonth": 4,
  "expiryYear": 2026,
  "cvv": "123",
  "currency": "GBP",
  "amount": 100
}
```

**Declined Response:**
```json
{
  "id": "37429f70-95b4-4f8e-8255-8fb07412297a",
  "status": "Declined",
  "cardNumberLastFour": "0366",
  "expiryMonth": 4,
  "expiryYear": 2026,
  "currency": "GBP",
  "amount": 100
}
```

#### GET /v1/payments/{id}
Retrieve payment by ID

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "Authorized",
  "cardNumberLastFour": "0366",
  "expiryMonth": 12,
  "expiryYear": 2025,
  "currency": "USD",
  "amount": 10000
}
```

### Swagger UI
Interactive API documentation available at:
```
http://localhost:8090/swagger-ui/index.html
```

## Validation

### Card Number Validation (Luhn Algorithm)

The application implements the **Luhn algorithm** for credit card validation:

```java
// Example: 4532015112830366
// 1. Double every second digit from right
// 2. Sum all digits
// 3. If sum % 10 == 0, card is valid


//    378282246310005 Authorised
//    371449635398431 Authorised
//    378734493671000 BadGateway
//    6011000990139424 Declined
```

**Validation Rules:**
- 14-19 digits required
- Numeric characters only
- Must pass Luhn checksum
- Last 4 digits returned in response (masked)

### Currency Validation

**Supported Currencies:**
- `GBP` - British Pound
- `USD` - US Dollar  
- `EUR` - Euro

**Validation Rules:**
- 3-character ISO 4217 codes only
- Case insensitive input handling
- Rejects unsupported currencies

### Expiry Date Validation

**Validation Rules:**
- Year must be current year or future
- If current year, month must be current or future
- Logical date combination validation
- Month range: 1-12

### Error Responses

All validation errors return structured responses:

```json
{
  "message": "Validation error description"
}
```

**Common Error Codes:**
- `400 Bad Request` - Validation failures
- `404 Not Found` - Payment not found
- `502 Bad Gateway` - Bank integration errors
- `500 Internal Server Error` - Unexpected errors

## Logging

### Application Logging

The application uses SLF4J with Logback for comprehensive logging:

**Log Levels:**
- `INFO`: Payment processing, successful operations
- `WARN`: Validation failures, business rule violations
- `ERROR`: System errors, exceptions

**Key Log Messages:**
```
INFO: Processing payment with payment ID: 550e8400-e29b-41d4-a716-446655440000
WARN: Card number ****0366 is not valid
ERROR: Exception happened
```

### Log Configuration

Logs are output to console and can be configured for file output in production environments.

## Luhn Algorithm Implementation

The Luhn algorithm is implemented in `CardChecksumValidator`:

```java
private boolean passesLuhnCheck(String cardNumber) {
    var sum = 0;
    var isEvenPositionDigit = false;
    
    // Process from right to left
    for (int i = cardNumber.length() - 1; i >= 0; i--) {
        var digit = getNumericValue(cardNumber.charAt(i));
        
        // Double every second digit
        if (isEvenPositionDigit) {
            digit *= 2;
        }
        
        // Sum digits of doubled numbers
        sum += digit / 10;
        sum += digit % 10;
        isEvenPositionDigit = !isEvenPositionDigit;
    }
    
    return sum % 10 == 0;
}
```

**Algorithm Steps:**
1. Starting from rightmost digit, double every second digit
2. If doubled value > 9, add digits (e.g., 16 → 1 + 6 = 7)
3. Sum all resulting digits
4. If total modulo 10 equals 0, card number is valid

## Getting Started

### Prerequisites
- **JDK 17** or higher
- **Docker** and Docker Compose
- **Gradle** or Maven build tool

### Local Development Setup

1. **Clone the repository:**
   ```bash
   git clone git@github.com:fbdev1/payment-gateway-challenge-java.git
   cd payment-gateway-challenge-java
   ```

2. **Start the bank simulator:**
   ```bash
   docker-compose up -d
   ```

3. **Build the application:**
   ```bash
   ./gradlew build
   ```

4. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

5. **Access the API:**
   - Application: `http://localhost:8090`
   - Swagger UI: `http://localhost:8090/swagger-ui/index.html`

### Configuration

The application uses default Spring Boot configuration. Custom properties can be added to `application.properties`:

```properties
# Server configuration
server.port=8090

# Logging configuration
logging.level.com.checkout.payment.gateway=INFO

#Bank client simulator url
client.url.default=http://localhost:8080
```

## Testing

### Test Coverage

The application has comprehensive test coverage:

- **Unit Tests**: Service layer, validators, utilities
- **Integration Tests**: Controller endpoints, repository layer
- **Validation Tests**: All custom validators
- **Exception Tests**: Error handling scenarios

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests PaymentGatewayServiceTest
```

### Test Structure

- **Controller Tests**: MockMvc for HTTP endpoint testing
- **Service Tests**: Mockito for dependency mocking
- **Validator Tests**: Edge cases and boundary conditions
- **Exception Tests**: Error response validation

## Future Improvements

### Immediate Enhancements

1. **Database Integration**
   - Replace in-memory storage with PostgreSQL
   - Add connection pooling and transaction management
   - Implement data migration scripts(ie LiquiBase)

2. **Security Features**
   - Idempotency check
   - API key authentication
   - Rate limiting per client
   - Enhanced idempotency with distributed cache

3. **Monitoring & Observability**
   - Micrometer metrics collection
   - Health check endpoints
   - Distributed tracing with Jaeger
   - Custom dashboards

4. **Performance Optimizations**
   - Caching for frequently accessed payments
   - Async payment processing
   - Connection pooling for bank client
   - Database query optimization
   - Retry mechanism for external systems
   - Circuit breaker for external systems
---

## Development Guidelines

### Code Style
- Follow existing patterns and conventions
- Use meaningful variable and method names
- Add comprehensive unit tests for new features
- Document public APIs with JavaDoc

### Git Workflow
1. Create feature branch from main
2. Implement changes with tests
3. Ensure all tests pass
4. Submit pull request for review
5. Merge after approval

For questions or support, refer to the original challenge documentation and existing codebase patterns.