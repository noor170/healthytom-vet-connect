# VetConnect API - Postman Manual

A comprehensive guide for testing the VetConnect API using Postman. This manual covers all available endpoints with detailed request and response examples.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Authentication Endpoints](#authentication-endpoints)
3. [User Endpoints](#user-endpoints)
4. [Pet Endpoints](#pet-endpoints)
5. [Consultation Endpoints](#consultation-endpoints)
6. [Prescription Endpoints](#prescription-endpoints)
7. [Postman Collection Setup](#postman-collection-setup)

---

## Getting Started

### Base URL

```
Development: http://localhost:8081/api
Production: http://localhost:8080/api
```

### Authentication

Most endpoints require JWT authentication. After login, you'll receive:
- `accessToken` - Used in the Authorization header (expires in 24 hours)
- `refreshToken` - Used to get a new access token (expires in 7 days)

### Headers

All requests (except auth endpoints) require:
```
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

### User Roles

| Role | Description |
|------|-------------|
| `OWNER` | Pet owner - can manage pets and consultations |
| `VETERINARIAN` | Veterinarian - can manage consultations and prescriptions |
| `ADMIN` | Administrator - full access to all resources |

---

## Authentication Endpoints

### 1. Register Owner

Create a new pet owner account.

**Endpoint:** `POST /api/auth/register`

**Request:**
```json
{
  "email": "owner@example.com",
  "password": "Password@123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "role": "OWNER"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "owner@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "profileImageUrl": null,
    "role": "OWNER",
    "emailVerified": false,
    "specialization": null,
    "licenseNumber": null
  }
}
```

**Validation Rules:**
- `email` - Required, valid email format
- `password` - Required, minimum 6 characters
- `firstName` - Required
- `lastName` - Required
- `phoneNumber` - Optional, valid phone format
- `role` - Required, must be "OWNER" or "VETERINARIAN"

---

### 2. Register Veterinarian

Create a new veterinarian account with specialization.

**Endpoint:** `POST /api/auth/register`

**Request:**
```json
{
  "email": "vet@example.com",
  "password": "Password@123",
  "firstName": "Dr. Sarah",
  "lastName": "Smith",
  "phoneNumber": "+0987654321",
  "role": "VETERINARIAN",
  "specialization": "Small Animals",
  "licenseNumber": "VET123456"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 2,
    "email": "vet@example.com",
    "firstName": "Dr. Sarah",
    "lastName": "Smith",
    "phoneNumber": "+0987654321",
    "profileImageUrl": null,
    "role": "VETERINARIAN",
    "emailVerified": false,
    "specialization": "Small Animals",
    "licenseNumber": "VET123456"
  }
}
```

**Additional Fields (Veterinarian):**
- `specialization` - Optional, veterinarian's area of expertise
- `licenseNumber` - Optional, veterinary license number

---

### 3. Login

Authenticate an existing user.

**Endpoint:** `POST /api/auth/login`

**Request:**
```json
{
  "email": "owner@example.com",
  "password": "Password@123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "owner@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "profileImageUrl": null,
    "role": "OWNER",
    "emailVerified": false,
    "specialization": null,
    "licenseNumber": null
  }
}
```

**Error Response (401 Unauthorized):**
```json
{
  "timestamp": "2026-03-02T10:30:00",
  "message": "Invalid email or password",
  "details": "uri=/api/auth/login"
}
```

---

### 4. Refresh Token

Get a new access token using the refresh token.

**Endpoint:** `POST /api/auth/refresh-token`

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "owner@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "profileImageUrl": null,
    "role": "OWNER",
    "emailVerified": false,
    "specialization": null,
    "licenseNumber": null
  }
}
```

---

## User Endpoints

### 5. Get Current User

Get the currently authenticated user's profile.

**Endpoint:** `GET /api/users/me`

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "email": "owner@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "profileImageUrl": null,
  "role": "OWNER",
  "emailVerified": false,
  "specialization": null,
  "licenseNumber": null
}
```

**Required Role:** Any authenticated user

---

### 6. Get User by ID

Get a user by their ID. Only veterinarians and admins can access.

**Endpoint:** `GET /api/users/{id}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | User ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "email": "owner@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "profileImageUrl": null,
  "role": "OWNER",
  "emailVerified": false,
  "specialization": null,
  "licenseNumber": null
}
```

**Required Role:** VETERINARIAN or ADMIN

**Error Response (404 Not Found):**
```json
{
  "timestamp": "2026-03-02T10:30:00",
  "message": "User not found with id: 999",
  "details": "uri=/api/users/999"
}
```

---

## Pet Endpoints

### 7. Create Pet

Add a new pet to an owner's account.

**Endpoint:** `POST /api/pets?ownerId={ownerId}`

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| ownerId | Long | ID of the pet owner |

**Headers:**
```
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

**Request:**
```json
{
  "name": "Buddy",
  "species": "Dog",
  "breed": "Golden Retriever",
  "color": "Golden",
  "weight": 30.5,
  "dateOfBirth": "2020-01-15",
  "microchipNumber": "123456789",
  "medicalHistory": "Vaccinated, no known allergies",
  "vaccinations": "DHPP, Rabies"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "Buddy",
  "species": "Dog",
  "breed": "Golden Retriever",
  "color": "Golden",
  "weight": 30.5,
  "dateOfBirth": "2020-01-15",
  "microchipNumber": "123456789",
  "medicalRecordNumber": null,
  "medicalHistory": "Vaccinated, no known allergies",
  "vaccinations": "DHPP, Rabies",
  "photoUrl": null,
  "ownerId": 1
}
```

**Required Role:** OWNER or VETERINARIAN

---

### 8. Get Pet by ID

Get details of a specific pet.

**Endpoint:** `GET /api/pets/{id}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Pet ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Buddy",
  "species": "Dog",
  "breed": "Golden Retriever",
  "color": "Golden",
  "weight": 30.5,
  "dateOfBirth": "2020-01-15",
  "microchipNumber": "123456789",
  "medicalRecordNumber": null,
  "medicalHistory": "Vaccinated, no known allergies",
  "vaccinations": "DHPP, Rabies",
  "photoUrl": null,
  "ownerId": 1
}
```

**Required Role:** OWNER or VETERINARIAN

---

### 9. Get Pets by Owner

Get all pets belonging to a specific owner.

**Endpoint:** `GET /api/pets/owner/{ownerId}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| ownerId | Long | Owner ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Buddy",
    "species": "Dog",
    "breed": "Golden Retriever",
    "color": "Golden",
    "weight": 30.5,
    "dateOfBirth": "2020-01-15",
    "microchipNumber": "123456789",
    "medicalRecordNumber": null,
    "medicalHistory": "Vaccinated, no known allergies",
    "vaccinations": "DHPP, Rabies",
    "photoUrl": null,
    "ownerId": 1
  },
  {
    "id": 2,
    "name": "Whiskers",
    "species": "Cat",
    "breed": "Persian",
    "color": "White",
    "weight": 4.5,
    "dateOfBirth": "2019-06-20",
    "microchipNumber": "987654321",
    "medicalRecordNumber": null,
    "medicalHistory": "Healthy, spayed",
    "vaccinations": "FVRCP, Rabies",
    "photoUrl": null,
    "ownerId": 1
  }
]
```

**Required Role:** OWNER or VETERINARIAN

---

### 10. Get All Pets

Get all pets in the system. Admin and veterinarian only.

**Endpoint:** `GET /api/pets`

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Buddy",
    "species": "Dog",
    "breed": "Golden Retriever",
    "color": "Golden",
    "weight": 30.5,
    "dateOfBirth": "2020-01-15",
    "microchipNumber": "123456789",
    "medicalRecordNumber": null,
    "medicalHistory": "Vaccinated, no known allergies",
    "vaccinations": "DHPP, Rabies",
    "photoUrl": null,
    "ownerId": 1
  }
]
```

**Required Role:** VETERINARIAN or ADMIN

---

### 11. Update Pet

Update an existing pet's information.

**Endpoint:** `PUT /api/pets/{id}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Pet ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

