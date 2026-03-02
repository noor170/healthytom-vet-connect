# Quick Reference - cURL Commands for Vet Connect API

## Setup

```bash
# Save these as environment variables for easier testing
export BASE_URL="http://localhost:8081/api"
export EMAIL="demo@healthytom.com"
export PASSWORD="Demo@1234"
export TOKEN=""  # Will be populated after login
```

## 1. Authentication

### Register Demo User
```bash
curl -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Demo",
    "lastName": "User",
    "email": "'$EMAIL'",
    "password": "'$PASSWORD'",
    "role": "OWNER",
    "phoneNumber": "+1234567890"
  }' | python3 -m json.tool
```

### Login (Get Token)
```bash
TOKEN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "'$EMAIL'",
    "password": "'$PASSWORD'"
  }' | python3 -c "import sys, json; print(json.load(sys.stdin).get('token', ''))")

echo "Token: $TOKEN"
```

### Refresh Token
```bash
curl -X POST "$BASE_URL/auth/refresh-token" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }' | python3 -m json.tool
```

## 2. User Operations

### Get Current User Profile
```bash
curl -X GET "$BASE_URL/users/me" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

### Get User by ID (Vet/Admin only)
```bash
curl -X GET "$BASE_URL/users/1" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

## 3. Pet Management

### Create Pet
```bash
curl -X POST "$BASE_URL/pets?ownerId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Buddy",
    "species": "Dog",
    "breed": "Golden Retriever",
    "age": 3,
    "weight": 30.5,
    "dateOfBirth": "2023-01-15"
  }' | python3 -m json.tool
```

### Get Pet by ID
```bash
curl -X GET "$BASE_URL/pets/1" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

### Get All Pets for Owner
```bash
curl -X GET "$BASE_URL/pets/owner/1" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

### Get All Pets (Vet/Admin only)
```bash
curl -X GET "$BASE_URL/pets" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

### Update Pet
```bash
curl -X PUT "$BASE_URL/pets/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Buddy Updated",
    "species": "Dog",
    "breed": "Golden Retriever",
    "age": 4,
    "weight": 32.0,
    "dateOfBirth": "2023-01-15"
  }' | python3 -m json.tool
```

### Delete Pet
```bash
curl -X DELETE "$BASE_URL/pets/1" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

## 4. Consultations

### Create Consultation
```bash
curl -X POST "$BASE_URL/consultations" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "petId": 1,
    "veterinarianId": 2,
    "scheduledDate": "2026-03-10T14:00:00",
    "type": "GENERAL_CHECKUP",
    "notes": "Routine health check"
  }' | python3 -m json.tool
```

### Get Consultation by ID
```bash
curl -X GET "$BASE_URL/consultations/1" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

### Get All Consultations
```bash
curl -X GET "$BASE_URL/consultations" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

### Update Consultation
```bash
curl -X PUT "$BASE_URL/consultations/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "status": "COMPLETED",
    "notes": "Routine health check completed successfully",
    "diagnosis": "Pet is healthy"
  }' | python3 -m json.tool
```

### Cancel Consultation
```bash
curl -X DELETE "$BASE_URL/consultations/1" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

## 5. Prescriptions

### Create Prescription
```bash
curl -X POST "$BASE_URL/prescriptions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "petId": 1,
    "veterinarianId": 2,
    "medicationName": "Allergy Medicine",
    "dosage": "5mg",
    "frequency": "Once daily",
    "duration": "30 days",
    "reason": "Allergy management",
    "instructions": "Take with food"
  }' | python3 -m json.tool
```

### Get Prescription by ID
```bash
curl -X GET "$BASE_URL/prescriptions/1" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

### Get Prescriptions for Pet
```bash
curl -X GET "$BASE_URL/prescriptions/pet/1" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

### Update Prescription
```bash
curl -X PUT "$BASE_URL/prescriptions/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "dosesRemaining": 25,
    "refillsRemaining": 1,
    "status": "ACTIVE"
  }' | python3 -m json.tool
```

## 6. Admin Operations

### Get Admin Stats
```bash
curl -X GET "$BASE_URL/admin/stats" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

### Get User Logs
```bash
curl -X GET "$BASE_URL/admin/logs" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

### Get All Users (Admin only)
```bash
curl -X GET "$BASE_URL/admin/users" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

## Testing Batch Operations

### Complete User Registration and Pet Creation Flow

```bash
#!/bin/bash

# 1. Register
echo "1. Registering user..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Demo",
    "lastName": "User",
    "email": "demo@healthytom.com",
    "password": "Demo@1234",
    "role": "OWNER",
    "phoneNumber": "+1234567890"
  }')

TOKEN=$(echo "$REGISTER_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('token', ''))" 2>/dev/null)
echo "✓ Token: ${TOKEN:0:20}..."

# 2. Get User
echo "2. Getting user profile..."
curl -s -X GET "$BASE_URL/users/me" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

# 3. Create Pet
echo "3. Creating pet..."
PET_RESPONSE=$(curl -s -X POST "$BASE_URL/pets?ownerId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Buddy",
    "species": "Dog",
    "breed": "Golden Retriever",
    "age": 3,
    "weight": 30.5,
    "dateOfBirth": "2023-01-15"
  }')

PET_ID=$(echo "$PET_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null)
echo "✓ Pet created with ID: $PET_ID"

# 4. Get Pet
echo "4. Getting pet profile..."
curl -s -X GET "$BASE_URL/pets/$PET_ID" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

## Response Examples

### Successful Registration (201 Created)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkRlbW8gVXNlciIsImlhdCI6MTUxNjIzOTAyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "demo@healthytom.com",
  "firstName": "Demo",
  "lastName": "User",
  "role": "OWNER"
}
```

### Error Response (400 Bad Request)
```json
{
  "error": "Email already exists",
  "status": 400,
  "timestamp": "2026-03-02T15:57:35Z"
}
```

### Unauthorized (401)
```json
{
  "error": "Invalid or missing token",
  "status": 401,
  "timestamp": "2026-03-02T15:57:35Z"
}
```

## Debugging Tips

### Check Response Headers
```bash
curl -i -X GET "$BASE_URL/users/me" \
  -H "Authorization: Bearer $TOKEN"
```

### View Raw Response (no pretty print)
```bash
curl -s -X GET "$BASE_URL/users/me" \
  -H "Authorization: Bearer $TOKEN"
```

### Save Response to File
```bash
curl -X GET "$BASE_URL/users/me" \
  -H "Authorization: Bearer $TOKEN" > response.json
```

### Test with Verbose Output
```bash
curl -v -X GET "$BASE_URL/users/me" \
  -H "Authorization: Bearer $TOKEN"
```

## Performance Testing

### Load Test (10 requests)
```bash
for i in {1..10}; do
  curl -s -X GET "$BASE_URL/pets/owner/1" \
    -H "Authorization: Bearer $TOKEN" > /dev/null
done
echo "✓ 10 requests completed"
```

### Concurrent Requests
```bash
for i in {1..5}; do
  (curl -s -X GET "$BASE_URL/pets" \
    -H "Authorization: Bearer $TOKEN" > /dev/null &)
done
wait
echo "✓ 5 concurrent requests completed"
```
