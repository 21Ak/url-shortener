# URL Shortener 🔗

A production-ready, highly scalable URL Shortening Service built with **Java 17**, **Spring Boot 3**, and **PostgreSQL**. 

This MVP converts long, unmanageable URLs into short, shareable 7-character links and reliably redirects visitors back to the original destination. 

---

## 🚀 Project Overview

Users frequently share URLs across platforms, but raw URLs can be incredibly long, visually unappealing, and prone to breaking when copied. Furthermore, raw URLs offer no tracking capabilities once shared.

This project solves these issues by providing a robust API to:
1. **Shorten:** Accept a long URL and return a mathematically unique 7-character base62 code.
2. **Redirect:** Perform an HTTP 302 temporary redirect when the short code is accessed, seamlessly routing the user to the original long URL.
3. **Validate:** Gracefully handle invalid URLs, unknown short codes, and API errors with standardized JSON error responses.

---

## 🛠️ Tech Stack

- **Language:** Java 17+
- **Framework:** Spring Boot 3.x (Web, Data JPA, Validation)
- **Database:** PostgreSQL 14+ 
- **Testing:** JUnit 5, Mockito, Spring Boot Test, H2 (In-memory DB for tests)
- **Build Tool:** Maven

---

## 🧠 System Architecture & Design Decisions

Building a URL Shortener sounds simple, but at scale, it presents unique challenges around collision resistance, database latency, and redirect tracking. Here are the key technical decisions made for this MVP:

### 1. Short Code Generation: Hash-Based vs. Random/Counters
We opted for a **Hash-Based (SHA-256 → Base62)** approach rather than a simple Random string or DB auto-increment counter. 
- **How it works:** `SHA-256(Original_URL + System.nanoTime())` -> Take first 8 bytes -> Base62 encode -> Truncate to 7 characters.
- **Why?** It prevents easily guessable/sequential URLs (which is a security flaw of counters), requires zero cross-instance synchronization (unlike counters), and minimizes the chance of collisions (compared to `SecureRandom`). 
- **Collision Handling:** In the statistically improbable event of a hash collision, the service has an automatic 3-retry fallback mechanism.

### 2. Redirect Type: HTTP 302 (Found)
We purposefully use `302 Found` (temporary redirect) instead of `301 Moved Permanently`.
- **Why?** A 301 instructs the browser to cache the redirect permanently. By using 302, every click hits our server. This allows us to implement **Click Analytics** and **Link Expiration** in the future, as we retain visibility over every redirect event.

### 3. Forward-Compatible Database Schema (P1-Ready)
The `url_mappings` table includes an `expires_at` (TIMESTAMP) and `click_count` (BIGINT) column.
- **Why?** Even though TTL and Analytics are out-of-scope for this MVP, including these nullable/default columns now means we can ship P1 features later *without* requiring a database schema migration. It costs negligible storage while massively improving extensibility. 
- Fast redirect lookups are supported by a `B-Tree` index on the `short_code` column.

### 4. Strict Layered Architecture
The codebase strictly separates concerns:
- **Controllers** handle HTTP mapping and input validation.
- **Transformers** isolate DTO-to-Entity masking.
- **Helpers** (like the `ShortCodeGenerator` Strategy interface) decouple the heavy algorithmic lifting.
- **GlobalExceptionHandler** centralizes all error state formatting.

---

## 📈 Scalability Considerations

This architecture is designed to scale horizontally as traffic grows:
1. **Stateless App Nodes:** The Spring Boot application is entirely stateless. We can spin up multiple instances behind a Load Balancer to increase throughput.
2. **Database Read Replicas:** The read path (Redirects / GET) will heavily outpace the write path (Shorten / POST). The Hot Path `findByShortCode` query can easily be routed to PostgreSQL Read Replicas.
3. **No Centralized Generators:** Because we use a Hash-based generator, we do not need a centralized service like Apache ZooKeeper or Redis to manage ID ranges.

---

## 🔮 Future Scope

The foundation is designed with extensibility in mind. Planned upcoming features include:

- **Link Expiration (TTL):** Allowing users to set an `expires_at` date, automatically returning 404s for expired campaigns.
- **Analytics & Tracking:** Utilizing the `click_count` column and a separate event-sourcing table to track geographic, referrer, and time-series data for links.
- **Caching Layer (Redis):** Implementing a Cache-Aside pattern. The hottest 20% of URLs will be stored in a Redis cluster to drastically reduce PostgreSQL I/O and drop redirect latency to sub-2ms.
- **Custom Aliases:** Permitting users to choose their own short codes (e.g., `api/shorten -> domain.com/SpringSale`).
- **Rate Limiting:** Protecting the API endpoints against abuse using sliding-window rate limiters.

---

## 💻 Running the Project Locally

### Prerequisites
- JDK 17
- Maven 3.8+
- PostgreSQL running locally on default port `5432`

### 1. Database Setup
Ensure PostgreSQL is running, then create the database:
```sql
CREATE DATABASE urlshortener;
```
*(The schema and tables will be auto-generated by Hibernate (`spring.jpa.hibernate.ddl-auto=update`) during the first run).*

### 2. Build and Test
```bash
mvn clean install
```
*(This will compile the project and run the comprehensive unit & integration test suites, utilizing an in-memory H2 database).*

### 3. Run the Application
```bash
mvn spring-boot:run
```
The server will start on `http://localhost:8080`.

---

## 📡 API Usage

### 1. Shorten a URL
```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.google.com"}'
```
**Response:** `201 Created`
```json
{
  "shortCode": "aB3xK9m",
  "shortUrl": "http://localhost:8080/aB3xK9m",
  "originalUrl": "https://www.google.com",
  "createdAt": "2026-02-28T12:00:00"
}
```

### 2. Redirect to Original URL
Open `http://localhost:8080/aB3xK9m` in your browser, or via cURL:
```bash
curl -i http://localhost:8080/aB3xK9m
```
**Response:** `302 Found`
```
HTTP/1.1 302
Location: https://www.google.com
```
