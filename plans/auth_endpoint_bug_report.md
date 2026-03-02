# Auth Endpoint Bug Report

## Summary
The `/auth/*` endpoints have been analyzed for bugs and issues. The following report documents findings from reviewing the AuthController, AuthService, JwtTokenProvider, SecurityConfig, and GlobalExceptionHandler.

---

## Critical Bugs

### 1. Duplicate Authentication Call in Registration
**Location:** [`AuthService.register()`](backend/src/main/java/com/healthytom/service/AuthService.java:48)

**Issue:** After successfully saving a new user to the database, the method calls `authenticateAndGenerateTokens()` which performs authentication again. This is redundant and could cause unnecessary overhead.

```java
User savedUser = userRepository.save(user);
return authenticateAndGenerateTokens(savedUser.getEmail(), request.getPassword());
```

---

### 2. Missing Role Validation
**Location:** [`AuthService.register()`](backend/src/main/java/com/healthytom/service/AuthService.java:38)

**Issue:** The role value from the request is directly converted to uppercase without validation. If an invalid role is provided, `IllegalArgumentException` will be thrown but not properly handled.

```java
.role(User.UserRole.valueOf(request.getRole().toUpperCase()))
```

**Impact:** If a user sends an invalid role like "ADMIN" (when only OWNER/VETERINARIAN are valid), the endpoint will return a generic 500 Internal Server Error instead of a proper 400 Bad Request.

---

### 3. No Input Validation on RegisterRequest
**Location:** [`RegisterRequest`](backend/src/main/java/com/healthytom/dto/RegisterRequest.java)

**Issue:** No validation annotations (e.g., `@NotBlank`, `@Email`, `@Size`) on the DTO fields. This allows:
- Empty or null email addresses
- Weak passwords (empty or very short)
- Missing required fields

---

### 4. RuntimeException Used Instead of Custom Exceptions
**Location:** [`AuthService`](backend/src/main/java/com/healthytom/service/AuthService.java:29, 67, 72, 93)

**Issue:** Generic `RuntimeException` is thrown for business logic errors. This makes error handling inconsistent and error messages less descriptive.

```java
throw new RuntimeException("Email already in use");
throw new RuntimeException("Invalid refresh token");
throw new RuntimeException("User not found");
```

**Impact:** All these errors are caught by the generic `RuntimeException` handler in GlobalExceptionHandler and return HTTP 500 instead of more appropriate status codes (409 for duplicate, 401 for invalid token, 404 for user not found).

---

### 5. Refresh Token Doesn't Rotate
**Location:** [`AuthService.refreshToken()`](backend/src/main/java/com/healthytom/service/AuthService.java:75)

**Issue:** Every time a refresh token is used, a new refresh token is generated, but the old one is not invalidated. This is a security concern as old tokens remain valid.

```java
String refreshToken = jwtTokenProvider.generateRefreshToken(email);
```

---

## Medium Issues

### 6. Inconsistent Token Generation for Refresh
**Location:** [`JwtTokenProvider.generateAccessTokenFromUsername()`](backend/src/main/java/com/healthytom/security/JwtTokenProvider.java:44-53)

**Issue:** When refreshing tokens, the access token is generated without the "authorities" claim, while the original access token includes it. This could cause authorization issues if the client relies on the access token authorities.

```java
// Missing authorities claim in refresh flow
return Jwts.builder()
    .setSubject(username)
    .setIssuedAt(new Date())
    .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
    .signWith(key, SignatureAlgorithm.HS512)
    .compact();
```

---

### 7. Weak JWT Secret in Default Configuration
**Location:** [`application.properties:20`](backend/src/main/resources/application.properties:20)

**Issue:** The default JWT secret is hardcoded and should be changed in production:
```
app.jwt.secret=your-super-secret-jwt-key-change-this-in-production-must-be-at-least-256-bits-long
```

---

### 8. No Rate Limiting
**Location:** Auth endpoints

**Issue:** The `/auth/register` and `/auth/login` endpoints have no rate limiting protection, making them vulnerable to brute force attacks.

---

### 9. Password Not Validated for Minimum Requirements
**Location:** [`RegisterRequest`](backend/src/main/java/com/healthytom/dto/RegisterRequest.java)

**Issue:** No minimum password length or complexity requirements are enforced.

---

### 10. Missing Email Format Validation
**Location:** [`RegisterRequest`](backend/src/main/java/com/healthytom/dto/RegisterRequest.java)

**Issue:** Email format is not validated at the DTO level.

---

## Low Issues

### 11. Logging Sensitive Information
**Location:** [`AuthController`](backend/src/main/java/com/healthytom/controller/AuthController.java:22, 29)

**Issue:** Email is logged, which could be considered PII (Personally Identifiable Information) depending on compliance requirements.

```java
log.info("Registration request for email: {}", request.getEmail());
log.info("Login request for email: {}", request.getEmail());
```

---

### 12. CORS Allowed Origins Hardcoded
**Location:** [`SecurityConfig.java:78`](backend/src/main/java/com/healthytom/config/SecurityConfig.java:78)

**Issue:** Allowed origins are hardcoded instead of being configurable via properties.

---

## Recommendations

1. Add input validation annotations to `RegisterRequest` and `LoginRequest` DTOs
2. Create custom exceptions (e.g., `EmailAlreadyExistsException`, `InvalidTokenException`) with proper HTTP status codes
3. Add rate limiting to auth endpoints
4. Add password strength validation
5. Add email format validation
6. Consider token rotation strategy for refresh tokens
7. Make JWT secret configurable without a default value
8. Consider adding email verification flow
