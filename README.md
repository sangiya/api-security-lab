# api-security-lab

Production-grade API security reference on Spring Boot 3.3.5 (Java 21) demonstrating four
core security dimensions for a banking-style token API: **JWT authentication**, **OAuth2
client-credentials (simulated)**, **per-IP rate limiting**, and **input validation**.

## Security dimensions

| Dimension | What it covers |
|-----------|----------------|
| JWT authentication | HS256-signed access tokens via JJWT 0.12.6; `JwtAuthFilter` validates Bearer tokens and populates the `SecurityContext` |
| OAuth2 client-credentials (simulated) | A dedicated `/api/auth/token` (see below) mints short-lived bearer tokens the way an OAuth2 client-credentials flow would, for service-to-service calls |
| Rate limiting | Fixed-window limiter per client IP on the auth endpoints, returning HTTP 429 with a consistent JSON body |
| Input validation | Jakarta Validation (`@Valid`, `@NotNull`, `@Positive`, `@Pattern`) plus a custom `@ValidAccountNumber` constraint on the transfer endpoint |

## Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/auth/login` | none | Authenticates username/password, returns a signed JWT |
| `POST` | `/api/auth/token` | client_id/client_secret | Simulated OAuth2 client-credentials flow, returns a bearer token |
| `GET` | `/api/accounts` | JWT | Returns the accounts of the authenticated user |
| `POST` | `/api/transfers` | JWT | Validated transfer; enforces amount > 0, max amount, and recipient account format |

## Login and use a token

```bash
# 1. Log in (rate limited per IP)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'

# Response:
# {"accessToken":"eyJhbGciOiJIUzI1NiJ9...","tokenType":"Bearer","username":"user","role":"USER","expiresIn":900000}

# 2. Call a protected endpoint with the token
curl http://localhost:8080/api/accounts \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

### Simulated OAuth2 client-credentials

```bash
curl -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"clientId":"service-a","clientSecret":"secret-a"}'
```

### Transfer with validation

```bash
# Valid transfer
curl -X POST http://localhost:8080/api/transfers \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"amount":250.00,"recipient":"USR-12345","reference":"rent"}'

# Invalid (amount <= 0 or recipient format wrong) -> 400 with field errors
curl -X POST http://localhost:8080/api/transfers \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"amount":-5.00,"recipient":"bad","reference":"tax"}'
```

## Error format

All errors return a consistent JSON body:

```json
{
  "timestamp": "2024-01-01T00:00:00.000Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password",
  "path": "/api/auth/login",
  "fieldErrors": null
}
```

Validation failures (`400`) populate `fieldErrors`:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "fieldErrors": [
    { "field": "recipient", "message": "recipient account number must match format XXX-#####" }
  ]
}
```

Status codes surfaced by the API: `400` (validation), `401` (unauthenticated / bad
credentials), `403` (access denied), `429` (rate limited), `422` (semantic errors).

## Users (in-memory)

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |

Passwords are stored only as BCrypt hashes. Replace `UserService` with a real user store in
production.

## Configuration

`src/main/resources/application.yml`:

```yaml
app:
  jwt:
    secret: "<HMAC-SHA256 secret, >= 32 bytes>"
    access-token-expiry-ms: 900000
    issuer: api-security-lab
  ratelimit:
    max-requests: 10
    window-ms: 60000
    protected-paths: "/api/auth/login"
```

## Run tests

```bash
mvn -B test
```

## Project layout

```
src/main/java/com/sangiya/apisec/
├── auth/        # LoginRequest/Response, AuthController
├── jwt/         # JwtService, JwtAuthFilter
├── config/      # SecurityConfig (stateless chain + filter ordering)
├── account/     # Account, AccountService, AccountController
├── transfer/    # TransferRequest (validated), TransferService, TransferController
├── ratelimit/   # RateLimiter (fixed-window), RateLimitFilter
├── error/       # ErrorResponse, GlobalExceptionHandler
├── model/       # UserService (in-memory BCrypt users)
└── ApiSecurityLabApplication.java
```
