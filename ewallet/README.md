# Sterling Corporation — E-Wallet Microservices

**Team Members:** Simranpreet Singh | Preetinder Singh Kundi | Allen John

---

## Problem Statement

Sterling Corporation is developing an E-Wallet system that allows users to add money, transfer funds, and make merchant payments. The existing monolithic architecture — where user management, transactions, and balance management are tightly coupled — is creating scalability issues, deployment bottlenecks, and challenges integrating third-party payment gateways. As transaction volumes increase, the system struggles with slow API responses, high system load, and reliability concerns where failures in one component affect the entire platform.

---

## Solution: Microservices Architecture

We decomposed the monolith into **4 independent microservices**, each with its own database, deployed independently, and communicating via REST (Feign Client).

```
┌──────────────────────────────────────────────────────────────────┐
│                    Eureka Server  :8761                          │
│              (Service Registry & Discovery)                      │
└──────────────────────────────────────────────────────────────────┘
         ▲                 ▲                        ▲
         │ registers       │ registers              │ registers
         │                 │                        │
┌────────┴──────┐  ┌───────┴──────┐  ┌─────────────┴──────────┐
│ User Service  │  │Wallet Service│  │  Transaction Service    │
│   :8081       │  │   :8082      │  │        :8083            │
│               │  │              │  │                         │
│ Registration  │  │ Create wallet│  │  P2P Transfers          │
│ Login / JWT   │  │ Add money    │  │  Merchant Payments      │
│ User mgmt     │  │ Debit/Credit │  │  History lookup         │
│               │  │              │◄─┤  (calls Wallet via      │
│ H2: userdb    │  │ H2: walletdb │  │   Feign Client)         │
└───────────────┘  └──────────────┘  └─────────────────────────┘
```

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Spring Boot 3.2.5 | Microservice framework |
| Spring Cloud Eureka | Service discovery & dynamic routing |
| Spring Cloud OpenFeign | Declarative inter-service HTTP client |
| Spring Security 6 + JWT | Stateless authentication & authorisation |
| Spring Boot Actuator | Health monitoring, metrics |
| H2 Database | Lightweight in-memory DB per service |
| Spring Data JPA | ORM / repository layer |
| JUnit 5 + Mockito | Unit and integration testing |
| Lombok | Boilerplate reduction |
| Maven (multi-module) | Build management |

---

## Project Structure

