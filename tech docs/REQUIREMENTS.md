# URL Shortener — Requirements & Design Thinking

## 🎯 The Problem

Users share links all the time — in tweets, messages, emails, presentations. Long URLs are ugly, hard to remember, break when copy-pasted, and provide zero insight into who's clicking them.

**A URL shortener solves this by:**
1. Converting a long URL into a short, shareable link
2. Redirecting anyone who visits the short link to the original URL
3. Optionally tracking how that link is being used

---

## 👤 User Stories (Who are we building for?)

**As a user, I want to...**

| # | User Story | Priority |
|---|-----------|----------|
| 1 | Paste a long URL and get a short link back | **P0** |
| 2 | Click a short link and be redirected to the original URL | **P0** |
| 3 | Get a meaningful error if the short link doesn't exist or is invalid | **P0** |
| 4 | Set an expiry time on my short link | **P1** |
| 5 | See how many times my link was clicked (basic analytics) | **P1** |
| 6 | Provide a custom alias (e.g., `/my-portfolio`) instead of a random code | **P1** |
| 7 | See the top referrers and geographic breakdown of clicks | **P2** |
| 8 | Manage (list, update, delete) my shortened links via a dashboard | **P2** |
| 9 | Be protected from being redirected to malicious/phishing URLs | **P2** |
| 10 | Have my links work reliably even under heavy traffic | **P2** |

---

## P0 — Must-Have (Core MVP)

These are the **non-negotiable** features. Without these, the product doesn't exist.

### 1. Shorten a URL
- **Input:** A valid long URL (e.g., `https://example.com/very/long/path?with=params`)
- **Output:** A short URL (e.g., `http://localhost:3000/abc123`)
- **Rules:**
  - Validate that the input is a well-formed URL
  - Generate a unique, short code (6–8 characters)
  - Persist the mapping (short code → long URL) in a database
  - Return the full short URL to the user

### 2. Redirect to Original URL
- **Input:** A short code via `GET /:shortCode`
- **Output:** `HTTP 301/302` redirect to the original long URL
- **Rules:**
  - Look up the short code in the database
  - If found → redirect
  - If not found → return `404 Not Found` with a clear error message

### 3. Error Handling
- **Invalid URL submitted** → `400 Bad Request` with validation message
- **Short code not found** → `404 Not Found`
- **Duplicate short code (collision)** → Retry with a new code (transparent to user)
- **Server errors** → `500 Internal Server Error` with a generic safe message (no stack traces leaked)

---

## P1 — Should-Have (Next Iteration)

These add real value and make the product *useful* beyond a toy.

### 4. Link Expiry (TTL)
- User can optionally set an expiry duration (e.g., 24h, 7d, 30d)
- Expired links return `410 Gone`
- Background cleanup job removes expired entries

### 5. Basic Click Analytics
- Every redirect increments a click counter
- API endpoint to get click count for a given short code
- Track: timestamp, user-agent, referer header (stored asynchronously to not slow down redirects)

### 6. Custom Aliases
- User can optionally provide a custom short code (e.g., `my-portfolio`)
- Validate: alphanumeric + hyphens, 3–30 chars, not already taken
- If taken → return `409 Conflict`

---

## P2 — Nice-to-Have (Future Scope)

These make the product *competitive* but are not essential for learning.

### 7. Rich Analytics Dashboard
- Click trends over time (hourly/daily)
- Top referrers, browser breakdown, device type, geo (via IP lookup)

### 8. Link Management (CRUD)
- User authentication (simple API key or JWT)
- List all my links, update destination, delete a link

### 9. URL Safety Check
- Before creating a short link, check the URL against a blocklist or a safe browsing API
- Flag/block known malicious URLs

### 10. Rate Limiting
- Prevent abuse: limit link creation to N per minute per IP
- Limit redirects per short code to prevent DDoS amplification

---

## 📐 API Contract (P0 Scope)

### `POST /api/shorten`
**Request:**
```json
{
  "url": "https://example.com/very/long/path?with=params"
}
```
**Success Response (201):**
```json
{
  "shortCode": "abc123",
  "shortUrl": "http://localhost:3000/abc123",
  "originalUrl": "https://example.com/very/long/path?with=params",
  "createdAt": "2026-02-28T03:09:00Z"
}
```
**Error Response (400):**
```json
{
  "error": "INVALID_URL",
  "message": "The provided URL is not valid."
}
```

---

### `GET /:shortCode`
**Success:** `301 Redirect` to original URL (with `Location` header)

**Error (404):**
```json
{
  "error": "NOT_FOUND",
  "message": "Short link not found."
}
```

---

## ⚖️ Key Design Decisions to Make

Before we write code, we need to decide:

| Decision | Options | Recommendation |
|----------|---------|---------------|
| **Short code generation** | Random vs Counter-based vs Hash-based | Hash-based (MD5/SHA → Base62 first 7 chars) with collision retry |
| **Redirect status code** | 301 (permanent) vs 302 (temporary) | 302 — allows analytics tracking; 301 gets cached by browsers |
| **Language/Framework** | Java, Python, Node.js, Go | *User to decide* |
| **Database** | PostgreSQL, SQLite, MongoDB | PostgreSQL (for learning relational design) or SQLite (simplicity) |
| **Architecture** | Monolith layered | Clean layered architecture (Controller → Service → Repository) |

---

## 🚀 Proposed Build Plan

| Phase | What | Focus |
|-------|------|-------|
| **Phase 1** | P0 core (shorten + redirect + errors) | Layered architecture, Strategy pattern, Repository pattern, proper error handling |
| **Phase 2** | P1 features (expiry, analytics, custom alias) | Observer pattern, background jobs, input validation |
| **Phase 3** | P2 features (dashboard, rate limiting) | Middleware pattern, auth, advanced patterns |

> We will build **Phase 1 this weekend**, with the architecture designed to cleanly support Phase 2 and 3 later thanks to proper abstractions.
