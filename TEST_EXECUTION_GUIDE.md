# 🧪 Vet Connect - API & Firebase Testing Guide

## ✅ Setup Complete

Your Vet Connect application is fully configured with:
- ✓ Production Firebase integration (firebase-credentials.json)  
- ✓ Production properties file (application-prod.properties)
- ✓ Comprehensive API testing tools
- ✓ Demo data templates
- ✓ Complete curl command reference
- ✓ Python test automation script

---

## 🚀 Quick Start - Run Tests Now

### Step 1: Build the Backend (first time only)
```bash
cd backend
mvn clean package -DskipTests -q
echo "✓ Build complete"
```

### Step 2: Start the Backend Server
```bash
# Using Dev profile (H2 in-memory database - best for testing)
java -jar target/vet-connect-1.0.0.jar --spring.profiles.active=dev --server.port=8081 &

# Wait 25-30 seconds for initialization
sleep 30

# Verify server is running
curl -s http://localhost:8081/api/actuator/health | grep -o "UP"
```

### Step 3: Run the Full Test Suite
```bash
# Option A: Python test script (recommended)
python3 api_tester.py --test

# Option B: Bash test script  
chmod +x API_TEST_COMMANDS.sh
./API_TEST_COMMANDS.sh

# Option C: Interactive mode
python3 api_tester.py
```

---

## 📊 What Gets Tested

The comprehensive test suite executes:

### 1️⃣ **USER REGISTRATION**
- Creates demo user: `demo@healthytom.com` / `Demo@1234`
- Returns JWT token for authenticated requests

**Expected Output:**
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

### 2️⃣ **GET CURRENT USER**
- Verifies authentication token works
- Returns user profile data

**Expected Output:**
```json
{
  "id": 1,
  "email": "demo@healthytom.com",
  "firstName": "Demo",
  "lastName": "User",
  "role": "OWNER",
  "phoneNumber": "+1234567890",
  "emailVerified": false
}
```

### 3️⃣ **CREATE PETS**
- Creates: Buddy (Dog - Golden Retriever)
- Creates: Fluffy (Cat - Persian)

**Expected Output:**
```json
{
  "id": 1,
  "name": "Buddy",
  "species": "Dog",
  "breed": "Golden Retriever",
  "age": 3,
  "weight": 30.5,
  "dateOfBirth": "2023-01-15"
}
```

### 4️⃣ **GET PETS FOR OWNER**
- Retrieves all pets created during test
- Returns list with 2 pet objects

### 5️⃣ **CREATE CONSULTATION**
- Creates vet consultation for Buddy
- Verifies appointment scheduling works

**Expected Output:**
```json
{
  "id": 1,
  "petId": 1,
  "petName": "Buddy",
  "veterinarianId": 2,
  "type": "GENERAL_CHECKUP",
  "scheduledDate": "2026-03-02T...",
  "notes": "Routine health check"
}
```

---

## 🔥 Firebase Integration Tests

After API tests pass, verify Firebase integration:

### 1. Check Firebase Configuration
```bash
# Verify credentials loaded correctly
grep "firebase.project-id" backend/src/main/resources/application-prod.properties
# Expected: healthytom-vet-connect

# Verify credentials file exists
ls -l backend/src/main/resources/firebase-credentials.json
```

### 2. Test Firebase Data Export
```bash
# Generate Firebase demo data JSON
python3 api_tester.py --firebase-export

# View generated data
cat firebase_demo_data.json | python3 -m json.tool
```

### 3. Manual Firebase Testing
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select: `healthytom-vet-connect` project
3. Navigate to: **Firestore Database**
4. Create collections matching names in [FIREBASE_DEMO_DATA.md](FIREBASE_DEMO_DATA.md):
   - `prod_users`
   - `prod_pets`
   - `prod_consultations`
   - `prod_prescriptions`

---

## 📋 Manual Testing with cURL

If you prefer testing individual endpoints:

### Register User
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Manual",
    "lastName": "Test",
    "email": "manual@test.com",
    "password": "Test@1234",
    "role": "OWNER",
    "phoneNumber": "+1234567890"
  }'
```

###  Login and Get Token
```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@healthytom.com","password":"Demo@1234"}' \
  | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])")