```
ewallet/
├── pom.xml                          ← Parent Maven POM (multi-module)
│
├── eureka-server/                   ← Service Registry (port 8761)
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../EurekaServerApplication.java
│       └── resources/application.yml
│
├── user-service/                    ← Authentication Service (port 8081)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/sterling/user/
│       │   ├── UserServiceApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java      ← Spring Security 6 filter chain
│       │   │   └── DataSeeder.java          ← Seeds 4 demo users on startup
│       │   ├── controller/
│       │   │   ├── AuthController.java      ← /api/auth/** (public)
│       │   │   └── UserController.java      ← /api/users/** (secured)
│       │   ├── dto/
│       │   │   ├── RegisterRequest.java
│       │   │   ├── LoginRequest.java
│       │   │   ├── AuthResponse.java
│       │   │   └── UserResponse.java
│       │   ├── entity/
│       │   │   ├── User.java                ← Implements UserDetails
│       │   │   └── Role.java                ← USER | ADMIN | MERCHANT
│       │   ├── exception/
│       │   │   ├── GlobalExceptionHandler.java
│       │   │   ├── ApiError.java
│       │   │   ├── UserNotFoundException.java
│       │   │   └── DuplicateUserException.java
│       │   ├── filter/
│       │   │   └── JwtAuthFilter.java       ← OncePerRequestFilter
│       │   ├── repository/
│       │   │   └── UserRepository.java
│       │   └── service/
│       │       ├── UserService.java
│       │       └── JwtService.java          ← JJWT 0.12 token management
│       └── test/java/com/sterling/user/
│           ├── controller/AuthControllerTest.java
│           └── service/
│               ├── UserServiceTest.java
│               └── JwtServiceTest.java
│
├── wallet-service/                  ← Wallet Management Service (port 8082)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/sterling/wallet/
│       │   ├── WalletServiceApplication.java
│       │   ├── config/
│       │   │   └── DataSeeder.java          ← Seeds 4 demo wallets on startup
│       │   ├── controller/
│       │   │   └── WalletController.java
│       │   ├── dto/
│       │   │   ├── AddMoneyRequest.java
│       │   │   ├── TransferRequest.java
│       │   │   └── WalletResponse.java
│       │   ├── entity/
│       │   │   ├── Wallet.java
│       │   │   └── WalletStatus.java        ← ACTIVE | SUSPENDED | CLOSED
│       │   ├── exception/
│       │   │   ├── GlobalExceptionHandler.java
│       │   │   ├── ApiError.java
│       │   │   ├── WalletException.java
│       │   │   └── WalletNotFoundException.java
│       │   ├── repository/
│       │   │   └── WalletRepository.java
│       │   └── service/
│       │       └── WalletService.java
│       └── test/java/com/sterling/wallet/
│           ├── controller/WalletControllerTest.java
│           └── service/WalletServiceTest.java
│
└── transaction-service/             ← Transaction Orchestration Service (port 8083)
    ├── pom.xml
    └── src/
        ├── main/java/com/sterling/transaction/
        │   ├── TransactionServiceApplication.java
        │   ├── client/
        │   │   ├── WalletClient.java         ← Feign interface → wallet-service
        │   │   └── WalletClientFallback.java ← Circuit breaker fallback
        │   ├── controller/
        │   │   └── TransactionController.java
        │   ├── dto/
        │   │   ├── TransferRequest.java
        │   │   ├── PaymentRequest.java
        │   │   ├── TransactionResponse.java
        │   │   └── WalletTransferRequest.java
        │   ├── entity/
        │   │   ├── Transaction.java
        │   │   ├── TransactionType.java      ← TRANSFER | MERCHANT_PAYMENT | ADD_MONEY | REFUND
        │   │   └── TransactionStatus.java    ← PENDING | SUCCESS | FAILED | REFUNDED
        │   ├── exception/
        │   │   ├── GlobalExceptionHandler.java
        │   │   ├── ApiError.java
        │   │   └── TransactionNotFoundException.java
        │   ├── repository/
        │   │   └── TransactionRepository.java
        │   └── service/
        │       └── TransactionService.java
        └── test/java/com/sterling/transaction/
            ├── controller/TransactionControllerTest.java
            └── service/TransactionServiceTest.java
```

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 17 or higher |
| Apache Maven | 3.8 or higher |
| RAM | Minimum 4 GB |
| IDE | IntelliJ IDEA (recommended) / VS Code |

Verify your environment:
```bash
java -version     # Should show 17+
mvn -version      # Should show 3.8+
```

---

## How to Run

### Step 1: Extract and Import

```bash
unzip ewallet-microservices.zip
cd ewallet
```

**In IntelliJ IDEA:**
1. `File → Open` → select the `ewallet/` folder
2. IntelliJ auto-detects the Maven multi-module project
3. Right-click `pom.xml` → `Maven → Reload Project` to download dependencies

---

### Step 2: Start All Services (Order Matters!)

Open **4 separate terminals** and run in this order:

**Terminal 1 — Eureka Server (start first!)**
```bash
cd eureka-server
mvn spring-boot:run
```
Wait until you see: `Started EurekaServerApplication`
Then open: **http://localhost:8761** to verify the dashboard.

---

**Terminal 2 — User Service**
```bash
cd user-service
mvn spring-boot:run
```
Verify: `http://localhost:8081/api/auth/health`
Expected: `{"status": "User Service is running"}`

Demo users seeded automatically:
| Email | Password | Role |
|---|---|---|
| admin@sterling.com | Admin@123 | ADMIN |
| simran@sterling.com | Password@123 | USER |
| preetinder@sterling.com | Password@123 | USER |
| allen@sterling.com | Password@123 | MERCHANT |

---

**Terminal 3 — Wallet Service**
```bash
cd wallet-service
mvn spring-boot:run
```
Demo wallets seeded: userId 1–4 with balances ₹10,000 / ₹5,000 / ₹3,000 / ₹0

---

**Terminal 4 — Transaction Service**
```bash
cd transaction-service
mvn spring-boot:run
```

---

