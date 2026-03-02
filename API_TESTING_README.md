# Vet Connect - API Testing & Firebase Integration Guide

This directory contains comprehensive tools and documentation for testing the Vet Connect REST API and integrating with Firebase.

## 📁 Files Overview

### Documentation Files

- **[API_TESTING_GUIDE.md](API_TESTING_GUIDE.md)** - Complete API endpoint reference with all parameters
- **[CURL_QUICK_REFERENCE.md](CURL_QUICK_REFERENCE.md)** - Ready-to-use cURL command examples
- **[FIREBASE_DEMO_DATA.md](FIREBASE_DEMO_DATA.md)** - Firebase Firestore demo data structure and insertion guides
- **[application-prod.properties](backend/src/main/resources/application-prod.properties)** - Production configuration with Firebase settings
- **[firebase-credentials.json](backend/src/main/resources/firebase-credentials.json)** - Firebase service account credentials

### Testing Scripts

- **[API_TEST_COMMANDS.sh](API_TEST_COMMANDS.sh)** - Bash script for automated REST API testing
- **[api_tester.py](api_tester.py)** - Python script for comprehensive API testing and Firebase data export

## 🚀 Quick Start

### Prerequisites
- Java 17+ (for backend)
- Python 3.7+ (for Python testing script)
- cURL (for manual API testing)
- Maven (for building backend)

### 1. Start the Backend Server

```bash
# Navigate to backend directory
cd backend

# Build the project
mvn clean package -DskipTests

# Run with test profile (using H2 in-memory database)
java -jar target/*.jar --spring.profiles.active=test --server.port=8081

# Or run with production profile (requires PostgreSQL)
java -jar target/*.jar --spring.profiles.active=prod --server.port=8081
```

### 2. Test the API - Option A: Using Python Script

```bash
# Run full test suite
python3 api_tester.py --test

# Login only
python3 api_tester.py --login-only

# Generate Firebase demo data
python3 api_tester.py --firebase-export

# Interactive mode
python3 api_tester.py
```

### 3. Test the API - Option B: Using cURL Commands

```bash
# Make the script executable
chmod +x API_TEST_COMMANDS.sh

# Run automated tests
./API_TEST_COMMANDS.sh

# Or use cURL directly (examples in CURL_QUICK_REFERENCE.md)
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

### 4. Insert Firebase Demo Data

#### Via Firebase Console
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: `healthytom-vet-connect`
3. Open Firestore Database
4. Manually create collections and documents using the structure in [FIREBASE_DEMO_DATA.md](FIREBASE_DEMO_DATA.md)

#### Via Python Script
```bash
python3 api_tester.py --firebase-export
```

#### Via Firebase Admin SDK
```bash
npm install -g firebase-tools
firebase init emulators
firebase emulators:start
```

## 📚 API Endpoints

### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login user
- `POST /auth/refresh-token` - Refresh authentication token

### Users
- `GET /users/me` - Get current user profile
- `GET /users/{id}` - Get user by ID (Vet/Admin only)

### Pets
- `POST /pets?ownerId={id}` - Create pet
- `GET /pets/{id}` - Get pet by ID
- `GET /pets/owner/{ownerId}` - Get pets by owner
- `GET /pets` - Get all pets (Vet/Admin only)
- `PUT /pets/{id}` - Update pet
- `DELETE /pets/{id}` - Delete pet

### Consultations
- `POST /consultations` - Create consultation
- `GET /consultations` - Get all consultations
- `GET /consultations/{id}` - Get consultation by ID
- `PUT /consultations/{id}` - Update consultation
- `DELETE /consultations/{id}` - Cancel consultation

### Prescriptions
- `POST /prescriptions` - Create prescription
- `GET /prescriptions` - Get all prescriptions
- `GET /prescriptions/{id}` - Get prescription by ID
- `GET /prescriptions/pet/{petId}` - Get prescriptions for pet
- `PUT /prescriptions/{id}` - Update prescription

## 🔐 Demo Credentials

### Pet Owner
- Email: `demo@healthytom.com`
- Password: `Demo@1234`
- Role: `OWNER`

### Veterinarian
- Email: `vet@healthytom.com`
- Password: `Vet@1234`
- Role: `VETERINARIAN`

## 🐾 Demo Data

### Pet 1: Buddy (Dog)
- Breed: Golden Retriever
- Age: 3 years
- Weight: 30.5 kg
- Status: Vaccinated

### Pet 2: Fluffy (Cat)
- Breed: Persian
- Age: 5 years
- Weight: 4.2 kg
- Allergies: Fish
- Status: Vaccinated

## 🔥 Firebase Configuration

### Service Account
- **Project ID**: `healthytom-vet-connect`
- **Credentials File**: `firebase-credentials.json`
- **Messaging Sender ID**: `100368986720217027425`

### Firestore Collections
- `prod_users` - User profiles
- `prod_pets` - Pet information
- `prod_consultations` - Consultation records
- `prod_prescriptions` - Prescription information

### Firebase Storage
- **Bucket**: `healthytom-vet-connect.appspot.com`
- **Path Prefix**: `prod/`
- **Max File Size**: 50MB

### Environment Variables (Production)
```bash
FIREBASE_CREDENTIALS_PATH=classpath:firebase-credentials.json
FIREBASE_PROJECT_ID=healthytom-vet-connect
FIREBASE_DATABASE_URL=https://healthytom-vet-connect.firebaseio.com
FIREBASE_STORAGE_BUCKET=healthytom-vet-connect.appspot.com
FIREBASE_API_KEY=YOUR_API_KEY
FIREBASE_AUTH_DOMAIN=healthytom-vet-connect.firebaseapp.com
FIREBASE_MESSAGING_SENDER_ID=100368986720217027425
FIREBASE_APP_ID=YOUR_APP_ID
```

## 🧪 Example Testing Workflow

### 1. Register a New User
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "password": "Test@1234",
    "role": "OWNER",
    "phoneNumber": "+9876543210"
  }'
```