**Request:**
```json
{
  "name": "Buddy",
  "species": "Dog",
  "breed": "Golden Retriever",
  "color": "Golden",
  "weight": 31.0,
  "dateOfBirth": "2020-01-15",
  "microchipNumber": "123456789",
  "medicalHistory": "Vaccinated, no known allergies, recent checkup",
  "vaccinations": "DHPP, Rabies"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Buddy",
  "species": "Dog",
  "breed": "Golden Retriever",
  "color": "Golden",
  "weight": 31.0,
  "dateOfBirth": "2020-01-15",
  "microchipNumber": "123456789",
  "medicalRecordNumber": null,
  "medicalHistory": "Vaccinated, no known allergies, recent checkup",
  "vaccinations": "DHPP, Rabies",
  "photoUrl": null,
  "ownerId": 1
}
```

**Required Role:** OWNER or VETERINARIAN

---

### 12. Delete Pet

Delete a pet from the system.

**Endpoint:** `DELETE /api/pets/{id}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Pet ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (204 No Content):** Empty response

**Required Role:** OWNER or ADMIN

---

## Consultation Endpoints

### 13. Create Consultation

Create a new consultation request.

**Endpoint:** `POST /api/consultations?ownerId={ownerId}`

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| ownerId | Long | ID of the pet owner |

**Headers:**
```
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