### Step 3: Verify All Services Are Up

Open **http://localhost:8761** — you should see all 3 microservices registered:
- `USER-SERVICE`
- `WALLET-SERVICE`
- `TRANSACTION-SERVICE`

---

## Running Tests

```bash
# Run all tests across all modules from the project root
cd ewallet
mvn test

# Run tests for a specific service
cd user-service        && mvn test
cd wallet-service      && mvn test
cd transaction-service && mvn test
```

Test coverage includes:
- `UserServiceTest` — 6 test cases (register, login, get user)
- `JwtServiceTest` — 5 test cases (generate, validate, extract)
- `AuthControllerTest` — 4 test cases (controller layer + @Valid)
- `WalletServiceTest` — 7 test cases (create, add, transfer, debit, credit)
- `WalletControllerTest` — 5 test cases
- `TransactionServiceTest` — 7 test cases (transfer, payment, history)
- `TransactionControllerTest` — 5 test cases

---

## API Reference

### H2 In-Memory Database Consoles

| Service | URL | JDBC URL |
|---|---|---|
| User Service | http://localhost:8081/h2-console | `jdbc:h2:mem:userdb` |
| Wallet Service | http://localhost:8082/h2-console | `jdbc:h2:mem:walletdb` |
| Transaction Service | http://localhost:8083/h2-console | `jdbc:h2:mem:transactiondb` |

Credentials: Username: `sa` | Password: *(leave blank)*

---

### Actuator Health Checks

```
GET http://localhost:8081/actuator/health
GET http://localhost:8082/actuator/health
GET http://localhost:8083/actuator/health
```

---

### USER SERVICE — Port 8081

#### POST /api/auth/register
Register a new user and receive a JWT token.

