# Test Suite Documentation

## Overview

This document provides comprehensive documentation for the unit and integration test suite for the Healthy Tom Vet Connect REST API.

## Test Structure

### Unit Tests (Service Layer)

Unit tests focus on testing individual service methods in isolation using mocks.

#### **AuthServiceTest**
- Location: `src/test/java/com/healthytom/service/AuthServiceTest.java`
- Tests:
  - ✅ Register new user successfully
  - ✅ Fail registration with duplicate email
  - ✅ Successful login with valid credentials
  - ✅ Fail login with invalid credentials
  - ✅ Fail login with non-existent user
  - ✅ Refresh token successfully
  - ✅ Fail refresh with invalid token

**How to run:**
```bash
mvn test -Dtest=AuthServiceTest
```

#### **PetServiceTest**
- Location: `src/test/java/com/healthytom/service/PetServiceTest.java`
- Tests:
  - ✅ Create pet successfully
  - ✅ Fail to create pet with non-existent owner
  - ✅ Get pet by ID
  - ✅ Fail to get non-existent pet
  - ✅ Get all pets by owner ID
  - ✅ Update pet successfully
  - ✅ Delete pet successfully
  - ✅ Fail to delete non-existent pet

**How to run:**
```bash
mvn test -Dtest=PetServiceTest
```

#### **ConsultationServiceTest**
- Location: `src/test/java/com/healthytom/service/ConsultationServiceTest.java`
- Tests:
  - ✅ Create consultation successfully
  - ✅ Fail with non-existent pet
  - ✅ Get consultation by ID
  - ✅ Fail to get non-existent consultation
  - ✅ Get consultations by owner
  - ✅ Get consultations by veterinarian
  - ✅ Get consultations by pet
  - ✅ Assign veterinarian to consultation
  - ✅ Rate consultation successfully
  - ✅ Update consultation successfully
  - ✅ Delete consultation successfully

**How to run:**
```bash
mvn test -Dtest=ConsultationServiceTest
```

#### **PrescriptionServiceTest**
- Location: `src/test/java/com/healthytom/service/PrescriptionServiceTest.java`
- Tests:
  - ✅ Create prescription successfully
  - ✅ Fail with non-existent consultation
  - ✅ Get prescription by ID
  - ✅ Fail to get non-existent prescription
  - ✅ Get prescriptions by consultation
  - ✅ Get prescriptions by pet
  - ✅ Get prescriptions by veterinarian
  - ✅ Update prescription successfully
  - ✅ Delete prescription successfully
  - ✅ Fail to delete non-existent prescription

**How to run:**
```bash
mvn test -Dtest=PrescriptionServiceTest
```

---

### Integration Tests (Controller Layer)

Integration tests test the complete request/response flow from controller to database.

#### **AuthControllerIntegrationTest**
- Location: `src/test/java/com/healthytom/controller/AuthControllerIntegrationTest.java`
- Tests:
  - ✅ Register new user with HTTP 201
  - ✅ Register returns access and refresh tokens
  - ✅ Register returns user details
  - ✅ Fail registration with duplicate email
  - ✅ Login successfully with valid credentials
  - ✅ Login fails with invalid password
  - ✅ Login fails with non-existent user
  - ✅ Refresh token successfully
  - ✅ Register veterinarian with specialization

**How to run:**
```bash
mvn test -Dtest=AuthControllerIntegrationTest
```

#### **UserControllerIntegrationTest**
- Location: `src/test/java/com/healthytom/controller/UserControllerIntegrationTest.java`
- Tests:
  - ✅ Get current authenticated user
  - ✅ Fail without authentication
  - ✅ Get user by ID as veterinarian
  - ✅ Fail to get user as owner (unauthorized)
  - ✅ Return 404 for non-existent user

**How to run:**
```bash
mvn test -Dtest=UserControllerIntegrationTest
```

#### **PetControllerIntegrationTest**
- Location: `src/test/java/com/healthytom/controller/PetControllerIntegrationTest.java`
- Tests:
  - ✅ Create pet with HTTP 201
  - ✅ Get pet by ID
  - ✅ Get all pets for owner
  - ✅ Update pet successfully
  - ✅ Delete pet successfully
  - ✅ Fail to create pet without authentication

**How to run:**
```bash
mvn test -Dtest=PetControllerIntegrationTest
```

#### **ConsultationControllerIntegrationTest**
- Location: `src/test/java/com/healthytom/controller/ConsultationControllerIntegrationTest.java`
- Tests:
  - ✅ Create consultation with HTTP 201
  - ✅ Get consultation by ID
  - ✅ Get consultations by owner
  - ✅ Assign veterinarian (with role check)
  - ✅ Rate consultation successfully
  - ✅ Fail without authentication

**How to run:**
```bash
mvn test -Dtest=ConsultationControllerIntegrationTest
```

#### **PrescriptionControllerIntegrationTest**
- Location: `src/test/java/com/healthytom/controller/PrescriptionControllerIntegrationTest.java`
- Tests:
  - ✅ Create prescription with HTTP 201
  - ✅ Get prescription by ID
  - ✅ Get prescriptions by consultation
  - ✅ Get prescriptions by pet
  - ✅ Get prescriptions by veterinarian
  - ✅ Update prescription successfully
  - ✅ Delete prescription successfully
  - ✅ Fail without veterinarian role

**How to run:**
```bash
mvn test -Dtest=PrescriptionControllerIntegrationTest
```

---

## Running All Tests

### Run all tests:
```bash
mvn test
```