**Request:**
```json
{
  "petId": 1,
  "title": "Annual Checkup",
  "description": "Regular health examination",
  "symptoms": "No specific symptoms",
  "status": "PENDING",
  "consultationDate": "2026-03-10T14:00:00"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "petId": 1,
  "ownerId": 1,
  "veterinarianId": null,
  "title": "Annual Checkup",
  "description": "Regular health examination",
  "symptoms": "No specific symptoms",
  "diagnosis": null,
  "notes": null,
  "status": "PENDING",
  "consultationDate": "2026-03-10T14:00:00",
  "completedAt": null,
  "rating": null,
  "feedback": null
}
```

**Required Role:** OWNER

**Status Values:** PENDING, IN_PROGRESS, COMPLETED, CANCELLED

---

### 14. Get Consultation by ID

Get details of a specific consultation.

**Endpoint:** `GET /api/consultations/{id}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Consultation ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "petId": 1,
  "ownerId": 1,
  "veterinarianId": 2,
  "title": "Annual Checkup",
  "description": "Regular health examination",
  "symptoms": "No specific symptoms",
  "diagnosis": "Healthy, all vitals normal",
  "notes": "Dog is in excellent health",
  "status": "COMPLETED",
  "consultationDate": "2026-03-10T14:00:00",
  "completedAt": "2026-03-10T15:00:00",
  "rating": 5.0,
  "feedback": "Great service!"
}
```

**Required Role:** OWNER, VETERINARIAN, or ADMIN

---

### 15. Get Consultations by Owner

Get all consultations for a specific owner.

**Endpoint:** `GET /api/consultations/owner/{ownerId}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| ownerId | Long | Owner ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "petId": 1,
    "ownerId": 1,
    "veterinarianId": 2,
    "title": "Annual Checkup",
    "description": "Regular health examination",
    "symptoms": "No specific symptoms",
    "diagnosis": "Healthy, all vitals normal",
    "notes": "Dog is in excellent health",
    "status": "COMPLETED",
    "consultationDate": "2026-03-10T14:00:00",
    "completedAt": "2026-03-10T15:00:00",
    "rating": 5.0,
    "feedback": "Great service!"
  }
]
```

**Required Role:** OWNER or ADMIN

---

### 16. Get Consultations by Veterinarian