**Response** (with JWT token):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "...",
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User",
  "role": "OWNER"
}
```

### 2. Get Current User Profile
```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer PASTE_TOKEN_HERE"
```

### 3. Create a Pet
```bash
curl -X POST "http://localhost:8081/api/pets?ownerId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer PASTE_TOKEN_HERE" \
  -d '{
    "name": "Max",
    "species": "Dog",
    "breed": "Labrador",
    "age": 2,
    "weight": 25.5,
    "dateOfBirth": "2024-01-15"
  }'
```

### 4. Retrieve Pet Information
```bash
curl -X GET http://localhost:8081/api/pets/owner/1 \
  -H "Authorization: Bearer PASTE_TOKEN_HERE"
```

## 📊 Testing Performance

### Load Testing
```bash
# Test with 100 requests
for i in {1..100}; do
  curl -s http://localhost:8081/api/pets/owner/1 \
    -H "Authorization: Bearer TOKEN" > /dev/null &
done
```

### Response Time Testing
```bash
time curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer TOKEN"
```

## 🐛 Troubleshooting

### Backend won't start
- Check port 8081 is available: `lsof -i :8081`
- Verify Java is installed: `java -version`
- Check Maven configuration: `mvn -v`

### Authentication failures
- Verify email hasn't been registered yet
- Check password meets security requirements (min 8 chars, special chars)
- Ensure JWT secret is configured in application-dev.properties

### API returns 401 Unauthorized
- Token may have expired - use refresh-token endpoint
- Check "Authorization: Bearer TOKEN" header format
- Verify token is correct

### Firebase connection issues
- Verify `firebase-credentials.json` exists in correct path
- Check project ID matches in properties file
- Ensure service account has proper IAM roles
- Verify internet connection to Firebase

### CORS errors
- Check CORS is enabled in AuthController
- Verify frontend is making requests from allowed origin
- Update CORS settings if needed in controller

## 📖 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)
- [REST API Best Practices](https://restfulapi.net/)
- [JWT Authentication](https://jwt.io/)
- [Firestore Documentation](https://firebase.google.com/docs/firestore)

## 🔧 Maintenance

### Clear Test Data
```bash
# Truncate H2 database (if using test profile)
# Database is in-memory, so restart clears it

# Or manually delete documents from Firestore
# In Firebase Console: Select documents and delete
```

### Monitor API Usage
```bash
# Check backend logs
tail -f /tmp/backend.log

# Monitor requests
lsof -i :8081
```

### Update Dependencies
```bash
cd backend
mvn clean install -U
```

## 🤝 Contributing

When adding new API endpoints:
1. Update controllers in `src/main/java/com/healthytom/controller/`
2. Add DTOs in `src/main/java/com/healthytom/dto/`
3. Implement services in `src/main/java/com/healthytom/service/`
4. Test with curl or Python script
5. Update documentation files
6. Add Firebase integration if needed

## 📝 License

This project is part of Healthy Tom Vet Connect application.

---

**Last Updated**: March 2, 2026
**API Version**: 1.0.0
**Firebase Project**: healthytom-vet-connect
