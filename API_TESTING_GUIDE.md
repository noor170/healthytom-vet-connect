# API Testing Guide - Vet Connect with Firebase Integration

## Prerequisites
- Spring Boot backend running on port 8081
- Firebase credentials configured in `application-prod.properties`
- curl installed
- python3 for JSON formatting (optional)

## Quick Start

### 1. Start the Backend Server
```bash
cd backend
java -jar target/*.jar --spring.profiles.active=test --server.port=8081
```

### 2. Run the API Test Suite
```bash
chmod +x API_TEST_COMMANDS.sh
./API_TEST_COMMANDS.sh
```

## API Endpoints

### Authentication

#### Register a New User
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Demo",
    "lastName": "User",
    "email": "demo@healthytom.com",
    "password": "Demo@1234",
    "role": "OWNER",
    "phoneNumber": "+1234567890"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "...",
  "email": "demo@healthytom.com",
  "firstName": "Demo",
  "lastName": "User",
  "role": "OWNER"
}
```

#### Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@healthytom.com",
    "password": "Demo@1234"
  }'
```

#### Refresh Token
```bash
curl -X POST http://localhost:8081/api/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "your_refresh_token_here"
  }'
```

### User Management

#### Get Current User
```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Get User by ID (Veterinarian/Admin only)
```bash
curl -X GET http://localhost:8081/api/users/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Pet Management

#### Create Pet
```bash
curl -X POST http://localhost:8081/api/pets?ownerId=1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Buddy",
    "species": "Dog",
    "breed": "Golden Retriever",
    "age": 3,
    "weight": 30.5,
    "dateOfBirth": "2023-01-15"
  }'
```

#### Get Pet by ID
```bash
curl -X GET http://localhost:8081/api/pets/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Get Pets by Owner ID
```bash
curl -X GET http://localhost:8081/api/pets/owner/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Get All Pets (Veterinarian/Admin only)
```bash
curl -X GET http://localhost:8081/api/pets \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Update Pet
```bash
curl -X PUT http://localhost:8081/api/pets/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Buddy Updated",
    "species": "Dog",
    "breed": "Golden Retriever",
    "age": 4,
    "weight": 32.0,
    "dateOfBirth": "2023-01-15"
  }'
```

#### Delete Pet
```bash
curl -X DELETE http://localhost:8081/api/pets/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Consultations

#### Create Consultation
```bash
curl -X POST http://localhost:8081/api/consultations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "petId": 1,
    "veterinarianId": 2,
    "scheduledDate": "2026-03-10T14:00:00",
    "type": "GENERAL_CHECKUP",
    "notes": "Routine health check"
  }'
```

#### Get Consultations
```bash
curl -X GET http://localhost:8081/api/consultations \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Demo Data

### Demo User 1 - Pet Owner
- Email: `demo@healthytom.com`
- Password: `Demo@1234`
- Role: `OWNER`
- Phone: `+1234567890`

### Demo Pet
- Name: `Buddy`
- Species: `Dog`
- Breed: `Golden Retriever`
- Age: `3`
- Weight: `30.5 kg`

## Firebase Integration

### Firestore Database Structure
```
prod_users/
  - userId
    - email
    - firstName
    - lastName
    - role
    - phoneNumber

prod_pets/
  - petId
    - name
    - species
    - breed
    - age
    - weight
    - ownerId

prod_consultations/
  - consultationId
    - petId
    - veterinarianId
    - scheduledDate
    - type
    - notes
```

### Firebase Storage
- Path Prefix: `prod/`
- Max File Size: 50MB
- Use Cases:
  - Pet photos
  - Medical reports
  - Prescription documents
  - Prescription images

## Testing Best Practices

1. **Always extract the token** from the first successful authentication request
2. **Use the token** in the `Authorization: Bearer TOKEN` header for subsequent requests
3. **Check role-based endpoints** - Some endpoints require VETERINARIAN or ADMIN roles
4. **Test with valid data** - Ensure JSON format is correct before sending
5. **Monitor response headers** - Check for CORS, authentication, and other security headers

## Troubleshooting

### Connection Refused
- Ensure backend is running on port 8081
- Check if port is already in use: `lsof -i :8081`

### Authentication Failed
- Verify email hasn't been registered yet
- Check password meets requirements
- Ensure JWT secret is properly configured

### Firebase Connection Issues
- Verify `firebase-credentials.json` path is correct
- Check Firebase project ID matches
- Ensure service account has proper permissions

## Environment Variables (Production)

```bash
FIREBASE_CREDENTIALS_PATH=classpath:firebase-credentials.json
FIREBASE_PROJECT_ID=healthytom-vet-connect
FIREBASE_DATABASE_URL=${FIREBASE_DATABASE_URL}
FIREBASE_STORAGE_BUCKET=${FIREBASE_STORAGE_BUCKET}
FIREBASE_API_KEY=${FIREBASE_API_KEY}
FIREBASE_AUTH_DOMAIN=${FIREBASE_AUTH_DOMAIN}
FIREBASE_MESSAGING_SENDER_ID=100368986720217027425
FIREBASE_APP_ID=${FIREBASE_APP_ID}
```