Get all consultations assigned to a veterinarian.

**Endpoint:** `GET /api/consultations/veterinarian/{veterinarianId}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| veterinarianId | Long | Veterinarian ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "petId": 1,
    "ownerId": 1,
    "veterinarianId": 2,
    "title": "Annual Checkup",
    "description": "Regular health examination",
    "symptoms": "No specific symptoms",
    "diagnosis": "Healthy, all vitals normal",
    "notes": "Dog is in excellent health",
    "status": "COMPLETED",
    "consultationDate": "2026-03-10T14:00:00",
    "completedAt": "2026-03-10T15:00:00",
    "rating": 5.0,
    "feedback": "Great service!"
  }
]
```

**Required Role:** VETERINARIAN or ADMIN

---

### 17. Get Consultations by Pet

Get all consultations for a specific pet.

**Endpoint:** `GET /api/consultations/pet/{petId}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| petId | Long | Pet ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "petId": 1,
    "ownerId": 1,
    "veterinarianId": 2,
    "title": "Annual Checkup",
    "description": "Regular health examination",
    "symptoms": "No specific symptoms",
    "diagnosis": "Healthy, all vitals normal",
    "notes": "Dog is in excellent health",
    "status": "COMPLETED",
    "consultationDate": "2026-03-10T14:00:00",
    "completedAt": "2026-03-10T15:00:00",
    "rating": 5.0,
    "feedback": "Great service!"
  }
]
```

**Required Role:** OWNER, VETERINARIAN, or ADMIN

---

### 18. Get All Consultations

Get all consultations in the system. Admin only.

**Endpoint:** `GET /api/consultations`

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "petId": 1,
    "ownerId": 1,
    "veterinarianId": 2,
    "title": "Annual Checkup",
    "description": "Regular health examination",
    "symptoms": "No specific symptoms",
    "diagnosis": "Healthy, all vitals normal",
    "notes": "Dog is in excellent health",
    "status": "COMPLETED",
    "consultationDate": "2026-03-10T14:00:00",
    "completedAt": "2026-03-10T15:00:00",
    "rating": 5.0,
    "feedback": "Great service!"
  }
]
```

**Required Role:** ADMIN

---

### 19. Update Consultation

Update an existing consultation.

**Endpoint:** `PUT /api/consultations/{id}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Consultation ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

**Request:**
```json
{
  "petId": 1,
  "title": "Annual Checkup",
  "description": "Regular health examination completed",
  "symptoms": "No specific symptoms",
  "diagnosis": "Healthy, all vitals normal",
  "notes": "Dog is in excellent health. Recommend continued exercise and balanced diet.",
  "status": "COMPLETED",
  "veterinarianId": 2
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "petId": 1,
  "ownerId": 1,
  "veterinarianId": 2,
  "title": "Annual Checkup",
  "description": "Regular health examination completed",
  "symptoms": "No specific symptoms",
  "diagnosis": "Healthy, all vitals normal",
  "notes": "Dog is in excellent health. Recommend continued exercise and balanced diet.",
  "status": "COMPLETED",
  "consultationDate": "2026-03-10T14:00:00",
  "completedAt": "2026-03-10T15:00:00",
  "rating": null,
  "feedback": null
}
```

**Required Role:** VETERINARIAN or ADMIN

---

### 20. Assign Veterinarian

Assign a veterinarian to a consultation.

**Endpoint:** `PUT /api/consultations/{id}/assign-veterinarian/{veterinarianId}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Consultation ID |
| veterinarianId | Long | Veterinarian ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "petId": 1,
  "ownerId": 1,
  "veterinarianId": 2,
  "title": "Annual Checkup",
  "description": "Regular health examination",
  "symptoms": "No specific symptoms",
  "diagnosis": null,
  "notes": null,
  "status": "IN_PROGRESS",
  "consultationDate": "2026-03-10T14:00:00",
  "completedAt": null,
  "rating": null,
  "feedback": null
}
```

