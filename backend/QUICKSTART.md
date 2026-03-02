# Quick Start Guide - Vet Connect Backend

## Option 1: Local Development (Recommended)

### Prerequisites
- Java 17+
- Maven 3.6.3+

### Steps

1. **Navigate to backend directory**
   ```bash
   cd backend
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
   ```

4. **Access the API**
   - API Base URL: `http://localhost:8080/api`
   - H2 Console: `http://localhost:8080/api/h2-console`

### Sample Test

```bash
# Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@123",
    "firstName": "John",
    "lastName": "Doe",
    "role": "OWNER"
  }'

# Response will include: accessToken, refreshToken, user details
```

---

## Option 2: Docker Compose (Production-like)

### Prerequisites
- Docker
- Docker Compose

### Steps

1. **Navigate to backend directory**
   ```bash
   cd backend
   ```

2. **Start services**
   ```bash
   docker-compose up -d
   ```

3. **Access the API**
   - API Base URL: `http://localhost:8080/api`
   - PostgreSQL: `localhost:5432`

4. **View logs**
   ```bash
   docker-compose logs -f api
   ```

5. **Stop services**
   ```bash
   docker-compose down
   ```

---

## Option 3: Maven & Docker

### Build Docker Image Manually

```bash
# Build the image
docker build -t vet-connect-api:latest .

# Run the container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/vet_connect \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  vet-connect-api:latest
```

---

## Default Credentials (Development Only)

### H2 Database Console
- URL: `http://localhost:8080/api/h2-console`
- Driver: `org.h2.Driver`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

---

## Environment Variables

### For Docker/Production

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/vet_connect
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SERVER_PORT=8080
APP_JWT_SECRET=your-secret-key-here
```

### For Development

```bash
# Use dev profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

---

## API Testing

### Using Postman

1. Import collection from `postman_collection.json` (if available)
2. Set environment variables:
   - `base_url`: `http://localhost:8080/api`
   - `token`: (obtained from login response)

### Using cURL

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@123"
  }'

# Use token in requests
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <your_token_here>"
```

---

## Troubleshooting

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Maven Build Issues
```bash
# Clear Maven cache
mvn clean
rm -rf ~/.m2/repository

# Rebuild
mvn install
```

### Docker Issues
```bash
# Remove dangling containers
docker container prune
docker image prune
docker volume prune

# Rebuild
docker-compose down -v
docker-compose up --build
```

---

## Next Steps

1. **Connect Frontend**: Update frontend API base URL to `http://localhost:8080/api`
2. **Configure JWT Secret**: Update `app.jwt.secret` for production
3. **Set up PostgreSQL**: Switch from H2 to PostgreSQL for production
4. **Deploy**: Use Docker or cloud platform (AWS, Azure, etc.)

---

## Additional Resources

- API Documentation: See `README.md`
- Database Schema: Automatically created by Hibernate
- Logs: Check `logs/` directory or console output

---

**Happy Coding! 🐾**