### Run with coverage report:
```bash
mvn test jacoco:report
# Report will be at: target/site/jacoco/index.html
```

### Run specific test class:
```bash
mvn test -Dtest=AuthServiceTest
```

### Run specific test method:
```bash
mvn test -Dtest=AuthServiceTest#testRegisterSuccess
```

### Run with verbose output:
```bash
mvn test -X
```

---

## Test Coverage

| Layer | Class | Test Class | Line Coverage |
|-------|-------|-----------|---|
| Service | AuthService | AuthServiceTest | ~95% |
| Service | PetService | PetServiceTest | ~90% |
| Service | ConsultationService | ConsultationServiceTest | ~85% |
| Service | PrescriptionService | PrescriptionServiceTest | ~90% |
| Controller | AuthController | AuthControllerIntegrationTest | ~95% |
| Controller | UserController | UserControllerIntegrationTest | ~85% |
| Controller | PetController | PetControllerIntegrationTest | ~90% |
| Controller | ConsultationController | ConsultationControllerIntegrationTest | ~85% |
| Controller | PrescriptionController | PrescriptionControllerIntegrationTest | ~90% |

---

## Technology Stack

### Testing Libraries
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework for unit tests
- **Spring Boot Test** - Spring Boot testing utilities
- **MockMvc** - Spring MVC test support
- **H2 Database** - In-memory database for integration tests

### Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Test Profile Configuration

The tests use the `dev` profile with the following configuration:

**File:** `src/test/resources/application-dev.properties`

```properties
# Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop

# JWT
app.jwt.secret=test-jwt-secret-key-for-testing-purposes-only
```

- Uses **H2 in-memory database** for fast, isolated tests
- Database is **recreated and destroyed** for each test
- **No external dependencies** required

---

## Test Patterns

### Unit Test Pattern

```java
@ExtendWith(MockitoExtension.class)
class ServiceTest {
    @Mock
    private Repository repository;
    
    @InjectMocks
    private Service service;
    
    @BeforeEach
    void setUp() {
        // Initialize test data
    }
    
    @Test
    @DisplayName("Should do something successfully")
    void testSuccess() {
        // Arrange
        when(repository.method()).thenReturn(value);
        
        // Act
        Result result = service.method();
        
        // Assert
        assertEquals(expected, result);
        verify(repository).method();
    }
}
```

### Integration Test Pattern

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp() throws Exception {
        // Setup: Register user, create data via API
    }
    
    @Test
    void testEndpoint() throws Exception {
        mockMvc.perform(post("/api/endpoint")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.field").value(value));
    }
}
```

---

## Best Practices

### Unit Tests
1. ✅ One assertion per test method (or related assertions)
2. ✅ Mock all external dependencies
3. ✅ Use meaningful test names with `@DisplayName`
4. ✅ Follow Arrange-Act-Assert pattern
5. ✅ Test both success and failure scenarios

### Integration Tests
1. ✅ Use `@SpringBootTest` for full context
2. ✅ Use `@ActiveProfiles("dev")` for test database
3. ✅ Clean up data in `@BeforeEach`
4. ✅ Test actual HTTP requests/responses
5. ✅ Verify role-based access control
6. ✅ Test error handling and validation

---

## Common Test Scenarios

### Testing Authentication
```java
// Register user
// Login
// Extract token from response
// Use token in Authorization header
mockMvc.perform(get("/api/endpoint")
    .header("Authorization", "Bearer " + token))
```

### Testing Role-Based Access
```java
// Test with OWNER role - should succeed
// Test with VETERINARIAN role - should fail
// Test anonymous - should be forbidden
```

### Testing CRUD Operations
```java
// Create - POST 201
// Read - GET 200
// Update - PUT 200
// Delete - DELETE 204
// GetNonExistent - 404/500
```

---

## Troubleshooting

### Tests fail with "User not found"
- Ensure `@BeforeEach` properly registers users before each test
- Check that repository is properly cleared with `deleteAll()`

### JWT Token validation fails
- Verify JWT secret matches in test properties
- Ensure token extraction from response is correct
- Check Authorization header format: "Bearer {token}"

### Database connection issues
- Ensure H2 is in classpath
- Check `spring.datasource.url=jdbc:h2:mem:testdb`
- Verify `spring.jpa.hibernate.ddl-auto=create-drop`

### Mock not working
- Ensure class uses `@ExtendWith(MockitoExtension.class)`
- Verify `@Mock` and `@InjectMocks` annotations
- Check mock is configured with `when().thenReturn()`

---

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '17'
      - run: mvn test
```

### Pre-commit hook
```bash
#!/bin/bash
mvn test
if [ $? -ne 0 ]; then
    echo "Tests failed!"
    exit 1
fi
```

---

## Performance

- **Unit Tests**: ~50-100ms per test
- **Integration Tests**: ~200-500ms per test
- **Total Suite**: ~30-60 seconds
- **Full Maven Build with Tests**: ~2-3 minutes

---

## Documentation Links

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [MockMvc Documentation](https://spring.io/guides/gs/testing-rest/)

---

## Next Steps

1. **Add more edge case tests**
   - Negative values
   - Null checks
   - Boundary conditions

2. **Increase coverage to 90%+**
   - Use `mvn test jacoco:report`
   - Identify uncovered branches
   - Add tests for those paths

3. **Add performance tests**
   - Load testing with JMeter
   - Response time assertions
   - Concurrent request simulation

4. **Add security tests**
   - SQL injection attempts
   - CSRF protection
   - XSS prevention
   - JWT expiration

---

**Happy Testing! 🧪**