**Required Role:** ADMIN

---

### 21. Rate Consultation

Rate a completed consultation.

**Endpoint:** `PUT /api/consultations/{id}/rate?rating={rating}&feedback={feedback}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Consultation ID |

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| rating | Float | Rating (1.0 to 5.0) |
| feedback | String | Optional feedback text |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "petId": 1,
  "ownerId": 1,
  "veterinarianId": 2,
  "title": "Annual Checkup",
  "description": "Regular health examination",
  "symptoms": "No specific symptoms",
  "diagnosis": "Healthy, all vitals normal",
  "notes": "Dog is in excellent health",
  "status": "COMPLETED",
  "consultationDate": "2026-03-10T14:00:00",
  "completedAt": "2026-03-10T15:00:00",
  "rating": 5.0,
  "feedback": "Excellent service!"
}
```

**Required Role:** OWNER

---

### 22. Delete Consultation

Delete a consultation from the system.

**Endpoint:** `DELETE /api/consultations/{id}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Consultation ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (204 No Content):** Empty response

**Required Role:** ADMIN

---

## Prescription Endpoints

### 23. Create Prescription

Create a new prescription for a consultation.

**Endpoint:** `POST /api/prescriptions`

**Headers:**
```
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

**Request:**
```json
{
  "consultationId": 1,
  "petId": 1,
  "veterinarianId": 2,
  "medicationName": "Amoxicillin",
  "dosage": "250mg",
  "frequency": "Twice daily",
  "duration": 7,
  "instructions": "Take with food",
  "sideEffects": "May cause mild stomach upset",
  "status": "ACTIVE",
  "prescribedDate": "2026-03-10",
  "startDate": "2026-03-10",
  "endDate": "2026-03-17"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "consultationId": 1,
  "petId": 1,
  "veterinarianId": 2,
  "medicationName": "Amoxicillin",
  "dosage": "250mg",
  "frequency": "Twice daily",
  "duration": 7,
  "instructions": "Take with food",
  "sideEffects": "May cause mild stomach upset",
  "status": "ACTIVE",
  "prescribedDate": "2026-03-10",
  "startDate": "2026-03-10",
  "endDate": "2026-03-17",
  "notes": null
}
```

**Required Role:** VETERINARIAN

**Status Values:** ACTIVE, COMPLETED, CANCELLED

---

### 24. Get Prescription by ID

Get details of a specific prescription.

**Endpoint:** `GET /api/prescriptions/{id}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Prescription ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "consultationId": 1,
  "petId": 1,
  "veterinarianId": 2,
  "medicationName": "Amoxicillin",
  "dosage": "250mg",
  "frequency": "Twice daily",
  "duration": 7,
  "instructions": "Take with food",
  "sideEffects": "May cause mild stomach upset",
  "status": "ACTIVE",
  "prescribedDate": "2026-03-10",
  "startDate": "2026-03-10",
  "endDate": "2026-03-17",
  "notes": null
}
```

**Required Role:** OWNER, VETERINARIAN, or ADMIN

---

### 25. Get Prescriptions by Consultation

Get all prescriptions for a specific consultation.

**Endpoint:** `GET /api/prescriptions/consultation/{consultationId}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| consultationId | Long | Consultation ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "consultationId": 1,
    "petId": 1,
    "veterinarianId": 2,
    "medicationName": "Amoxicillin",
    "dosage": "250mg",
    "frequency": "Twice daily",
    "duration": 7,
    "instructions": "Take with food",
    "sideEffects": "May cause mild stomach upset",
    "status": "ACTIVE",
    "prescribedDate": "2026-03-10",
    "startDate": "2026-03-10",
    "endDate": "2026-03-17",
    "notes": null
  }
]
```

**Required Role:** OWNER, VETERINARIAN, or ADMIN

---

### 26. Get Prescriptions by Pet

Get all prescriptions for a specific pet.

