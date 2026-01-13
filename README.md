A backend banking-style application built with Spring Boot, focused on
customer management, account transactions, internal transfers, and
AI-powered transaction insights.

---

## Tech Stack

- Java 17
- Spring Boot 3.3.0
- Spring Data JPA
- Spring Security
- Hibernate
- WebClient
- OpenAI API
- MySQL (relational database)
- Async processing (`@Async`)

---

### Authentication & Users

- User registration
- User login endpoint
- Password encryption (BCrypt)
- Basic Spring Security configuration

---

### Customer Management

- Create customer profiles
- Search customers using dynamic criteria
- Validation for unique identifiers (email, phone, government ID)

---

### Accounts & Transactions

- Accounts linked to customers
- Credit and debit transactions
- Internal transfers between accounts
- Balance consistency enforced at service layer
- Transfer validation (same-account, insufficient balance, transfer limits)

---

### Transaction Model

- Transactions persisted as first-class domain objects
- Transaction types supported (CREDIT, DEBIT, TRANSFER)
- Transaction status enum defined (foundation for lifecycle handling)

---

### Advanced Querying

- Dynamic filtering using JPA Specifications
- Criteria-based searches for customers and transactions
- Query layer designed to support pagination extension

---

### AI-Powered Insights

- Aggregation of transaction data
- Prompt construction for financial analysis
- Integration with OpenAI using WebClient
- AI used strictly for insights (read-only analysis)

---

### Email Notifications

- Email service abstraction
- Asynchronous email sending using `@Async`
- Triggered by business events

---

### Architecture & Code Quality

- Layered architecture (Controller / Service / Repository)
- DTO-based APIs (no entity leakage)
- Bean Validation
- Transactional service boundaries
- Centralized exception handling
- Initial data seeding for development/testing

---

## Current Scope

This project focuses on:

- Business correctness over infrastructure
- Financial domain modeling
- Clean service-layer logic
- Extensibility toward production readiness

Future enhancements (security hardening, observability, deployment, etc.)
are intentionally separated from the current scope.

---

## Author Notes

This repository is designed to evolve incrementally toward a
production-grade financial backend while keeping business logic explicit
and testable.
