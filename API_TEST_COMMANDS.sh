#!/bin/bash

# Healthy Tom Vet Connect - API Testing Script
# Comprehensive REST API testing with Firebase integration

BASE_URL="http://localhost:8081/api"
EMAIL="demo@healthytom.com"
PASSWORD="Demo@1234"
TOKEN=""

echo "=========================================="
echo "Healthy Tom Vet Connect - API Test Suite"
echo "=========================================="
echo ""

# Test 1: Register a new user
echo "Test 1: USER REGISTRATION"
echo "============================"
echo "POST $BASE_URL/auth/register"
echo ""

REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Demo",
    "lastName": "User",
    "email": "'$EMAIL'",
    "password": "'$PASSWORD'",
    "role": "OWNER",
    "phoneNumber": "+1234567890"
  }')

echo "Response:"
echo "$REGISTER_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$REGISTER_RESPONSE"
echo ""

# Extract token
TOKEN=$(echo "$REGISTER_RESPONSE" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('token', ''))" 2>/dev/null || echo "")

if [ -z "$TOKEN" ]; then
  echo "Token not found in registration response. Attempting login..."
  echo ""
  echo "Test 2: USER LOGIN"
  echo "=================="
  echo "POST $BASE_URL/auth/login"
  echo ""
  
  LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
      "email": "'$EMAIL'",
      "password": "'$PASSWORD'"
    }')
  
  echo "Response:"
  echo "$LOGIN_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$LOGIN_RESPONSE"
  echo ""
  
  TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('token', ''))" 2>/dev/null || echo "")
fi

if [ ! -z "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
  echo "✓ Token obtained: ${TOKEN:0:20}..."
  echo ""
  
  # Test 3: Get current user
  echo "Test 3: GET CURRENT USER"
  echo "========================"
  echo "GET $BASE_URL/users/me"
  echo "Header: Authorization: Bearer $TOKEN"
  echo ""
  
  curl -s -X GET "$BASE_URL/users/me" \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null || echo "Failed to get user"
  echo ""
  
  # Test 4: Create a pet
  echo "Test 4: CREATE PET"
  echo "=================="
  echo "POST $BASE_URL/pets?ownerId=1"
  echo "Header: Authorization: Bearer $TOKEN"
  echo ""
  
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
  
  echo "Response:"
  echo "$PET_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$PET_RESPONSE"
  echo ""
  
  # Test 5: Get all pets
  echo "Test 5: GET ALL PETS (requires VETERINARIAN role)"
  echo "=================================================="
  echo "GET $BASE_URL/pets"
  echo "Header: Authorization: Bearer $TOKEN"
  echo ""
  
  curl -s -X GET "$BASE_URL/pets" \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null || echo "Access denied or error"
  echo ""
else
  echo "✗ Failed to obtain authentication token"
  echo "Cannot proceed with authenticated tests"
fi

echo ""
echo "=========================================="
echo "API Test Suite Complete"
echo "=========================================="