**Endpoint:** `GET /api/prescriptions/pet/{petId}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| petId | Long | Pet ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "consultationId": 1,
    "petId": 1,
    "veterinarianId": 2,
    "medicationName": "Amoxicillin",
    "dosage": "250mg",
    "frequency": "Twice daily",
    "duration": 7,
    "instructions": "Take with food",
    "sideEffects": "May cause mild stomach upset",
    "status": "ACTIVE",
    "prescribedDate": "2026-03-10",
    "startDate": "2026-03-10",
    "endDate": "2026-03-17",
    "notes": null
  }
]
```

**Required Role:** OWNER, VETERINARIAN, or ADMIN

---

### 27. Get Prescriptions by Veterinarian

Get all prescriptions created by a specific veterinarian.

**Endpoint:** `GET /api/prescriptions/veterinarian/{veterinarianId}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| veterinarianId | Long | Veterinarian ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "consultationId": 1,
    "petId": 1,
    "veterinarianId": 2,
    "medicationName": "Amoxicillin",
    "dosage": "250mg",
    "frequency": "Twice daily",
    "duration": 7,
    "instructions": "Take with food",
    "sideEffects": "May cause mild stomach upset",
    "status": "ACTIVE",
    "prescribedDate": "2026-03-10",
    "startDate": "2026-03-10",
    "endDate": "2026-03-17",
    "notes": null
  }
]
```

**Required Role:** VETERINARIAN or ADMIN

---

### 28. Get All Prescriptions

Get all prescriptions in the system. Admin only.

**Endpoint:** `GET /api/prescriptions`

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "consultationId": 1,
    "petId": 1,
    "veterinarianId": 2,
    "medicationName": "Amoxicillin",
    "dosage": "250mg",
    "frequency": "Twice daily",
    "duration": 7,
    "instructions": "Take with food",
    "sideEffects": "May cause mild stomach upset",
    "status": "ACTIVE",
    "prescribedDate": "2026-03-10",
    "startDate": "2026-03-10",
    "endDate": "2026-03-17",
    "notes": null
  }
]
```

**Required Role:** ADMIN

---

### 29. Update Prescription

Update an existing prescription.

**Endpoint:** `PUT /api/prescriptions/{id}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Prescription ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

**Request:**
```json
{
  "consultationId": 1,
  "petId": 1,
  "veterinarianId": 2,
  "medicationName": "Amoxicillin",
  "dosage": "250mg",
  "frequency": "Twice daily",
  "duration": 7,
  "instructions": "Take with food after meals",
  "sideEffects": "May cause mild stomach upset",
  "status": "COMPLETED",
  "prescribedDate": "2026-03-10",
  "startDate": "2026-03-10",
  "endDate": "2026-03-17",
  "notes": "Patient responded well to treatment"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "consultationId": 1,
  "petId": 1,
  "veterinarianId": 2,
  "medicationName": "Amoxicillin",
  "dosage": "250mg",
  "frequency": "Twice daily",
  "duration": 7,
  "instructions": "Take with food after meals",
  "sideEffects": "May cause mild stomach upset",
  "status": "COMPLETED",
  "prescribedDate": "2026-03-10",
  "startDate": "2026-03-10",
  "endDate": "2026-03-17",
  "notes": "Patient responded well to treatment"
}
```

**Required Role:** VETERINARIAN or ADMIN

---

### 30. Delete Prescription

Delete a prescription from the system.

