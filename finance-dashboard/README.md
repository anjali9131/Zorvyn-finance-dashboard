# Finance Dashboard API

A production-quality backend for a **Finance Dashboard** system, built with **Spring Boot 3**, featuring JWT-based authentication, role-based access control, financial record management, and aggregated dashboard analytics.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture Overview](#architecture-overview)
- [Roles & Access Control](#roles--access-control)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Design Decisions & Assumptions](#design-decisions--assumptions)
- [Running Tests](#running-tests)

---

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Language     | Java 17                             |
| Framework    | Spring Boot 3.2                     |
| Security     | Spring Security + JWT (JJWT 0.11)  |
| Database     | H2 (in-memory, zero-setup)         |
| ORM          | Spring Data JPA / Hibernate         |
| Validation   | Jakarta Bean Validation             |
| Build        | Maven                               |
| Testing      | JUnit 5, Mockito, MockMvc           |

---

## Architecture Overview

```
com.finance.dashboard
├── config/          # Security config, data seeder
├── controller/      # REST controllers (thin — only HTTP concerns)
├── dto/
│   ├── request/     # Input DTOs with validation annotations
│   └── response/    # Output DTOs / API envelope
├── entity/          # JPA entities + enums
├── exception/       # Custom exceptions + global handler
├── repository/      # Spring Data JPA repositories (custom JPQL queries)
├── security/        # JWT filter + JWT utility
└── service/
    ├── *Service     # Interfaces (contracts)
    └── impl/        # Business logic implementations
```

The design follows a clean **Controller → Service → Repository** layering with no business logic leaking into controllers or entities.

---

## Roles & Access Control

| Endpoint group            | VIEWER | ANALYST | ADMIN |
|---------------------------|:------:|:-------:|:-----:|
| `POST /api/auth/**`       | ✅     | ✅      | ✅    |
| `GET /api/dashboard/**`   | ✅     | ✅      | ✅    |
| `GET /api/transactions/**`| ✅     | ✅      | ✅    |
| `POST /api/transactions`  | ❌     | ✅      | ✅    |
| `PUT /api/transactions`   | ❌     | ✅      | ✅    |
| `DELETE /api/transactions`| ❌     | ❌      | ✅    |
| `GET /api/users/**`       | ❌     | ❌      | ✅    |
| `PUT /api/users/**`       | ❌     | ❌      | ✅    |
| `DELETE /api/users/**`    | ❌     | ❌      | ✅    |

> Access control is enforced at the **HTTP layer** in `SecurityConfig` using `requestMatchers` per HTTP method — no annotation-based magic needed for the core rules.

---

## Project Structure

```
finance-dashboard/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/finance/dashboard/
    │   │   ├── FinanceDashboardApplication.java
    │   │   ├── config/
    │   │   │   ├── DataSeeder.java          ← seeds 3 default users + 15 transactions
    │   │   │   └── SecurityConfig.java
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   ├── DashboardController.java
    │   │   │   ├── TransactionController.java
    │   │   │   └── UserController.java
    │   │   ├── dto/request/
    │   │   │   ├── AuthRequest.java
    │   │   │   ├── TransactionRequest.java
    │   │   │   └── UserUpdateRequest.java
    │   │   ├── dto/response/
    │   │   │   ├── ApiResponse.java         ← unified response envelope
    │   │   │   ├── AuthResponse.java
    │   │   │   ├── DashboardSummary.java
    │   │   │   ├── TransactionResponse.java
    │   │   │   └── UserResponse.java
    │   │   ├── entity/
    │   │   │   ├── Role.java               ← VIEWER, ANALYST, ADMIN
    │   │   │   ├── Transaction.java
    │   │   │   ├── TransactionType.java    ← INCOME, EXPENSE
    │   │   │   └── User.java
    │   │   ├── exception/
    │   │   │   ├── AccessDeniedException.java
    │   │   │   ├── DuplicateResourceException.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   ├── repository/
    │   │   │   ├── TransactionRepository.java  ← custom JPQL filters + aggregations
    │   │   │   └── UserRepository.java
    │   │   ├── security/
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   └── JwtUtil.java
    │   │   └── service/
    │   │       ├── AuthService.java
    │   │       ├── DashboardService.java
    │   │       ├── TransactionService.java
    │   │       ├── UserService.java
    │   │       └── impl/
    │   │           ├── AuthServiceImpl.java
    │   │           ├── DashboardServiceImpl.java
    │   │           ├── TransactionServiceImpl.java
    │   │           └── UserServiceImpl.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/finance/dashboard/
            ├── controller/
            │   ├── AuthControllerTest.java
            │   ├── DashboardControllerTest.java
            │   └── TransactionControllerTest.java
            └── service/
                ├── TransactionServiceTest.java
                └── UserServiceTest.java
```

---

## Getting Started

### Prerequisites

- **Java 17+** — `java -version`
- **Maven 3.8+** — `mvn -version`

### Run

```bash
git clone <repo-url>
cd finance-dashboard
mvn spring-boot:run
```

The server starts on **http://localhost:8080**.

### Default Seed Users

Three users are automatically created on first startup:

| Username  | Password     | Role    |
|-----------|-------------|---------|
| `admin`   | `admin123`  | ADMIN   |
| `analyst` | `analyst123`| ANALYST |
| `viewer`  | `viewer123` | VIEWER  |

15 sample transactions spanning the last 3 months are also seeded.

### H2 Console (dev only)

```
URL:      http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:financedb
Username: sa
Password: (empty)
```

---

## API Reference

All responses follow a unified envelope:

```json
{
  "success": true,
  "message": "Optional message",
  "data": { ... },
  "errors": null
}
```

### Authentication

#### Register
```
POST /api/auth/register
```
```json
{
  "username": "john",
  "email": "john@example.com",
  "password": "secret123"
}
```
> New registrations default to the **VIEWER** role. An admin must promote them if needed.

#### Login
```
POST /api/auth/login
```
```json
{
  "username": "admin",
  "password": "admin123"
}
```
Response includes a `token`. Pass it on every subsequent request:
```
Authorization: Bearer <token>
```

---

### Transactions

#### Create
```
POST /api/transactions                    (ANALYST, ADMIN)
```
```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-04-01",
  "notes": "Monthly salary"
}
```

#### List (with filtering & pagination)
```
GET /api/transactions                     (VIEWER, ANALYST, ADMIN)

Query params:
  type        INCOME | EXPENSE
  category    partial match, case-insensitive
  startDate   ISO date, e.g. 2026-01-01
  endDate     ISO date, e.g. 2026-04-01
  page        default 0
  size        default 10, max 100
  sort        field,direction  e.g. date,desc (default)
```

#### Get by ID
```
GET /api/transactions/{id}                (VIEWER, ANALYST, ADMIN)
```

#### Update
```
PUT /api/transactions/{id}                (ANALYST, ADMIN)
```

#### Delete (soft)
```
DELETE /api/transactions/{id}             (ADMIN)
```

---

### Dashboard

#### Summary
```
GET /api/dashboard/summary                (VIEWER, ANALYST, ADMIN)
```
Returns:
```json
{
  "totalIncome": 15000.00,
  "totalExpenses": 4350.00,
  "netBalance": 10650.00,
  "incomeByCategory":   { "Salary": 15000.00 },
  "expensesByCategory": { "Rent": 2400.00, "Groceries": 750.00 },
  "recentActivity": [ /* last 10 transactions */ ],
  "monthlyTrends": [
    {
      "year": 2026, "month": 1, "monthName": "JANUARY",
      "income": 5000.00, "expenses": 1750.00, "net": 3250.00
    }
  ]
}
```

---

### Users (Admin only)

| Method | Endpoint          | Description                        |
|--------|-------------------|------------------------------------|
| GET    | `/api/users`      | List all users                     |
| GET    | `/api/users/{id}` | Get user by ID                     |
| PUT    | `/api/users/{id}` | Update email / password / role / status |
| DELETE | `/api/users/{id}` | Deactivate (soft delete) user      |
| GET    | `/api/users/me`   | Current user's own profile (any role) |

#### Update user body
```json
{
  "email": "new@email.com",
  "password": "newpassword",
  "role": "ANALYST",
  "active": true
}
```
All fields are optional — only provided fields are updated.

---

## Design Decisions & Assumptions

### Database
H2 in-memory was chosen for **zero-setup evaluation convenience**. The JPA/JPQL layer is database-agnostic; swapping to PostgreSQL or MySQL requires only a one-line `pom.xml` dependency change and updating `application.properties`.

### Soft Deletes
Both users and transactions use soft deletion (a `deleted` / `active` flag) rather than hard `DELETE` SQL. This preserves audit history and prevents orphaned foreign-key references.

### Authentication Flow
New users self-registering are assigned the **VIEWER** role. Role promotion requires an Admin to call `PUT /api/users/{id}` with `"role": "ANALYST"` or `"role": "ADMIN"`.

### Token Expiry
JWT tokens expire after **24 hours** (configurable via `app.jwt.expiration-ms`). There is no refresh-token mechanism — this is a deliberate simplification for the scope of this assessment.

### Response Envelope
All endpoints return a consistent `ApiResponse<T>` wrapper with `success`, `message`, `data`, and `errors` fields. This makes frontend integration predictable regardless of the endpoint.

### Pagination Cap
Page size is capped at 100 server-side to prevent inadvertent full-table fetches even if a client requests `size=999`.

### Category Matching
Category filter uses a case-insensitive `LIKE` query, so `"rent"` matches `"Rent"`, `"RENT"`, etc.

---

## Running Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=TransactionControllerTest

# Run with output
mvn test -Dsurefire.useFile=false
```

Tests include:
- **Unit tests** — `UserServiceTest`, `TransactionServiceTest` (Mockito, no Spring context)
- **Integration tests** — `AuthControllerTest`, `TransactionControllerTest`, `DashboardControllerTest` (full Spring context, MockMvc, in-memory H2)
