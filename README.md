# Smart Banking Platform (Java 17 & Spring Boot 3.3)

A high-concurrency digital banking backend engineered with automated AI-driven financial insights. This project
demonstrates enterprise-level development standards, focusing on data integrity, fault tolerance, and context-aware LLM
integration.

---

## 🚀 Core Features & Senior Implementations

### 1. AI-Augmented Financial Insights

* **LLM Integration:** Utilizes **OpenAI's Chat Completion API** via a reactive `WebClient` to analyze user transaction
  history.
* **Contextual Awareness:** Dynamically builds structured prompts by processing real-time transaction data (type,
  amount, status) to provide personalized financial advisories.
* **Safety & Privacy:** Implements account masking logic to ensure PII (Personally Identifiable Information) is
  protected during external API calls.

### 2. Robust Fault Tolerance & Resilience

* **Reliable API Communication:** Configured dedicated `WebClient` beans with tailored timeouts (`timeout-ms`) to
  prevent downstream latency from impacting core banking threads.
* **Global Exception Handling:** Implements a centralized `@RestControllerAdvice` to manage transactional failures,
  validation errors, and business logic exceptions (e.g., `EmailAlreadyExistsException`), ensuring a unified API
  response structure.

### 3. High-Concurrency & Data Integrity

* **Transactional Safety:** (Planned) Implementing **Optimistic Locking** to manage concurrent account updates without
  data corruption.
* **Database Management:** Leverages **Spring Data JPA** with Hibernate for optimized persistence and schema management.

---

## 🏗️ Technical Architecture

* **Backend:** Java 17, Spring Boot 3.3, Spring Data JPA, Spring Validation.
* **AI Engine:** OpenAI API (GPT-4o).
* **Database:** MySQL 8.0.
* **Communication:** Reactive Spring WebClient.
* **Security:** Masking logic and Environment-based configuration (`application.yml`).

---

## 🛠️ Installation & Setup

### Prerequisites

* JDK 17 or higher
* Maven 3.6+
* MySQL 8.0
* OpenAI API Key

### Configuration

1. Clone the repository.
2. Set your environment variables in `application.yml` or your system environment:
   ```yaml
   ai:
     openai:
       api-key: ${OPENAI_API_KEY}
       base-url: "[https://api.openai.com/v1](https://api.openai.com/v1)"
       model: "gpt-4o"
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

---

## 📈 API Documentation

### AI Insights Endpoint

* **URL:** `/api/ai/transactions/generate`
* **Method:** `POST`
* **Payload:** `{ "accountNumber": "1234567890" }`
* **Description:** Fetches transaction history and returns an AI-generated summary of financial health.