**Endpoint:** `DELETE /api/prescriptions/{id}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | Long | Prescription ID |

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

**Response (204 No Content):** Empty response

**Required Role:** VETERINARIAN or ADMIN

---

## Postman Collection Setup

### Creating Variables

1. Open Postman and create a new Collection
2. Go to the collection's **Variables** tab
3. Add the following variables:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| baseUrl | http://localhost:8081/api | http://localhost:8081/api |
| accessToken | (empty) | (will be set after login) |
| refreshToken | (empty) | (will be set after login) |
| ownerId | 1 | 1 |
| vetId | 2 | 2 |
| petId | 1 | 1 |
| consultationId | 1 | 1 |
| prescriptionId | 1 | 1 |

### Setting Up Authentication

1. Create a **Login** request
2. After getting the response, add a **Tests** script to save the token:

```javascript
// Login Success Test Script
var jsonData = pm.response.json();
pm.collectionVariables.set("accessToken", jsonData.token);
pm.collectionVariables.set("refreshToken", jsonData.refreshToken);
pm.collectionVariables.set("ownerId", jsonData.user.id);
```

### Common Error Responses

**401 Unauthorized:**
```json
{
  "timestamp": "2026-03-02T10:30:00",
  "message": "Invalid email or password",
  "details": "uri=/api/auth/login"
}
```

**403 Forbidden:**
```json
{
  "timestamp": "2026-03-02T10:30:00",
  "message": "Access Denied",
  "details": "uri=/api/consultations"
}
```

**404 Not Found:**
```json
{
  "timestamp": "2026-03-02T10:30:00",
  "message": "User not found with id: 999",
  "details": "uri=/api/users/999"
}
```

**500 Internal Server Error:**
```json
{
  "timestamp": "2026-03-02T10:30:00",
  "message": "Email already exists",
  "details": "uri=/api/auth/register"
}
```

---

## Quick Reference

### All Endpoints Summary

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | /auth/register | Register new user | Public |
| POST | /auth/login | Login | Public |
| POST | /auth/refresh-token | Refresh token | Public |
| GET | /users/me | Get current user | Any authenticated |
| GET | /users/{id} | Get user by ID | VETERINARIAN, ADMIN |
| POST | /pets | Create pet | OWNER, VETERINARIAN |
| GET | /pets/{id} | Get pet by ID | OWNER, VETERINARIAN |
| GET | /pets/owner/{ownerId} | Get pets by owner | OWNER, VETERINARIAN |
| GET | /pets | Get all pets | VETERINARIAN, ADMIN |
| PUT | /pets/{id} | Update pet | OWNER, VETERINARIAN |
| DELETE | /pets/{id} | Delete pet | OWNER, ADMIN |
| POST | /consultations | Create consultation | OWNER |
| GET | /consultations/{id} | Get consultation | OWNER, VETERINARIAN, ADMIN |
| GET | /consultations/owner/{ownerId} | Get by owner | OWNER, ADMIN |
| GET | /consultations/veterinarian/{vetId} | Get by vet | VETERINARIAN, ADMIN |
| GET | /consultations/pet/{petId} | Get by pet | OWNER, VETERINARIAN, ADMIN |
| GET | /consultations | Get all | ADMIN |
| PUT | /consultations/{id} | Update consultation | VETERINARIAN, ADMIN |
| PUT | /consultations/{id}/assign-vet/{vetId} | Assign vet | ADMIN |
| PUT | /consultations/{id}/rate | Rate consultation | OWNER |
| DELETE | /consultations/{id} | Delete consultation | ADMIN |
| POST | /prescriptions | Create prescription | VETERINARIAN |
| GET | /prescriptions/{id} | Get prescription | OWNER, VETERINARIAN, ADMIN |
| GET | /prescriptions/consultation/{id} | Get by consultation | OWNER, VETERINARIAN, ADMIN |
| GET | /prescriptions/pet/{petId} | Get by pet | OWNER, VETERINARIAN, ADMIN |
| GET | /prescriptions/veterinarian/{vetId} | Get by vet | VETERINARIAN, ADMIN |
| GET | /prescriptions | Get all | ADMIN |
| PUT | /prescriptions/{id} | Update prescription | VETERINARIAN, ADMIN |
| DELETE | /prescriptions/{id} | Delete prescription | VETERINARIAN, ADMIN |

---

**Document Version:** 1.0  
**Last Updated:** 2026-03-02  
**API Base URL:** http://localhost:8081/api