**Request:**
```json
{
  "fullName": "Simranpreet Singh",
  "email": "simran@sterling.com",
  "password": "Password@123",
  "phone": "9876543210"
}
```
**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "simran@sterling.com",
  "fullName": "Simranpreet Singh",
  "role": "USER",
  "expiresIn": 86400000
}
```

#### POST /api/auth/login
**Request:**
```json
{
  "email": "simran@sterling.com",
  "password": "Password@123"
}
```
**Response (200 OK):** Same as register response.

#### GET /api/users/{id}  *(requires JWT)*
```
Authorization: Bearer <token>
```
**Response (200 OK):**
```json
{
  "id": 1,
  "fullName": "Simranpreet Singh",
  "email": "simran@sterling.com",
  "phone": "9876543210",
  "role": "USER",
  "createdAt": "2024-01-01T10:00:00"
}
```

#### GET /api/users/all  *(ADMIN role only)*

---

### WALLET SERVICE — Port 8082

#### POST /api/wallets/create/{userId}
Create wallet for a user after registration.

**Response (201 Created):**
```json
{
  "id": 1,
  "userId": 1,
  "walletNumber": "STRL3A1B2C4D5E6F",
  "balance": 0.00,
  "status": "ACTIVE",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

#### GET /api/wallets/{userId}
Get wallet details including balance.

#### GET /api/wallets/{userId}/balance
```json
{ "balance": 5000.00 }
```

#### PUT /api/wallets/{userId}/add
Add money to wallet (top-up).
```json
{ "amount": 2000.00 }
```

#### POST /api/wallets/transfer
Direct wallet-to-wallet transfer (also called by Transaction Service via Feign).
```json
{
  "senderUserId": 1,
  "receiverUserId": 2,
  "amount": 500.00
}
```

---

### TRANSACTION SERVICE — Port 8083

#### POST /api/transactions/transfer
Initiate a P2P fund transfer. Internally calls Wallet Service via Feign.

**Request:**
```json
{
  "senderUserId": 1,
  "receiverUserId": 2,
  "amount": 1000.00,
  "description": "Rent split"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "referenceId": "TXN3A1B2C4D5E6F7G",
  "senderUserId": 1,
  "receiverUserId": 2,
  "amount": 1000.00,
  "type": "TRANSFER",
  "status": "SUCCESS",
  "description": "Rent split",
  "failureReason": null,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:05"
}
```

#### POST /api/transactions/payment
Make a merchant payment.
```json
{
  "userId": 1,
  "merchantId": 4,
  "amount": 250.00,
  "merchantName": "Starbucks",
  "description": "Coffee"
}
```

#### GET /api/transactions/history/{userId}
Returns all transactions (sent and received) for a user.

#### GET /api/transactions/{id}
Get transaction by ID.

#### GET /api/transactions/ref/{referenceId}
Get transaction by its reference ID (e.g. `TXN3A1B2C4D5E6F7G`).

---

## Sample Test Flow (cURL)

```bash
# 1. Register a new user
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"test@sterling.com","password":"Password@123","phone":"9000000099"}'

# 2. Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"simran@sterling.com","password":"Password@123"}'

# 3. Create wallet (userId=5 for the new user above)
curl -X POST http://localhost:8082/api/wallets/create/5

# 4. Add money to wallet
curl -X PUT http://localhost:8082/api/wallets/5/add \
  -H "Content-Type: application/json" \
  -d '{"amount": 10000.00}'

# 5. Check balance
curl http://localhost:8082/api/wallets/5/balance

# 6. Transfer money (demo: userId=2 already has a wallet with ₹5000)
curl -X POST http://localhost:8083/api/transactions/transfer \
  -H "Content-Type: application/json" \
  -d '{"senderUserId":2,"receiverUserId":3,"amount":500.00,"description":"Lunch"}'

# 7. Pay a merchant (userId=4 is the MERCHANT)
curl -X POST http://localhost:8083/api/transactions/payment \
  -H "Content-Type: application/json" \
  -d '{"userId":2,"merchantId":4,"amount":200.00,"merchantName":"Coffee Shop"}'

# 8. View transaction history
curl http://localhost:8083/api/transactions/history/2
```

---

## Error Responses

All services return a consistent error format:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields are invalid",
  "path": "/api/auth/register",
  "timestamp": "2024-01-01 10:00:00",
  "validationErrors": [
    "Password must be at least 8 characters",
    "Invalid email format"
  ]
}
```

| HTTP Status | Scenario |
|---|---|
| 400 | Validation failed (missing/invalid fields) |
| 401 | Invalid JWT token or wrong credentials |
| 403 | Access denied (insufficient role) |
| 404 | User / Wallet / Transaction not found |
| 409 | Duplicate email or phone |
| 422 | Business rule violation (insufficient balance, inactive wallet) |
| 500 | Unexpected server error |

---

## Architecture Decisions

### Why Microservices?
- **Independent scaling** — Transaction Service can be scaled horizontally during high traffic without scaling User Service
- **Fault isolation** — If Wallet Service goes down, User login still works; transactions fail gracefully with FAILED status rather than crashing the whole system
- **Independent deployment** — Each service can be deployed, updated, and rolled back independently
- **Technology flexibility** — Each service can use a different database in production

### Why Feign Client?
Feign provides declarative REST client generation. Instead of manually writing `RestTemplate` calls, we define an interface:
```java
@FeignClient(name = "wallet-service")   // Eureka resolves this name — no hardcoded URL!
public interface WalletClient {
    @PostMapping("/api/wallets/transfer")
    ResponseEntity<Map<String, String>> transfer(@RequestBody WalletTransferRequest request);
}
```
Eureka dynamically resolves `wallet-service` to the actual host:port, enabling zero-configuration load balancing.

### Why H2 In-Memory Database?
For development and testing, H2 avoids the need to install and configure PostgreSQL/MySQL. The project is designed so that switching to a production database requires only changing the `application.yml` datasource URL and adding the driver dependency.

### Transaction State Machine
Every transaction starts as `PENDING`, then transitions to `SUCCESS` or `FAILED`. This ensures the transaction record is always persisted even if the wallet operation fails — providing an audit trail for debugging and potential retry logic.

```
PENDING → SUCCESS   (wallet operation completed)
PENDING → FAILED    (wallet service error or insufficient balance)
SUCCESS → REFUNDED  (future: refund flow)
```

---

## Team

| Name | Role |
|---|---|
| Simranpreet Singh | Lead Developer — User Service & Security |
| Preetinder Singh Kundi | Developer — Wallet Service & Transaction Service |
| Allen John | Developer — Eureka, Feign Integration & Testing |

---

*Sterling Corporation E-Wallet — Microservices Architecture Project*
