# Healthy Tom Vet Connect - Backend API

A comprehensive Spring Boot REST API for a veterinary consultation platform with JWT-based authentication and role-based access control.

## Technologies Used

- **Java 17**
- **Spring Boot 3.2.3**
- **Spring Security with JWT**
- **Spring Data JPA with Hibernate**
- **PostgreSQL** (Production) / **H2** (Development)
- **Maven**
- **Lombok**
- **jjwt 0.12.3** (JSON Web Tokens)

## Project Structure

```
backend/
├── src/main/java/com/healthytom/
│   ├── VetConnectApplication.java      # Main application class
│   ├── controller/                     # REST API Controllers
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── PetController.java
│   │   ├── ConsultationController.java
│   │   └── PrescriptionController.java
│   ├── service/                        # Business Logic
│   │   ├── AuthService.java
│   │   ├── PetService.java
│   │   ├── ConsultationService.java
│   │   └── PrescriptionService.java
│   ├── entity/                         # JPA Entities
│   │   ├── User.java
│   │   ├── Pet.java
│   │   ├── Consultation.java
│   │   └── Prescription.java
│   ├── repository/                     # Data Access Layer
│   ├── dto/                           # Data Transfer Objects
│   ├── security/                      # JWT & Security Configuration
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── UserDetailsServiceImpl.java
│   ├── config/                        # Application Configuration
│   │   └── SecurityConfig.java
│   └── exception/                     # Exception Handling
├── pom.xml                            # Maven Configuration
└── src/main/resources/
    └── application.properties          # Application Properties
```

## Prerequisites

- Java 17 or higher
- Maven 3.6.3 or higher
- PostgreSQL 12 or higher (optional, H2 can be used for development)

## Setup and Installation

### 1. Clone the Repository

```bash
cd backend
```

### 2. Configure Database

Update `src/main/resources/application.properties`:

```properties
# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/vet_connect
spring.datasource.username=postgres
spring.datasource.password=your_password

# Or use H2 in-memory database for development
spring.datasource.url=jdbc:h2:mem:testdb
```

### 3. Generate JWT Secret Key

The default JWT secret in `application.properties` is a placeholder. For production, generate a secure key:

```bash
# Using OpenSSL
openssl rand -base64 64
```

Update `app.jwt.secret` in `application.properties` with your generated key.

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080/api`

## API Endpoints

### Authentication Endpoints

- **POST** `/api/auth/register` - Register a new user
- **POST** `/api/auth/login` - Login and get access token
- **POST** `/api/auth/refresh-token` - Refresh access token

### User Endpoints

- **GET** `/api/users/me` - Get current authenticated user
- **GET** `/api/users/{id}` - Get user by ID

### Pet Endpoints

- **POST** `/api/pets` - Create a new pet
- **GET** `/api/pets/{id}` - Get pet by ID
- **GET** `/api/pets/owner/{ownerId}` - Get pets by owner
- **GET** `/api/pets` - Get all pets (Admin/Veterinarian only)
- **PUT** `/api/pets/{id}` - Update pet
- **DELETE** `/api/pets/{id}` - Delete pet

### Consultation Endpoints

- **POST** `/api/consultations` - Create a new consultation
- **GET** `/api/consultations/{id}` - Get consultation by ID
- **GET** `/api/consultations/owner/{ownerId}` - Get consultations by owner
- **GET** `/api/consultations/veterinarian/{veterinarianId}` - Get consultations by veterinarian
- **GET** `/api/consultations/pet/{petId}` - Get consultations by pet
- **GET** `/api/consultations` - Get all consultations (Admin only)
- **PUT** `/api/consultations/{id}` - Update consultation
- **DELETE** `/api/consultations/{id}` - Delete consultation (Admin only)
- **PUT** `/api/consultations/{id}/assign-veterinarian/{veterinarianId}` - Assign veterinarian
- **PUT** `/api/consultations/{id}/rate` - Rate consultation

### Prescription Endpoints

- **POST** `/api/prescriptions` - Create a new prescription
- **GET** `/api/prescriptions/{id}` - Get prescription by ID
- **GET** `/api/prescriptions/consultation/{consultationId}` - Get prescriptions by consultation
- **GET** `/api/prescriptions/pet/{petId}` - Get prescriptions by pet
- **GET** `/api/prescriptions/veterinarian/{veterinarianId}` - Get prescriptions by veterinarian
- **GET** `/api/prescriptions` - Get all prescriptions (Admin only)
- **PUT** `/api/prescriptions/{id}` - Update prescription
- **DELETE** `/api/prescriptions/{id}` - Delete prescription

## Authentication

The API uses JWT (JSON Web Tokens) for authentication. Include the token in the `Authorization` header:

```
Authorization: Bearer <your_access_token>
```

### User Roles

- **OWNER** - Pet owner who can create consultations and manage their pets
- **VETERINARIAN** - Licensed veterinarian who can diagnose and prescribe
- **ADMIN** - System administrator with full access

## Request Examples

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "owner@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "1234567890",
    "role": "OWNER"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "owner@example.com",
    "password": "password123"
  }'
```

### Create Pet

```bash
curl -X POST http://localhost:8080/api/pets?ownerId=1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Buddy",
    "species": "Dog",
    "breed": "Golden Retriever",
    "color": "Golden",
    "weight": 30.5,
    "dateOfBirth": "2020-01-15"
  }'
```

## Database Schema

The application automatically creates and manages database tables using Hibernate with `ddl-auto=update`.

### Main Tables

- **users** - User accounts with roles
- **pets** - Pet information
- **consultations** - Veterinary consultations
- **prescriptions** - Medication prescriptions

## Configuration Properties

Key properties in `application.properties`:

```properties
# Server
server.port=8080
server.servlet.context-path=/api

# JWT
app.jwt.secret=<your-secret-key>
app.jwt.expiration=86400000              # 24 hours
app.jwt.refresh.expiration=604800000     # 7 days

# Database
spring.jpa.hibernate.ddl-auto=update     # Auto-update schema
```

## Security Features

- ✅ JWT-based stateless authentication
- ✅ Role-based access control (RBAC)
- ✅ Password encryption using BCrypt
- ✅ CORS support for frontend integration
- ✅ Method-level security annotations
- ✅ Comprehensive exception handling

## Development

### Running Tests

```bash
mvn test
```

### Building for Production

```bash
mvn clean package -DskipTests
java -jar target/vet-connect-1.0.0.jar
```

## Environment Variables (Optional)

Create a `.env` file for sensitive configurations:

```env
DB_URL=jdbc:postgresql://localhost:5432/vet_connect
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_super_secret_key
```

## Troubleshooting

### Database Connection Issues

- Ensure PostgreSQL is running
- Verify credentials in `application.properties`
- Check if database exists or create with: `CREATE DATABASE vet_connect;`

### JWT Token Issues

- Ensure token is included in `Authorization: Bearer` format
- Check token expiration time
- Regenerate using `/api/auth/refresh-token`

### CORS Issues

- Verify frontend URL is in `SecurityConfig.corsConfigurationSource()`
- Default: `http://localhost:5173`

## API Documentation

The API follows RESTful conventions with:
- Standard HTTP methods (GET, POST, PUT, DELETE)
- Appropriate HTTP status codes
- JSON request/response bodies
- Consistent error response format

## License

© 2026 Healthy Tom Vet Connect. All rights reserved.

## Support

For issues or questions, please contact the development team.