echo "Token: $TOKEN"
```

### Verify Authentication
```bash
curl -s -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -m json.tool
```

### Create Pet
```bash
curl -s -X POST "http://localhost:8081/api/pets?ownerId=1" \
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

---

## ⚡ Production Testing

### With PostgreSQL Database (Production-like)

1. **Install & Start PostgreSQL**
   ```bash
   # macOS with Homebrew
   brew install postgresql@15
   brew services start postgresql@15
   createdb vet_connect
   ```

2. **Start Backend with Production Profile**
   ```bash
   java -jar target/vet-connect-1.0.0.jar \
     --spring.profiles.active=prod \
     --server.port=8081
   ```

3. **Run Tests**
   ```bash
   python3 api_tester.py --test
   ```

---

## 🐛 Troubleshooting

### Backend Won't Start

**Issue:** `Connection refused on localhost:5432`
- **Solution:** Development uses H2 (in-memory), so just use `--spring.profiles.active=dev`

### Authentication Returns 403

**Issue:** Registration/Login returns 403 Forbidden
- **Solution:** CORS has been configured for localhost. Make sure to use correct port.

### Python Test Won't Connect

**Issue:** `Connection refused` or `timeout`
- **Solution:** Backend takes 20-30 seconds to start. Use `sleep 30` before running tests.

### Token Extraction Fails

**Issue:** `Token not found in response`
- **Solution:** Check JSON response format. Use `curl -s ... | python3 -m json.tool` to debug.

---

## 📚 Complete Documentation Files

| File | Purpose |
|------|---------|
| [API_TESTING_README.md](API_TESTING_README.md) | Complete setup and architecture |
| [API_TESTING_GUIDE.md](API_TESTING_GUIDE.md) | All endpoints with full parameters |
| [CURL_QUICK_REFERENCE.md](CURL_QUICK_REFERENCE.md) | Copy-paste cURL commands |
| [FIREBASE_DEMO_DATA.md](FIREBASE_DEMO_DATA.md) | Firebase structure & demo data |
| [api_tester.py](api_tester.py) | Python test automation script |
| [API_TEST_COMMANDS.sh](API_TEST_COMMANDS.sh) | Bash test automation script |

---

## ✨ Test Data Summary

### Demo User Account
- **Email:** `demo@healthytom.com`
- **Password:** `Demo@1234`
- **Role:** OWNER
- **Phone:** +1234567890

### Demo Pet 1 - Buddy
- **Type:** Dog
- **Breed:** Golden Retriever  
- **Age:** 3 years
- **Weight:** 30.5 kg

### Demo Pet 2 - Fluffy
- **Type:** Cat
- **Breed:** Persian
- **Age:** 5 years
- **Weight:** 4.2 kg
- **Allergies:** Fish

---

## ✅ Success Checklist

After running tests, verify you see:

- ✓ Registration succeeded (user created with JWT token)
- ✓ User profile retrieved (correct email and name)
- ✓ First pet created (Buddy - Dog)
- ✓ Second pet created (Fluffy - Cat)
- ✓ Pets retrieval returned 2 pets
- ✓ Consultation created (appointment scheduled)
- ✓ All responses in valid JSON format
- ✓ No 403/401/500 errors

---

## 🔒 Security Reminders

1. **Protect Firebase Credentials**
   ```bash
   # Add to .gitignore (already done)
   echo "firebase-credentials.json" >> .gitignore
   ```

2. **Never Commit Secrets**
   - firebase-credentials.json
   - application-prod.properties (with real keys)
   - .env files

3. **Production Deployment**
   - Use environment variables for all secrets
   - Enable Firestore security rules (included in FIREBASE_DEMO_DATA.md)
   - Configure CORS for production domain
   - Use HTTPS only

---

## 📞 Next Steps

1. **✓ Run the test suite** - Confirm API is working
2. **✓ Insert Firebase demo data** - Use console or Admin SDK
3. **✓ Review Firestore security rules** - See FIREBASE_DEMO_DATA.md
4. **✓ Update CORS for production** - Set production domain in AuthController
5. **✓ Configure environment variables** - For production deployment
6. **✓ Deploy to Azure** - Use the deployment guides

---

**Last Updated:** March 2, 2026  
**API Version:** 1.0.0  
**Firebase Project:** healthytom-vet-connect  
**Status:** ✅ Ready for Testing
