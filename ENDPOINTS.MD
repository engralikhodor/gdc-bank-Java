This document provides a comprehensive list of the available API endpoints in the banking application, including their
access levels and functional descriptions.

---

### 1. Authentication & Identity

These endpoints manage user registration and session security.

| Method | Path                  | Auth Level | Description                                                      | 
|--------|-----------------------|------------|------------------------------------------------------------------|
| POST   | /api/v1/auth/register | **Public** | Creates a new user and customer profile simultaneously.          |
| POST   | /api/v1/auth/login    | **Public** | Authenticates credentials and returns Access & Refresh tokens.   |
| POST   | /api/v1/auth/refresh  | **Public** | Validates a refresh token to issue a new 15-minute access token. |

---

### 2. AI Financial Intelligence

Endpoints leveraging OpenAI for transaction analysis and real-time streaming insights.

| Method | Path                                    | Auth Level  | Description                                                 |
|--------|-----------------------------------------|-------------|-------------------------------------------------------------|
| POST   | /api/v1/ai/transactions/generate        | **Public*** | Generates a summarized AI report of account transactions.   |
| POST   | /api/v1/ai/transactions/insights/stream | **Public*** | Streams real-time AI insights via Server-Sent Events (SSE). |

_*Note: Recommended for transition to Authenticated in production._

---

### 3. Core Banking Operations

Standard financial actions requiring an active user session:
the choice to use `POST` for these retrieval endpoints is a standard security practice in FinTech for three specific
reasons:

- **PII Protection (Log Security):** `GET` requests pass data via URL query parameters. These URLs are often captured in
  plain
  text by browser history, web server logs, and proxy servers. Since account numbers are sensitive Personal Identifiable
  Information (PII), using `POST` ensures the data stays in the request body, which is encrypted during transit (via
  HTTPS)
  and not typically logged.


- **Preventing Cache Exposure:** Browsers and intermediate network infrastructure are designed to cache `GET` responses
  by
  default. For sensitive data like a bank balance, you want to ensure that a previous response isn't cached and
  potentially viewed by another user on a shared machine. (In a professional banking environment, protecting the "trail"
  of sensitive data is more important than following the strict REST definition of `GET`).

| Method | Path                          | Auth Level        | Description                                                     |
|--------|-------------------------------|-------------------|-----------------------------------------------------------------|
| POST   | /api/v1/customers/balance     | **Authenticated** | Retrieves the current account balance for a customer.           |
| POST   | /api/v1/customers/enquiry     | **Authenticated** | Returns the full name associated with an account number.        |
| POST   | /api/v1/customers/credit      | **Authenticated** | Deposits a specified amount into the account.                   |
| POST   | /api/v1/customers/debit       | **Authenticated** | Withdraws a specified amount from the account.                  |
| POST   | /api/v1/transactions/transfer | **Authenticated** | Transfers funds between two distinct accounts (ACID compliant). |

_ACID represents the four properties that guarantee the financial transactions are processed reliably_

- A = Atomicity: The "all or nothing" rule. In the Transfer logic, it ensures that if the money is deducted from the
  sender
  but a crash occurs before it reaches the recipient, the whole operation is canceled. No money disappears into thin
  air.

- C = Consistency: The "rule follower." It ensures the database always stays in a valid state, such as preventing two
  people
  from having the same account number or ensuring a balance never becomes a null value.

- I = Isolation: The "privacy" rule. It ensures that if two different bills are being paid at the exact same time from
  the
  same account, they don't interfere with each other. Each transaction "thinks" it is the only one running.

- D = Durability: The "permanence" rule. Once a transaction is completed and you see "Success," that data is safely
  written to
  the disk. Even if the computer loses power a second later, the bank balance and the S3 receipt link will still be
  there
  when it restarts.

---

### 4. Administrative Auditing

Advanced search and filtering capabilities restricted to users with the `ADMIN` role.

| Method | Path                        | Auth Level     | Description                                             |
|--------|-----------------------------|----------------|---------------------------------------------------------|
| GET    | /api/v1/customers/search    | **Admin Only** | Dynamic multi-parameter search for customer profiles.   |
| GET    | /api/v1/transactions/search | **Admin Only** | Dynamic multi-parameter search for transaction history. |

---

### 5. Billing & Cloud Testing

Utilities for managing recurring bills and verifying AWS S3 integration.

| Method | Path                           | Auth Level        | Description                                                              |
|--------|--------------------------------|-------------------|--------------------------------------------------------------------------|
| POST   | /api/v1/bills/create           | **Authenticated** | Manually creates a pending bill for testing purposes.                    |
| POST   | /api/v1/bills/trigger-payments | **Authenticated** | Manually triggers the scheduler to process bills and upload S3 receipts. |

---

### Technical Headers

For all **Authenticated** or **Admin** endpoints, the following header must be included:

`Authorization: Bearer <YOUR_ACCESS_TOKEN>`
